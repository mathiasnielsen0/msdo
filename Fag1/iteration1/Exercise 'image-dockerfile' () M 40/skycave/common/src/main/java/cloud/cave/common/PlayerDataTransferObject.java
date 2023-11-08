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

import cloud.cave.domain.*;

/**
 * Data Transfer Object for a Player reference: When a CaveProxy tries to
 * log in a player, an instance of this datatype is returned so
 * a local PlayerProxy can be made from the data within.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class PlayerDataTransferObject {

  private final String playerName;
  private final String playerId;
  private final String accessToken;
  private final LoginResult authenticationStatus;
  /**
   * Create a DTO representing a valid login
   *
   * @param playerId id of this player
   * @param accessToken this player's access token
   * @param playerName the name of the player
   * @param authenticationStatus the result of the login
   */
  public PlayerDataTransferObject(String playerId, String accessToken,
                                  String playerName, LoginResult authenticationStatus) {
    this.playerId = playerId;
    this.accessToken = accessToken;
    this.playerName = playerName;
    this.authenticationStatus = authenticationStatus;
  }

  /**
   * Create DTO representing an INVALID login
   * 
   * @param authenticationStatus
   *          the code representing what is the cause of the invalid login.
   */
  public PlayerDataTransferObject(LoginResult authenticationStatus) {
    playerId = accessToken = playerName = null;
    this.authenticationStatus = authenticationStatus;
  }

  public String getPlayerId() {
    return playerId;
  }
  public String getAccessToken() {
    return accessToken;
  }
  public LoginResult getAuthenticationStatus() {
    return authenticationStatus;
  }
  public String getPlayerName() {
    return playerName;
  }

  @Override
  public String toString() {
    return "PlayerDataTransferObject{" +
        "playerId='" + playerId + '\'' +
        ", accessToken='" + accessToken + '\'' +
        ", authenticationStatus=" + authenticationStatus +
        '}';
  }
}
