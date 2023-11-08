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

package cloud.cave.domain;

import cloud.cave.common.WallMessageDataTransferObject;

import java.util.List;

/**
 * The Player role is a the avatar that does the interaction on behalf of the
 * user in the cave.
 * <p>
 * A player is characterized by his/her
 * <ul>
 * <li>'loginName': the name (which MUST be your student ID in the CloudArch
 * course in order to give you your proper score) used to identify the player to
 * the subscription service; typically only used during login.</li>
 * 
 * <li>'playerName': the name by which the player is known in the cave; this is
 * the name that other players see during the cave exploration experience.</li>
 * 
 * <li>'playerID': a unique ID assigned by the subscription system to identify
 * the player; used throughout the code to identify a player but rarely used in
 * the user interface (similar to battle-tags in Blizzard systems.)</li>
 * </ul>
 * 
 * A player is always associated with specific room in the cave through his/her
 * 'position'.
 * <p>
 * Accessors include methods to view the room that the player is in, see the
 * list of other players in the same room, etc.
 * <p>
 * Some accessors are volatile, that is, two consecutive executions may return
 * different results, as other players may modify the state of the room the
 * player is in.
 * <p>
 * Mutators include methods to move a player in the cave, 'dig' new rooms, and
 * an 'execute' method that may execute command objects installed on the server.
 * The latter allows new behavior to be added at run-time.
 * <p>
 * Class invariants:
 * <p>
 * A player object shall never be instantiated directly but only returned as the
 * result of a successful cave.login().
 * <p>
 * A player object's method shall never be called after the cave.logout()
 * <p>
 * Note: The domain objects of the cave system are Player = the avatar with a
 * specific position in the cave, Cave = the matrix of rooms, and Room = the
 * location with a description, a set of exits, and a list of players currently
 * in this room.
 * <p>
 * However, from the perspective of the player, there is a one-to-one relation
 * to the room which the player is currently in. Therefore the present interface
 * acts as a facade to the room related behavior, i.e.
 * player.getShortRoomDescription() in order to avoid
 * player.getRoom().getShortDescription(). (Which also obeys Law of Demeter / Do
 * not talk to strangers).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */

public interface Player {

  /**
   * Return the authentication status of this player. If
   * LoginResult.isValidLogin(getAuthenticationStatus()) is
   * false, then all other methods' outcome is undefined!
   * @return if this player object is logged in or not.
   */
  LoginResult getAuthenticationStatus();

  /**
   * Return the in-game player name, a synonym under which the player is known
   * in the cave.
   * 
   * @return in-game player name
   */
  String getName();
  
  /**
   * Return the unique ID of this player
   * 
   * @return unique system generated ID of player, assigned by the server side
   *         at registration time.
   */
  String getID();

  /**
   * Get the position of the player in the grid, as a string in format (x,y,z).
   * The entry room is (0,0,0).
   * 
   * @return the coordinates of the player's position as a string (x,y,z)
   */
  String getPosition();

  /**
   * Get a short description of the room this player is currently in.
   * 
   * @return the description of the room the player is in.
   */
  String getShortRoomDescription();

  /**
   * Get the region (real physical region, not virtual in the cave) that this
   * player is subscribed in.
   * 
   * @return the region of the player
   */
  Region getRegion();

  /**
   * Get the access token of the current session this player is involved in. A session
   * lasts from when a login of the player is made until he/she logs out again,
   * which is similar to the OAuth 2 protocol.
   * If a player object is not logged in, the access token is null.
   * 
   * <br>
   * 
   * @return unique access token of this session or null in case no session
   */
  String getAccessToken();

  // === Volatile accessors

  /**
   * Get a list of in-game player names of all players in the same room as this
   * player is in. Of course may change as other players move in/out of
   * player's current room.
   *
   * @return list of player names
   */
  List<String> getPlayersHere();

  /**
   * Get a long description of the current room, which includes a textual list
   * of exits, and textual list of players in this room.
   * Example:
   * <pre>
   You are in the top of a tall tree, at the end of a road.
   Creator: Will Crowther, 10 minutes ago.
   There are exits in directions:
   UP   DOWN
   You see other players:
   [0] Mikkel
   * </pre>
   *
   *
   * @return long description of the room the player is in
   * as a list of strings.
   * */
  List<String> getLongRoomDescription();

  /**
   * Get the set of exits that lead to new rooms from the players current
   * position.
   * 
   * @return set of exits from the room the player is located in
   */
  List<Direction> getExitSet();

  /**
   * Return a specific quote from the quote service, or a random one if
   * index 0 is provided.
   * <br>
   * Example output: <br>
   * <code>
   * The true sign of intelligence is not knowledge but imagination. - Albert Einstein
   * </code>
   *
   * @return a quote and author.
   *
   */
  String getQuote(int quoteIndex);

  /** Default page size of wall messages, that is,
   * the getNumber of messages to retrieve per page.
   */
  public static final int WALL_PAGE_SIZE = 8;

  /**
   * Get the WALL_PAGE_SIZE messages currently on page 'pageNumber' on the
   * wall of the room that this player is located in.
   * The messages are sorted with the newest at location 0,
   * second newest at location 1, etc.
   * Messages are paginated and pageNumber = 0 is the
   * page with the newest WALL_PAGE_SIZE messages.
   *
   * @param pageNumber the page wanted, page 0 is the
   *                  newest
   * @return a list of wall messages, wrapped in a data
   * transfer object
   */
  List<WallMessageDataTransferObject> getMessageList(int pageNumber);

  // === Mutators

  /**
   * Move this player in the given direction.
   * 
   * @param direction
   *          the direction to move
   * @return an update result, is UPDATE_OK if move succeeded, or
   *         FAIL_AS_NOT_FOUND if the room does not exist.
   */
  UpdateResult move(Direction direction);

  /**
   * Create a new room with the given description in the given direction. The
   * player is NOT moved into the room created.
   * 
   * @param direction
   *          geographical direction to 'dig' the room in
   * @param description
   *          the description of the room
   * 
   * @return an update result of the operation
   */
  UpdateResult digRoom(Direction direction, String description);

  /**
   * Update the description of the room the player
   * is currently in with a new description text;
   * no other attributes of the room are changed
   * (including the timestamp). An update only
   * occurs iff the room
   * was originally created by this player.
   *
   * @param newDescription the new description of the
   *                       room
   * @return output of update which may succeed or
   * fail because the player is not the creator of
   * the room
   */

  UpdateResult updateRoom(String newDescription);

  /**
   * Add a message to this room's wall. Each message is added to the list of
   * messages on the wall.
   * 
   * @param message
   *          the message that this player adds to the wall of this room.
   *
   * @throws cloud.cave.common.CaveException in case the underlying persistence
   * layer/database experience an error
   */
  void addMessage(String message);

  /**
   * Update a given wall message with a new text;
   * no other attributes of the message are changed
   * (including the timestamp). An update only
   * occurs iff the message with the given id
   * was originally created by this player.
   *
   * @param messageId unique message Id, provided
   *                  by retrieving msg.getId()
   *                  of the message that must
   *                  be updated
   * @param newContents the new contents of the
   *                   message
   * @return result of the update which may
   * succeed, or fail due to either the message
   * not existing or message was not created by
   * the player.
   */
  UpdateResult updateMessage(String messageId, String newContents);

  /**
   * Execute a command object with the given name.
   * 
   * @param commandName
   *          the name of a Java class implementing the Command interface and
   *          located in the classpath defined by EXTENSION_CLASSPATH.
   *          (This is an application of the Command Pattern, see e.g.
   *          "Flexible, Reliable Software" p. 308, CRC Press 2010.)
   * @param parameters
   *          the (variable number of) parameters for the command; all must be
   *          strings
   * @return The output to print in the Cmd output.
   */
  List<String> execute(String commandName, String... parameters);
}
