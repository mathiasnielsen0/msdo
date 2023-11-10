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

package cloud.cave.server.common;

import cloud.cave.domain.Region;

import javax.servlet.http.HttpServletResponse;

/**
 * Record type / PODO representing the subscription for a player. This
 * encapsulates the basic data that is transferred from the SubscriptionService
 * for authenticating a player. Besides the data concerning the player,
 * it also returns the access token (OAuth 2 access token, that
 * is issued by the Authorization Server / SubscriptionServer) as
 * well as the error code of the authorization.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class SubscriptionRecord {

  private String playerName;
  private String playerID;
  private String groupName;
  private Region region;

  private String accessToken;
  private int httpStatusCode;
  
  /**
   * Construct a subscription match result that failed validation.
   * 
   * @param httpStatusCode
   *          the code of the error describing the failed subscription
   */
  public SubscriptionRecord(int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }
  
  /** Construct a valid subscription object.
   * 
   * @param playerID id of player
   * @param playerName name of player
   * @param groupName name of group
   * @param region region of player
   */
  public SubscriptionRecord(String playerID, String playerName, String groupName, Region region) {
    this.playerName = playerName;
    this.playerID = playerID;
    this.groupName = groupName;
    this.region = region;
    this.accessToken = "void";
    this.httpStatusCode = HttpServletResponse.SC_OK;
  }
  
  public String getPlayerName() {
    return playerName;
  }

  public String getPlayerID() {
    return playerID;
  }

  public Region getRegion() {
    return region;
  }

  /** The returned code is a HTTP status code, defining
   * the result of authorization, either 200 OK or 401 NOT_AUTHORIZED
   * @return the HTTP status code of a authorization request
   */
  public int getStatusCode() {
    return httpStatusCode;
  }

  @Override
  public String toString() {
    return "SubscriptionRecord{" +
            "playerName='" + playerName + '\'' +
            ", playerID='" + playerID + '\'' +
            ", groupName='" + groupName + '\'' +
            ", region=" + region +
            ", accessToken='" + accessToken + '\'' +
            ", errorCode=" + httpStatusCode +
            '}';
  }

  public String getGroupName() {
    return groupName;
  }

  public String getAccessToken() {
    return accessToken;
  }

  /** Set the access token; however do not do that manually,
   * only to be used by the login procedure.
   * @param accessToken the access token from the subscription
   *                    service
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

}
