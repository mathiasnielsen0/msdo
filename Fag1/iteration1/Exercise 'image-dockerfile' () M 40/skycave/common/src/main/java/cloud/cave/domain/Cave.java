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

/**
 * The Cave role defines the context for the user experience. A cave consists of
 * a 3 dimensional matrix/lattice of 'rooms'. A room has a textual description
 * and can be visited by any number of 'players'. In addition a room has a single
 * wall on which players can post messages as well as read all messages posted.
 * <p>
 * The main responsibility of the Cave is to log-in and log-out any registered
 * player to start/stop the (collaborative) cave exploration experience.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Cave {

  /**
   * Try to login a user with the given loginName and password into the cave.
   * A player object is returned, but if authentication failed, the only
   * valid method call on it is 'getAuthenticationStatus()'; if auth
   * succeeds then it is a complete player object.
   *
   * NOTE: Use LoginResult.isLoginValid(p.getAuthenticationStatus()) to
   * verify if login was proper.
   * 
   * @param loginName
   *          the name used for login
   * @param password
   *          the password
   * @return the result of the login, and if 'getAuthenticationStatus()'
   * is a valid login, then the player object is usable.
   */
  Player login(String loginName, String password);

  /**
   * Logout a player.
   * 
   * @param playerID
   *          id of the player
   * @return the result of the logout operation.
   */
  LogoutResult logout(String playerID);

  /**
   * Return a string containing the configuration of all internally used
   * delegates.
   * 
   * @return description of internal configuration of delegates
   */
  String describeConfiguration();
}
