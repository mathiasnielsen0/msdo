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

package cloud.cave.client;

import cloud.cave.common.MarshallingKeys;
import cloud.cave.common.PlayerDataTransferObject;

import cloud.cave.common.NotAuthenticatedPlayer;
import cloud.cave.domain.Cave;
import cloud.cave.domain.LoginResult;
import cloud.cave.domain.LogoutResult;
import cloud.cave.domain.Player;

import frds.broker.ClientProxy;
import frds.broker.IPCException;
import frds.broker.Requestor;

import javax.servlet.http.HttpServletResponse;

/** The ClientProxy implementation of the Cave.
 * <p>
 * It is a Proxy pattern (Flexible, Reliable Software, p. 317), 
 * more specifically a ClientProxy (Flexible, Reliable, Distributed Software,
 * find it on leanpub.com) acting as a remote proxy for
 * calls to the servant object on the remote server.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class CaveProxy implements Cave, ClientProxy {

  private Requestor requestor;

  /** Create the cave proxy with the given request handler.
   *
   * @param requestor the Requestor delegate to use
   */
  public CaveProxy(Requestor requestor) {
    this.requestor = requestor;
  }

  @Override
  public Player login(String loginName, String password) {
    PlayerDataTransferObject dto = null;
    Player player = null;

    try {
      dto = requestor.sendRequestAndAwaitReply("ignore-player-id",
              MarshallingKeys.LOGIN_METHOD_KEY, PlayerDataTransferObject.class,
              loginName, password);
      // Now we have a data transfer object representing the player,
      // and know it is valid, as otherwise an exception is thrown.
      // create the client side proxy for it.
      player = new PlayerProxy(requestor, dto.getAuthenticationStatus(),
              dto.getPlayerId(), dto.getPlayerName(), dto.getAccessToken());

    } catch (IPCException exc) {
      // A HTTP UNAUTHORIZED status code is used to signal
      // rejected login, test if this is the case
      if (exc.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
        player = new NotAuthenticatedPlayer(LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION);
      } else
        throw exc; // Rethrow, some other exception occured.
    }
    return player;
  }

  @Override
  public LogoutResult logout(String playerID) {
    LogoutResult result = requestor.sendRequestAndAwaitReply(playerID,
        MarshallingKeys.LOGOUT_METHOD_KEY, LogoutResult.class,
        playerID);
    return result;
  }

  @Override
  public String describeConfiguration() {
    String cfg = "CaveProxy configuration: " + this.getClass().getCanonicalName() + "\n";

    String serverCfg = requestor.sendRequestAndAwaitReply("ignore-player-id",
        MarshallingKeys.DESCRIBE_CONFIGURATION_METHOD_KEY, String.class);

    return cfg + serverCfg;
  }

}
