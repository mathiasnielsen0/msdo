/*
 * Copyright (C) 2015 - 2023. Henrik Bærbak Christensen, Aarhus University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.cave.doubles;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.IntStream;

import cloud.cave.common.ServerConfiguration;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;
import cloud.cave.service.wall.MessageRecord;

import javax.servlet.http.HttpServletResponse;

/**
 * Fake object implementation of storage. Map data structures are used to
 * simulate SQL tables / NoSQL collections. The used data structures are not
 * synchronized, do not use in multi-threaded server!
 * <p>
 * The cave is initialized with five rooms in a fixed layout, vaguely inspired
 * by the original Colossal Cave layout. These rooms serve the test cases as
 * well as populate newly created caves.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class FakeCaveStorage implements CaveStorage {

  // The table/collection of rooms in the cave.
  // The positionString is the primary key and the value object
  // for a room the rest of the tuple
  private Map<String, RoomRecord> roomMap;
  // The table/colleciton of all messages in all rooms
  private Map<String, List<MessageRecord>> messageMap;

  // Strategy to delegate how to define timestamps
  private NowStrategy nowStrategy;

  public FakeCaveStorage() {
    // Default to a timestamp strategy that uses the real clock.
    nowStrategy = new RealNowStrategy();

    roomMap = new HashMap<String, RoomRecord>();
    messageMap = new HashMap<String, List<MessageRecord>>();

    playerId2PlayerSpecs = new HashMap<String, PlayerRecord>(5);
  }

  public void setNowStrategy(NowStrategy nowStrategy) {
    this.nowStrategy = nowStrategy;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.serverConfiguration = config;
    // Initialize the default room layout
    RoomRecord entryRoom = new RoomRecord(
        "You are standing at the end of a road before a small brick building.",
            WILL_CROWTHER_ID);
    this.addRoom(new Point3(0, 0, 0).getPositionString(), entryRoom);
    this.addRoom(new Point3(0, 1, 0).getPositionString(), new RoomRecord(
        "You are in open forest, with a deep valley to one side.", WILL_CROWTHER_ID));
    this.addRoom(new Point3(1, 0, 0).getPositionString(), new RoomRecord(
        "You are inside a building, a well house for a large spring.", WILL_CROWTHER_ID));
    this.addRoom(new Point3(-1, 0, 0).getPositionString(), new RoomRecord(
        "You have walked up a hill, still in the forest.", WILL_CROWTHER_ID));
    this.addRoom(new Point3(0, 0, 1).getPositionString(), new RoomRecord(
        "You are in the top of a tall tree, at the end of a road.", WILL_CROWTHER_ID));
  }
  
  @Override
  public void disconnect() {
    roomMap = null;
  }

  @Override
  public RoomRecord getRoom(String positionString) {
    return roomMap.get(positionString);
  }

  @Override
  public int addRoom(String positionString, RoomRecord newRoom) {
    // if there is already a room, return FORBIDDEN
    if ( roomMap.containsKey(positionString) ) { return HttpServletResponse.SC_FORBIDDEN; }

    // Simulate classic DB behaviour: timestamp record and
    // assign unique id
    RoomRecord recordInDB = new RoomRecord(newRoom);
    ZonedDateTime now = nowStrategy.now();
    recordInDB.setCreationTime(now);
    recordInDB.setId(UUID.randomUUID().toString());

    roomMap.put(positionString, recordInDB);
    return HttpServletResponse.SC_CREATED;
  }

  @Override
  public int updateRoom(String positionString, RoomRecord updatedRoom) {
    // if room does not exist, return 404 NOT FOUND
    if ( !roomMap.containsKey(positionString) ) { return HttpServletResponse.SC_NOT_FOUND; }

    RoomRecord presentRecord = roomMap.get(positionString);
    if (! presentRecord.getCreatorId().equals(updatedRoom.getCreatorId())) {
      return HttpServletResponse.SC_UNAUTHORIZED;
    }

    roomMap.put(positionString, updatedRoom);
    return HttpServletResponse.SC_OK;
  }

  @Override
  public List<Direction> getSetOfExitsFromRoom(String positionString) {
    List<Direction> listOfExits = new ArrayList<Direction>();
    Point3 pZero = Point3.parseString(positionString);
    Point3 p;
    for ( Direction d : Direction.values()) {
      p = new Point3(pZero.x(), pZero.y(), pZero.z());
      p.translate(d);
      String position = p.getPositionString();
      if ( roomMap.containsKey(position)) {
        listOfExits.add(d);
      }
    }
    return listOfExits;
  }

  // === The table with primary key playerID whose columns are the
  // specifications of a given player. The private datastructure PlayerSpecs
  // represents the
  // remaining tuple values.
  
  Map<String,PlayerRecord> playerId2PlayerSpecs;

  @Override
  public PlayerRecord getPlayerByID(String playerID) {
    PlayerRecord ps = playerId2PlayerSpecs.get(playerID);
    return ps;
  }
  
  @Override
  public void updatePlayerRecord(PlayerRecord record) {
    playerId2PlayerSpecs.put(record.getPlayerID(), record);
  }

  @Override
  public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
    List<PlayerRecord> theList = new ArrayList<PlayerRecord>();
    for ( String id : playerId2PlayerSpecs.keySet() ) {
      PlayerRecord ps = playerId2PlayerSpecs.get(id);
      if (ps.isInCave() && ps.getPositionAsString().equals(positionString)) {
        theList.add(ps);
      }
    }
    return theList;
  }
  
  @Override
  public void addMessage(String positionInCave, MessageRecord messageRecord) {
    List<MessageRecord> msgInThisRoom = getMessageMapForRoom(positionInCave);
    // Simulate 'classic DB' behaviour, assign unique
    // id to item and timestamp it
    MessageRecord newRecord = new MessageRecord(messageRecord);
    ZonedDateTime now = nowStrategy.now();
    newRecord.setCreatorTimeStampISO8601(now);
    newRecord.setId(UUID.randomUUID().toString());

    // Append to position 0, so older records are pushed towards the end
    msgInThisRoom.add(0, newRecord);
    messageMap.put(positionInCave, msgInThisRoom);
  }

  @Override
  public int updateMessage(String positionInCave, String messageId, MessageRecord newMessageRecord) {
    List<MessageRecord> msgInThisRoom = getMessageMapForRoom(positionInCave);

    // Search and find index of first message with same ID as updating message
    OptionalInt indexOpt = IntStream.range(0, msgInThisRoom.size())
            .filter(i -> messageId.equals(msgInThisRoom.get(i).getId()))
            .findFirst();

    // Bail out if no message with given id exists
    if (! indexOpt.isPresent()) {
      return HttpServletResponse.SC_NOT_FOUND;
    }

    MessageRecord match = match = msgInThisRoom.get(indexOpt.getAsInt());

    // Bail out if the found message was not created by same person
    if (! match.getCreatorId().equals(newMessageRecord.getCreatorId())) {
      return HttpServletResponse.SC_UNAUTHORIZED;
    }

    // Update message and enter it back into the list
    MessageRecord updatedOne = new MessageRecord(match);
    updatedOne.setContents(newMessageRecord.getContents());
    msgInThisRoom.set(indexOpt.getAsInt(), updatedOne);

    return HttpServletResponse.SC_OK;
  }

  @Override
  public List<MessageRecord> getMessageList(String positionInCave, int startIndex, int pageSize) {
    List<MessageRecord> msgInThisRoom = getMessageMapForRoom(positionInCave);
    int size = msgInThisRoom.size();
    int firstIndex = startIndex > size ? size : startIndex;
    int lastIndex = startIndex + pageSize > size ? size : startIndex + pageSize;
    return msgInThisRoom.subList(firstIndex, lastIndex);
  }

  public List<MessageRecord> getMessageMapForRoom(String positionInCave) {
    List<MessageRecord> msgInThisRoom;
    msgInThisRoom = messageMap.get(positionInCave);
    if (msgInThisRoom == null) {
      msgInThisRoom = new ArrayList<MessageRecord>();
    }
    return msgInThisRoom;
  }

  public String toString() {
    return "FakeCaveStorage (" + roomMap.keySet().size() + " rooms. " +
        playerId2PlayerSpecs.keySet().size() + " players)";
  }

  private ServerConfiguration serverConfiguration;
  
  @Override
  public ServerConfiguration getConfiguration() {
    return serverConfiguration;
  }
  
}
