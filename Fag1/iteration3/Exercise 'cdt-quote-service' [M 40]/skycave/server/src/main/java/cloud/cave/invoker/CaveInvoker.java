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

package cloud.cave.invoker;

import cloud.cave.common.MarshallingKeys;
import cloud.cave.common.PlayerDataTransferObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import frds.broker.Invoker;
import frds.broker.ReplyObject;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import frds.broker.RequestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * SubInvoker implementation covering the methods provided by Cave.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class CaveInvoker implements Invoker {

  private final Gson gson;
  private ObjectManager objectManager;
  private Logger logger;

  /**
   * Construct the dispatcher on all Cave method invocations.
   * 
   * @param objectManager
   *          the object manager
   */
  public CaveInvoker(ObjectManager objectManager) {
    this.objectManager = objectManager;
    gson = new Gson();
    logger = LoggerFactory.getLogger(CaveInvoker.class);

  }

  @Override
  public String handleRequest(String request) {
    ReplyObject reply = null;
    Cave cave = objectManager.getCave();

    // Do the demarshalling
    RequestObject requestObject = gson.fromJson(request, RequestObject.class);
    String operationName = requestObject.getOperationName();
    JsonArray array = JsonParser.parseString(requestObject.getPayload()).getAsJsonArray();

    // Added for Humio logging
    logger.info("method=handleRequest, context=request, operationName={}", operationName);

    // === LOGIN
    if (operationName.equals(MarshallingKeys.LOGIN_METHOD_KEY)) {
      String loginName = array.get(0).getAsString();
      String password = array.get(1).getAsString();

      // FRDS's method of returning a server created object is
      // to just return the objectId and let the receiving
      // proxy create a client side proxy from it. However,
      // as we want to return also the status of the login
      // (was authentication successfull, we instead return
      // a DataTransferObject which encapslate multiple values:
      // authentication status, session id, etc.
      Player p = cave.login(loginName, password);
      PlayerDataTransferObject dto = null;
      if (LoginResult.isValidLogin(p.getAuthenticationStatus())) {
        // Create the PlayerDataTransferObject that the proxy expects
        dto = new PlayerDataTransferObject(p.getID(),
                p.getAccessToken(),
                p.getName(),
                p.getAuthenticationStatus());
        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(dto));

      } else {
        dto = new PlayerDataTransferObject(p.getAuthenticationStatus());
        reply = new ReplyObject(HttpServletResponse.SC_UNAUTHORIZED,
                gson.toJson(dto));
      }
    }
    // === LOGOUT
    else if (operationName.equals(MarshallingKeys.LOGOUT_METHOD_KEY)) {
      String playerID = array.get(0).getAsString();
      LogoutResult result = cave.logout(playerID);

      reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(result));
    }
    // === DESCRIBE CONFIGURATION
    else if (operationName.equals(MarshallingKeys.DESCRIBE_CONFIGURATION_METHOD_KEY)) {

      String cfg = cave.describeConfiguration();
      reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(cfg));
    }

    // Added for Humio logging
    logger.info("method=handleRequest, context=reply, statusCode={}", reply.getStatusCode());

    // No need for a 'default case' as the returned null value will
    // be caught in the calling invoker.

    return gson.toJson(reply);
  }

}
