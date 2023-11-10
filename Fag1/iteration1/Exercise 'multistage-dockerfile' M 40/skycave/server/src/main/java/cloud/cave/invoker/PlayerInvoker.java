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

import java.util.ArrayList;
import java.util.List;

import cloud.cave.common.Marshalling;
import cloud.cave.common.MarshallingKeys;
import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.service.quote.QuoteRecord;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import frds.broker.Invoker;
import frds.broker.ReplyObject;

import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import frds.broker.RequestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/** SubInvoker implementation covering all the methods
 * belonging to calls to Player.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class PlayerInvoker implements Invoker {

  private final Gson gson;
  private final Logger logger;
  private ObjectManager objectManager;

  /**
   * Dispatch on all player method invocations.
   * 
   * @param objectManager the object manager
   */
  public PlayerInvoker(ObjectManager objectManager) {
    this.objectManager = objectManager;
    gson = new Gson();
    logger = LoggerFactory.getLogger(PlayerInvoker.class);
  }

  @Override
  public String handleRequest(String request) {
    ReplyObject reply = null;

    // Do the demarshalling
    RequestObject requestObject =
            gson.fromJson(request, RequestObject.class);

    // Cache the name of called method
    String operationName = requestObject.getOperationName();

    // objectId is a mangling of both the player's playerId and accessToken,
    // so we have do demangle it to get the two parts
    String[] parts = Marshalling.demanglePlayerIDAndAccessToken(requestObject.getObjectId());
    String playerId = parts[0];
    String accessToken = parts[1];

    // Payload is delivered as JsonArray from the server request handler.
    JsonArray array = JsonParser.parseString(requestObject.getPayload()).getAsJsonArray();

    try {
      // Fetch the player object from the name service
      Player player = objectManager.getPlayerNameService().get(playerId);

      // Access control of the 'Blizzard' variant: the last
      // login (= session) is the one winning. If the session id
      // coming from the client differs from the one cached here
      // in the server means two different clients are accessing
      // the same player object. However we assign a new session
      // id upon each login thus if they differ, the client
      // calling us has the 'old session' and must thus be
      // told that he/she cannot control the avatar any more.

      // Thanks to Magnus Torvadal for quickly finding the
      // very special case for the next guard: if second
      // session logs in and out before the first session
      // sends any upcalls to the dispatcher, the player
      // session cache actually have no valid player object!
      // Funny, this bug went undetected through 130 students
      // bashing on this code for 7 weeks during the E15
      // course.

      if (player== null) {
        throw new PlayerSessionExpiredException(
                "PlayerDispatcher: The session for player " + playerId
                        + " is no longer valid (Client session=" + accessToken
                        + "/No server session)");
      }
      // Normal case: session two is still logged in
      // when session 1 does the upcall
      if (!accessToken.equals(player.getAccessToken())) {
        throw new PlayerSessionExpiredException(
                "PlayerDispatcher: The session for player " + playerId
                        + " is no longer valid (Client session=" + accessToken
                        + "/Server cached session=" + player.getAccessToken() + ").");
      }

      // === SHORT ROOM
      if (operationName
              .equals(MarshallingKeys.GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY)) {
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(player.getShortRoomDescription()));
      }
      // === LONG ROOM
      if (operationName
              .equals(MarshallingKeys.GET_LONG_ROOM_DESCRIPTION_METHOD_KEY)) {
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(player.getLongRoomDescription()));
      }
      // === REGION
      else if (operationName.equals(MarshallingKeys.GET_REGION_METHOD_KEY)) {
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(player.getRegion()));
      }
      // === POSITION
      else if (operationName.equals(MarshallingKeys.GET_POSITION_METHOD_KEY)) {
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(player.getPosition()));
      }
      // === MOVE
      else if (operationName.equals(MarshallingKeys.MOVE_METHOD_KEY)) {
        // move(direction)
        String directionAsString = gson.fromJson(array.get(0), String.class);
        Direction direction = Direction.valueOf(directionAsString);
        UpdateResult isValid = player.move(direction);

        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(isValid));
      }
      // === EXIT SET
      else if (operationName.equals(MarshallingKeys.GET_EXITSET_METHOD_KEY)) {
        List<Direction> exitSet = player.getExitSet();
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson(exitSet));
      }
      // === PLAYERS HERE
      else if (operationName.equals(MarshallingKeys.GET_PLAYERS_HERE_METHOD_KEY)) {
        List<String> playersHere = player.getPlayersHere();
        reply = new ReplyObject(HttpServletResponse.SC_OK,
                gson.toJson((playersHere)));
      }
      // === DIG
      else if (operationName.equals(MarshallingKeys.DIG_ROOM_METHOD_KEY)) {
        // dig(direction, description)
        String directionAsString = gson.fromJson(array.get(0), String.class);
        Direction direction = Direction.valueOf(directionAsString);
        String description = gson.fromJson(array.get(1), String.class);
        UpdateResult isValid = player.digRoom(direction, description);

        reply = new ReplyObject(HttpServletResponse.SC_CREATED,
                gson.toJson(isValid));
      }
      // === UPDATE ROOM
      else if (operationName.equals(MarshallingKeys.UPDATE_ROOM_METHOD_KEY)) {
          // update(description)
          String description = gson.fromJson(array.get(0), String.class);
          UpdateResult status = player.updateRoom(description);

          reply = new ReplyObject(HttpServletResponse.SC_OK,
                  gson.toJson(status));
      }
      // === EXECUTE
      else if (operationName.equals(MarshallingKeys.EXECUTE_METHOD_KEY)) {
        String commandName = gson.fromJson(array.get(0), String.class);
        String[] parameters = new String[array.size()];
        for (int i = 1; i < array.size(); i++) {
          parameters[i-1] = gson.fromJson(array.get(i), String.class);
        }

        List<String> stringList = player.execute(commandName, parameters);
        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(stringList));
      }

      // === QUOTE
      else if (operationName.equals(MarshallingKeys.GET_QUOTE_METHOD_KEY)) {
        int quoteIndex = gson.fromJson(array.get(0), int.class);

        String quoteRecord = player.getQuote(quoteIndex);
        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(quoteRecord));
      }

      // === Wall
      else if (operationName.equals(MarshallingKeys.ADD_MESSAGE_METHOD_KEY)) {
        String message = gson.fromJson(array.get(0), String.class);
        player.addMessage(message);
        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson("Message added"));
      }
      else if (operationName.equals(MarshallingKeys.UPDATE_MESSAGE_METHOD_KEY)) {
        String messageId = gson.fromJson(array.get(0), String.class);
        String newContents = gson.fromJson(array.get(1), String.class);

        UpdateResult status =  player.updateMessage(messageId, newContents);
        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(status));
      }
      else if (operationName.equals(MarshallingKeys.GET_MESSAGE_LIST_METHOD_KEY)) {
        int pageNumber = gson.fromJson(array.get(0), int.class);

        List<WallMessageDataTransferObject> wallMessages =  player.getMessageList(pageNumber);

        reply = new ReplyObject(HttpServletResponse.SC_OK, gson.toJson(wallMessages));
      }

      } catch (PlayerSessionExpiredException exc) {
      // Using 401 Unauthorized, as discussions of using 403 Forbidden
      // on stack overflow indicates that using 403 user should not attempt request
      // again, which is our case
      reply = new ReplyObject(HttpServletResponse.SC_UNAUTHORIZED,
              "The session for player with ID "
                      + playerId + " has expired (Multiple logins made)");
    }

    // Added for Humio logging
    logger.info("method=handleRequest, context=reply, status={}", reply.getStatusCode());

    return gson.toJson(reply);
  }
}
