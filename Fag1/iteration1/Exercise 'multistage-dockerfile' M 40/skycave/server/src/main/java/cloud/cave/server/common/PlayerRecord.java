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

/**
 * This is a record type (struct / PODO (Plain Old Data Object)) representing
 * the core data of a player like name, id, position, etc.
 * <p>
 * A record is a pure data object without any behavior, suitable for
 * networking and persistence as it only contains data.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class PlayerRecord {
  private String playerID;
  private String playerName;
  private String groupName; 
  private Region region;
  
  private String positionAsString;
  private String accessToken;

  public PlayerRecord(SubscriptionRecord subscription, 
      String positionString) {
    super();
    this.playerID = subscription.getPlayerID();
    this.playerName = subscription.getPlayerName();
    this.groupName = subscription.getGroupName();
    this.region = subscription.getRegion();
    this.positionAsString = positionString;
    this.accessToken = subscription.getAccessToken();
  }

  public String getPlayerID() {
    return playerID;
  }
  public String getPlayerName() {
    return playerName;
  }
  public String getGroupName() {
    return groupName;
  }
  public String getPositionAsString() {
    return positionAsString;
  }
  public Region getRegion() {
    return region;
  }
  /**
   * get the player's access token from the OAuth
   * authorization; if it is null
   * then the player is not presently in the cave
   * @return the access token or null in case
   * no session exists for the given player
   */
  public String getAccessToken() {
    return accessToken;
  }
  public boolean isInCave() {
    return accessToken != null;
  }

  public void setPositionAsString(String positionAsString) {
    this.positionAsString = positionAsString;
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((playerID == null) ? 0 : playerID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PlayerRecord other = (PlayerRecord) obj;
    if (playerID == null) {
      if (other.playerID != null)
        return false;
    } else if (!playerID.equals(other.playerID))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PlayerRecord [playerID=" + playerID + ", playerName=" + playerName
        + ", groupName=" + groupName + ", region=" + region
        + ", positionAsString=" + positionAsString + ", accessToken=" + accessToken
        + "]";
  }
}
