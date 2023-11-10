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

package cloud.cave.server;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.service.quote.QuoteRecord;
import cloud.cave.service.wall.MessageRecord;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Servant implementation of Player (Servant role in Broker), that is, the
 * domain implementation on the server side.
 * <p>
 * Interacts with underlying persistent storage for mutator methods,
 * and some of the more complex accessor methods.
 * <p>
 * In general, DO NOT create instances of this class directly.
 * Use the 'cave.login()' method instead.
 * </p>
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 */
public class PlayerServant implements Player {
  /**
   * The classpath used to search for Command objects
   */
  public static final String EXTENSION_CLASSPATH = "cloud.cave.extension";

  private String ID;
  private String accessToken;

  // These attributes of the player are essentially
  // caching of the 'true' information which is stored in
  // the underlying cave storage.
  private String name;
  private String groupName;
  private Region region;
  private RoomRecord currentRoom;
  private String position;

  private ObjectManager objectManager;

  /* It makes sense to cache the storage connector as we
   * use it very often.
   */
  private CaveStorage storage;
  private LoginResult authenticationStatus;


  /**
   * Never call this constructor directly, use cave.login() instead.
   * Create a player instance with the given player id, status of
   * authentication and object manager for delegates.
   *
   * @param authenticationStatus
   * @param playerID
   * @param objectManager
   */
  public PlayerServant(LoginResult authenticationStatus, String playerID, ObjectManager objectManager) {
    this(playerID, objectManager);
    this.authenticationStatus = authenticationStatus;
  }

  /**
   * Never call this constructor directly, use cave.login() instead, EXCEPT
   * for the PlayerDispatcher which can assume a player has already been
   * logged in. Create a
   * player instance bound to the given delegates for session caching,
   * persistence, etc.
   *
   * @param playerID      the player's id
   * @param objectManager the object manager holding all delegates
   */
  public PlayerServant(String playerID, ObjectManager objectManager) {
    this.ID = playerID;
    this.objectManager = objectManager;
    this.storage = objectManager.getCaveStorage();
    // This constructor assumes the user has already been logged in.
    this.authenticationStatus = LoginResult.LOGIN_SUCCESS;

    refreshFromStorage();
  }

  @Override
  public LoginResult getAuthenticationStatus() {
    return authenticationStatus;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public String getID() {
    return ID;
  }

  @Override
  public String getShortRoomDescription() {
    return currentRoom.getDescription();
  }

  @Override
  public List<String> getLongRoomDescription() {
    List<String> buf = new ArrayList<>();
    buf.add(currentRoom.getDescription());

    // append creator name
    String creatorId = currentRoom.getCreatorId();

    // Handle special case of the initial rooms
    String creatorName = "Will Crowther";
    if (!creatorId.equals(CaveStorage.WILL_CROWTHER_ID)) { // REMEMBER: IDs are STRINGS
      PlayerRecord creator = storage.getPlayerByID(creatorId);
      creatorName = creator.getPlayerName();
    }
    buf.add("  Creator: " + creatorName + ", "
            + Util.calcSinceNow(currentRoom.getCreationTimeISO8601())
            + ".");

    // append list of exits from room
    buf.add("There are exits in directions:");
    StringBuffer tmp = new StringBuffer();
    for (Direction dir : getExitSet()) {
      tmp.append("  " + dir + " ");
    }
    buf.add(tmp.toString());

    // and the list of players currently in room
    buf.add("You see other players:");
    List<String> playerNameList = getPlayersHere();
    int count = 0;
    tmp = new StringBuffer();
    for (String p : playerNameList) {
      tmp.append("  [" + count + "] " + p);
      count++;
    }
    buf.add(tmp.toString());
    return buf;
  }

  @Override
  public List<Direction> getExitSet() {
    // Cannot use cache, others may influence cave 
    return storage.getSetOfExitsFromRoom(position);
  }

  @Override
  public Region getRegion() {
    return region; // Use the cached value
  }

  @Override
  public String getPosition() {
    return position; // Use the cached value
  }

  @Override
  public List<String> getPlayersHere() {
    List<String> playerNameList =
            storage.computeListOfPlayersAt(getPosition())
                    .stream()
                    .map( record-> record.getPlayerName())
                    .collect(Collectors.toList());
    return playerNameList;
  }

  @Override
  public String getAccessToken() {
    return accessToken; // Use the cached value
  }

  @Override
  public void addMessage(String message) {
    MessageRecord msg = new MessageRecord(message, getID(), getName());
    storage.addMessage(getPosition(), msg);
  }

  @Override
  public UpdateResult updateMessage(String messageId, String newContents) {
    MessageRecord msg = new MessageRecord(newContents, getID(), getName());
    int status = storage.updateMessage(getPosition(), messageId, msg);
    UpdateResult result = UpdateResult.translateFromHTTPStatusCode(status);
    return result;
  }

  @Override
  public List<WallMessageDataTransferObject> getMessageList(int pageNumber) {
    List<MessageRecord> messageListFromStorage = storage.getMessageList(getPosition(),
            pageNumber * WALL_PAGE_SIZE, WALL_PAGE_SIZE);
    List<WallMessageDataTransferObject> contents =
            messageListFromStorage
                    .stream()
                    .map(msg -> new WallMessageDataTransferObject(msg.getId(), Util.formatWallPosting(msg)))
                    .collect(Collectors.toList());
    return contents;
  }

  @Override
  public String getQuote(int quoteIndex) {
    // TODO: Exercise 'random-quote-double' requires random quotes as indicated by 'quoteIndex==0'

    // TODO: Exercise 'quote-double' must introduce proper test doubles instead of these
    // hardcoded test values
    if (quoteIndex == 1)
      return "Take small steps - use the ladder, not the vaulting pole. - Henrik Bærbak Christensen";
    // Sorry for the complacency :)
    if (quoteIndex == 7)
      return "The true sign of intelligence is not knowledge but imagination. - Albert Einstein";
    if (quoteIndex == 13)
      return "Education is what remains after one has forgotten what one has learned in school. - Albert Einstein";

    return "*The requested quote was not found*";
  }


  private String convertToStringFormat(QuoteRecord quoteAsJSON) {
    if (quoteAsJSON.getStatusCode() == HttpServletResponse.SC_OK) {
      return quoteAsJSON.getQuote() + " - " + quoteAsJSON.getAuthor();
    }
    return "*The requested quote was not found*";
  }


  @Override
  public UpdateResult move(Direction direction) {
    // Convert present room position into Point3 which
    // allows computations
    Point3 presentPosition = Point3.parseString(position);

    // Clone it; we need the values of both present and
    // new position
    Point3 newPosition = (Point3) presentPosition.clone();

    // Calculate a new position given the movement direction
    newPosition.translate(direction);
    // convert to the new position in string format
    String newPositionAsString = newPosition.getPositionString();
    // get the room in that direction
    RoomRecord newRoom = storage.getRoom(newPositionAsString);

    // if it is null, then there is no room in that direction
    // and we return without any state modifications
    if (newRoom == null) {
      return UpdateResult.FAIL_AS_NOT_FOUND;
    }

    updateStateAndStorageToNewPosition(newPositionAsString, newRoom);

    return UpdateResult.UPDATE_OK;
  }

  private void updateStateAndStorageToNewPosition(String newPositionAsString,
                                                  RoomRecord newRoom) {
    // update internal state variables
    position = newPositionAsString;
    currentRoom = newRoom;

    // and update this player's position in the storage
    PlayerRecord pRecord = storage.getPlayerByID(getID());
    pRecord.setPositionAsString(position);
    storage.updatePlayerRecord(pRecord);
  }

  @Override
  public UpdateResult digRoom(Direction direction, String description) {
    // Calculate the offsets in the given direction
    Point3 p = Point3.parseString(position);
    p.translate(direction);
    RoomRecord room = new RoomRecord(description, getID());
    return UpdateResult.translateFromHTTPStatusCode(storage.addRoom(p.getPositionString(), room));
  }

  @Override
  public UpdateResult updateRoom(String newDescription) {
    // We do not need to fetch the room record from storage
    // as only the author is allowed to change it, thus
    // our cached room must be correct.
    RoomRecord updatedRoom = new RoomRecord(currentRoom);
    // overwrite creator and description
    updatedRoom.setCreatorId(getID());
    updatedRoom.setDescription(newDescription);
    int statusCode = storage.updateRoom(getPosition(), updatedRoom);

    if (statusCode == HttpServletResponse.SC_OK) {
      // update internal cache
      currentRoom = updatedRoom;
      return UpdateResult.UPDATE_OK;
    }
    // Only other possibility is that the player is not
    // the owner
    return UpdateResult.FAIL_AS_NOT_CREATOR;
  }

  @Override
  public List<String> execute(String commandName, String... parameters) {
    // Compute the qualified path of the command class that shall be loaded
    String qualifiedClassName = EXTENSION_CLASSPATH + "." + commandName;

    // Load it
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedClassName);
    } catch (ClassNotFoundException e) {
      return Arrays.asList("Player.execute failed to load Command class: " + commandName);
    }

    // Next, instantiate the command object
    Command command = null;
    try {
      command = (Command) theClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      return Arrays.asList("Player.execute failed to instantiate Command object: " + commandName);
    }

    // Initialize the command object
    command.setPlayerID(getID());
    command.setObjectManager(objectManager);

    // And execute the command...
    List<String> reply = command.execute(parameters);

    // as the command may update any aspect of the player' data
    // and as we cache it here locally, invalidate the caching
    refreshFromStorage();

    return reply;
  }

  /**
   * Query the storage for the player record associated with the player ID, and
   * update all cached instance variables according to the read state.
   */
  private void refreshFromStorage() {
    PlayerRecord pr = storage.getPlayerByID(ID);
    name = pr.getPlayerName();
    groupName = pr.getGroupName();
    position = pr.getPositionAsString();
    region = pr.getRegion();
    accessToken = pr.getAccessToken();

    currentRoom = storage.getRoom(position);
  }

  @Override
  public String toString() {
    return "PlayerServant{" +
            "ID='" + ID + '\'' +
            ", accessToken='" + accessToken + '\'' +
            ", name='" + name + '\'' +
            ", groupName='" + groupName + '\'' +
            ", region=" + region +
            ", currentRoom=" + currentRoom +
            ", position='" + position + '\'' +
            ", objectManager=" + objectManager +
            ", storage=" + storage +
            ", authenticationStatus=" + authenticationStatus +
            '}';
  }

}
