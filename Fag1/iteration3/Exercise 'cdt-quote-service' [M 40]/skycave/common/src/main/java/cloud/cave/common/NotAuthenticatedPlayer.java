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

import java.util.List;

/** A NULL OBJECT player, representing a 'failed login' player.
 * The only valid method call is to 'getAuthenticationStatus().
 *
 */

public class NotAuthenticatedPlayer implements Player {
  private LoginResult authenticationStatus;

  public NotAuthenticatedPlayer(LoginResult authenticationStatus) {
    this.authenticationStatus = authenticationStatus;
  }

  @Override
  public LoginResult getAuthenticationStatus() {
    return authenticationStatus;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getID() {
    return null;
  }

  @Override
  public String getPosition() {
    return null;
  }

  @Override
  public String getShortRoomDescription() {
    return null;
  }

  @Override
  public Region getRegion() {
    return null;
  }

  @Override
  public String getAccessToken() {
    return null;
  }

  @Override
  public List<String> getPlayersHere() {
    return null;
  }

  @Override
  public List<String> getLongRoomDescription() {
    return null;
  }

  @Override
  public List<Direction> getExitSet() {
    return null;
  }

  @Override
  public List<WallMessageDataTransferObject> getMessageList(int pageNumber) {
    return null;
  }

  @Override
  public String getQuote(int quoteIndex) {
    return null;
  }

  @Override
  public UpdateResult move(Direction direction) {
    return UpdateResult.FAIL_AS_NOT_FOUND;
  }

  @Override
  public UpdateResult digRoom(Direction direction, String description) {
    return UpdateResult.UPDATE_OK;
  }

  @Override
  public UpdateResult updateRoom(String newDescription) {
    return UpdateResult.UPDATE_OK;
  }

  @Override
  public void addMessage(String message) {
  }

  @Override
  public UpdateResult updateMessage(String messageId, String newContents) {
    return UpdateResult.UPDATE_OK;
  }

  @Override
  public List<String> execute(String commandName, String... parameters) {
    return null;
  }
}
