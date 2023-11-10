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

/** The various types of results of logging in.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public enum LoginResult {
  LOGIN_SUCCESS, // The login was successful
  LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN, // The login was conditionally successful, this is a warning that the player is already logged in

  LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, // The login failed, as the player with given id has no subscription, or credentials are wrong
  LOGIN_FAILED_SERVER_ERROR; // The login failed due to some error on the server side, review server logs

  /** Return true in case the login result represents a valid
   * login
   * @param loginResult one of the login result enums
   * @return true if the code represents a valid login
   */
  public static boolean isValidLogin(LoginResult loginResult) {
    boolean isValidLogin =
        loginResult == LoginResult.LOGIN_SUCCESS ||
        loginResult == LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN;

    return isValidLogin;
  } 
}
