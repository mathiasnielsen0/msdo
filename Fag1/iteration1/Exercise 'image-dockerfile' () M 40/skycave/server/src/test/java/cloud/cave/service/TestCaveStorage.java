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

package cloud.cave.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.*;

import cloud.cave.common.ServerConfiguration;

import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.common.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;


/** TDD of (most of) the CaveStorage interface and
 * driving the FakeObject implementation.
 *
 * Some test cases are grouped in other test classes
 * as they serve for exercises.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestCaveStorage {

  private CaveStorage storage;
  
  // Two subscription records, tied to id1 and id2
  private SubscriptionRecord sub1, sub2;
  
  private String id1 = "id02";
  private String id2 = "id-203212";
  
  private Point3 p000 = new Point3(0, 0, 0);

  private Point3 p876 = new Point3(8,7,6);
  private Point3 p273 = new Point3(2,7,3);

  @BeforeEach
  public void setUp() throws Exception {
    storage = new FakeCaveStorage();
    storage.initialize(null, null);
    
    sub1 = new SubscriptionRecord(id1,"Tutmosis", "grp01", Region.ODENSE);
    sub2 = new SubscriptionRecord(id2, "MrLongName", "grp02", Region.COPENHAGEN);
  }
  
  @AfterEach
  public void tearDown() {
    storage.disconnect();
  }

  @Test
  public void shouldReadAndCreateRoomInStorage() {
    RoomRecord room = storage.getRoom(p000.getPositionString());
    assertThat(room.getDescription(), is("You are standing at the end of a road before a small brick building."));
    assertThat(room.getCreatorId(), is(CaveStorage.WILL_CROWTHER_ID));
    
    p000 = new Point3(-1,0,0);
    room = storage.getRoom(p000.getPositionString());
    assertThat(room.getDescription(), containsString("You have walked up a hill, still"));
    assertThat(room.getCreatorId(), is(CaveStorage.WILL_CROWTHER_ID));

    // validate that rooms can be made
    int canAdd = storage.addRoom(p273.getPositionString(),
            new RoomRecord("You are in a dark lecturing hall.", "ArneID"));
    assertThat(canAdd, is(HttpServletResponse.SC_CREATED));

    room = storage.getRoom(p273.getPositionString());
    assertThat(room.getDescription(), is("You are in a dark lecturing hall."));
    assertThat(room.getCreatorId(), is("ArneID"));

    // validate that existing rooms cannot be overridden
    canAdd = storage.addRoom(p273.getPositionString(),
            new RoomRecord("This room must never be made", "BlackHat"));
    
    assertThat(canAdd, is(HttpServletResponse.SC_FORBIDDEN));
  }
  
  @Test
  public void shouldGetExitSet() {
    List<Direction> exits = storage.getSetOfExitsFromRoom(p000.getPositionString());
    assertThat( exits.size(), is(4));
    
    assertThat( exits.contains(Direction.EAST), is(true) );
    assertThat( exits.contains(Direction.WEST), is(true) );
    assertThat( exits.contains(Direction.NORTH), is(true) );
    assertThat( exits.contains(Direction.SOUTH), is(false) );
    assertThat( exits.contains(Direction.DOWN), is(false) );
    assertThat( exits.contains(Direction.UP), is(true) );
  }
  
  @Test
  public void shouldUpdatePlayerAndPositionTables() {
    // Add player
    SubscriptionRecord sub01 = sub1;
    addPlayerRecordToStorageForSubscription(sub01);

    // and move him to position 2,7,3
    updatePlayerPosition(id1, p273.getPositionString());

    // Tutmosis is in the cave
    assertThat(storage.getPlayerByID(id1).getPlayerName(), is("Tutmosis"));
    assertThat(storage.getPlayerByID(id1).isInCave(), is(true));
    
    // get all players at 2,7,3
    List<PlayerRecord>  ll = storage.computeListOfPlayersAt(p273.getPositionString());
    
    assertThat( ll.size(), is(1));
    assertThat( ll.get(0).getPlayerID(), is(id1));
    
    // and verify none are at 8,7,6
    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(0));
    
    // Intro another player
    addPlayerRecordToStorageForSubscription(sub2);

    // move player 2 to same 8,7,6
    updatePlayerPosition(id2, p876.getPositionString());

    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(1));
    assertThat( ll.get(0).getPlayerID(), is(id2));
    
    // move player 1 there also
    updatePlayerPosition(id1, p876.getPositionString());
    
    // and verify that computation is correct
    ll = storage.computeListOfPlayersAt(p876.getPositionString());
    assertThat( ll.size(), is(2));
    assertThat( ll.get(0).getPlayerID(), either(is(id1)).
        or(is(id2)));
    assertThat( ll.get(1).getPlayerID(), either(is(id1)).
        or(is(id2)));
  }

  private void updatePlayerPosition(String id12, String positionString) {
    PlayerRecord pRecord = storage.getPlayerByID(id12);
    pRecord.setPositionAsString(positionString);
    storage.updatePlayerRecord(pRecord);
  }

  private void addPlayerRecordToStorageForSubscription(SubscriptionRecord sub01) {
    PlayerRecord rec1 = new PlayerRecord(sub01, "(0,0,0)");
    storage.updatePlayerRecord(rec1);
  }
  
  @Test
  public void shouldUpdatePlayerTables() {
    addPlayerRecordToStorageForSubscription(sub1);
    addPlayerRecordToStorageForSubscription(sub2);

    // end session for player one
    PlayerRecord rec1 = storage.getPlayerByID(id1);
    rec1.setAccessToken(null);
    storage.updatePlayerRecord(rec1);
    
    // and the right one is left
    PlayerRecord p;
    p = storage.getPlayerByID(id1);
    assertThat( p.isInCave(), is(false));
    
    p = storage.getPlayerByID(id2);
    assertThat( p.isInCave(), is(true));
  }
  
  @Test
  public void shouldGetPlayerByID() {
    addPlayerRecordToStorageForSubscription(sub1);
    addPlayerRecordToStorageForSubscription(sub2);
    
    PlayerRecord p;
    p = storage.getPlayerByID(id1);
    assertThat( p.getPlayerName(), is("Tutmosis"));
    p = storage.getPlayerByID(id2);
    assertThat( p.getPlayerName(), is("MrLongName"));
  }
  
  @Test
  public void shouldInchUpCoverageOfPoint() {
    // Not really a storage test, but increases
    // test coverage just a tiny bit, and Point3
    // is closely associated with storage
    assertThat(p876.toString(), is("(8,7,6)"));
  }

  @Test
  public void shouldIncreaseCoverageForCaveStorage() {
    String t = storage.toString();
    assertThat(t, is("FakeCaveStorage (5 rooms. 0 players)"));
    ServerConfiguration cfg = storage.getConfiguration();
    assertThat(cfg, is(nullValue()));
  }

  // Test of the 2020 introduced room update methods
  @Test
  public void shouldAllowCreatorToUpdateRoom() {
    // Given a room added
    RoomRecord room = new RoomRecord("You are in a dark lecturing hall.", "Arne");
    int canAdd = storage.addRoom(p273.getPositionString(), room);
    assertThat(canAdd, is(HttpServletResponse.SC_CREATED));

    // When same creator updates it
    RoomRecord updatedRoom = new RoomRecord(room);
    updatedRoom.setDescription("A BRIGHT lecturing hall.");
    int status = storage.updateRoom(p273.getPositionString(), updatedRoom);

    // Then status is OK and the description updated
    assertThat(status, is(HttpServletResponse.SC_OK));
    assertThat(storage.getRoom(p273.getPositionString()).getDescription(), is("A BRIGHT lecturing hall."));
  }

  @Test
  public void shouldNotAllowUpdateOfNonExistingRoom() {
    // Given a non existing room position
    Point3 p777 = new Point3(7,7,7);
    // When updating room
    RoomRecord updatedRoom = new RoomRecord("A description", "hans");
    // Then 404 is returned
    int status = storage.updateRoom(p777.getPositionString(), updatedRoom);
    assertThat(status, is(HttpServletResponse.SC_NOT_FOUND));
    // and no room is created
    assertThat(storage.getRoom(p777.getPositionString()), is(nullValue()));
  }

  @Test
  public void shouldNotAllowNonCreatorToUpdateRoom() {
    // Given the room by Crowther at 0,0,0

    // When hans wants to update room
    RoomRecord updatedRoom = new RoomRecord("A description", "hans");
    // Then 401 is returned
    int status = storage.updateRoom(p000.getPositionString(), updatedRoom);
    assertThat(status, is(HttpServletResponse.SC_UNAUTHORIZED));
    // and room is not changed
    assertThat(storage.getRoom(p000.getPositionString()).getDescription(),
            containsString("You are standing at the end of a road"));
  }
}
