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

package cloud.cave.common;

/**
 * Define the string constants that define the 'operationName' for each
 * method for each class involved in the Broker pattern: The Cave and the
 * Player roles of SkyCave.
 * <p>
 * Note: a convention is made for method name keys, namely that all method names
 * are prefixed with a string indicating the type/class of the servant object
 * ('player-' = player; 'cave-' = cave), to allow easy lookup of the proper
 * event handler (called Dispatcher here) by the Invoker. This is essentially an
 * example of 'name mangling'.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class MarshallingKeys {

  // Prefixes of the types/classes that have methods associated
  // Note that the Invoker depends upon these ending in a dash
  // so do not change that.
  public static final String CAVE_TYPE_PREFIX = "cave-";
  public static final String PLAYER_TYPE_PREFIX = "player-";
  
  // List of player method keys
  public static final String MOVE_METHOD_KEY = PLAYER_TYPE_PREFIX + "move";
  public static final String GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-short-room-description";
  public static final String GET_LONG_ROOM_DESCRIPTION_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-long-room-description";
  public static final String GET_POSITION_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-position";
  public static final String GET_REGION_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-region";
  public static final String GET_PLAYERS_HERE_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-players-here";
  public static final String GET_EXITSET_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-exit-set";
  public static final String DIG_ROOM_METHOD_KEY = PLAYER_TYPE_PREFIX + "dig-room";
  public static final String UPDATE_ROOM_METHOD_KEY = PLAYER_TYPE_PREFIX + "update-room";
  public static final String EXECUTE_METHOD_KEY = PLAYER_TYPE_PREFIX + "execute";

  public static final String GET_QUOTE_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-quote";

  public static final String ADD_MESSAGE_METHOD_KEY = PLAYER_TYPE_PREFIX + "add-message";
  public static final String UPDATE_MESSAGE_METHOD_KEY = PLAYER_TYPE_PREFIX + "update-message";
  public static final String GET_MESSAGE_LIST_METHOD_KEY = PLAYER_TYPE_PREFIX + "get-message-list";

  // List of cave method keys
  public static final String LOGIN_METHOD_KEY = CAVE_TYPE_PREFIX + "login";
  public static final String LOGOUT_METHOD_KEY = CAVE_TYPE_PREFIX + "logout";
  public static final String DESCRIBE_CONFIGURATION_METHOD_KEY = CAVE_TYPE_PREFIX + "describe-configuration";
}
