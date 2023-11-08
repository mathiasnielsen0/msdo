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

import java.lang.reflect.Type;
import java.util.*;

import cloud.cave.common.Marshalling;
import cloud.cave.common.MarshallingKeys;
import cloud.cave.common.PlayerSessionExpiredException;

import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.domain.*;

import com.google.gson.reflect.TypeToken;
import frds.broker.ClientProxy;
import frds.broker.IPCException;
import frds.broker.Requestor;

import javax.servlet.http.HttpServletResponse;

/**
 * A Proxy (Flexible, Reliable Software, page 317) or more specifically a
 * ClientProxy (Broker pattern / Flexible, Reliable,
 * Distributed Software, on leanpub.com) for the Player role.
 * <p>
 * Some Player methods are implemented locally.
 * <p>
 * Never instantiate a player proxy instance directly, instead you must create a
 * cave proxy, and use its login method to retrieve the player proxy.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class PlayerProxy implements Player, ClientProxy {

  private Requestor requestor;

  private String playerID;
  private String playerName;
  private String accessToken;
  private LoginResult authenticationStatus;

  /**
   * DO NOT USE THIS CONSTRUCTOR DIRECTLY (except in unit tests perhaps). Create
   * the player proxy configured with the relevant request handler and
   * properties.
   *
   * @param requestor            the requestor used to communicate with the server based
   *                             player instance
   * @param authenticationStatus the status of the authentication
   * @param playerID             id of the player
   * @param playerName           name of the player
   * @param accessToken          OAuth Access Token of the authorized player
   */
  PlayerProxy(Requestor requestor, LoginResult authenticationStatus,
              String playerID, String playerName, String accessToken) {
    this.authenticationStatus = authenticationStatus;
    this.playerID = playerID;
    this.playerName = playerName;
    this.accessToken = accessToken;
    Requestor decoratedRequestor =
            new RequestorTranslatingSessionExceptionDecorator(requestor);
    this.requestor = decoratedRequestor;
  }

  @Override
  public String getID() {
    return playerID;
  }

  @Override
  public String getShortRoomDescription() {
    String asString = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY, String.class);
    return asString;
  }

  @Override
  public List<String> getLongRoomDescription() {
    Type listOfStringType = new TypeToken<ArrayList<String>>(){}.getType();
    List<String> asStringList = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_LONG_ROOM_DESCRIPTION_METHOD_KEY, listOfStringType);
    return asStringList;
  }

  @Override
  public LoginResult getAuthenticationStatus() {
    return authenticationStatus;
  }

  @Override
  public String getName() {
    return playerName;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public Region getRegion() {
    Region region = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_REGION_METHOD_KEY, Region.class);
    return region;
  }

  @Override
  public UpdateResult move(Direction direction) {
    UpdateResult isAllowed = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.MOVE_METHOD_KEY, UpdateResult.class, direction);
    return isAllowed;
  }

  @Override
  public UpdateResult digRoom(Direction direction, String description) {
    UpdateResult isAllowed = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.DIG_ROOM_METHOD_KEY, UpdateResult.class, direction, description);
    return isAllowed;
  }

  @Override
  public UpdateResult updateRoom(String newDescription) {
    UpdateResult status = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.UPDATE_ROOM_METHOD_KEY, UpdateResult.class, newDescription);
    return status;
  }

  @Override
  public String getPosition() {
    String positionString =
            requestor.sendRequestAndAwaitReply(getMangledID(),
                    MarshallingKeys.GET_POSITION_METHOD_KEY, String.class);
    return positionString;
  }

  // TODO: Exercise - solve the 'quote-client' exercise
  @Override
  public String getQuote(int quoteIndex) {
    String quoteString =
            requestor.sendRequestAndAwaitReply(getMangledID(),
                    MarshallingKeys.GET_QUOTE_METHOD_KEY, String.class,
                    quoteIndex);
    return quoteString;
  }

  @Override
  public List<Direction> getExitSet() {
    Type listType = new TypeToken<ArrayList<Direction>>(){}.getType();
    List<Direction> exits = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_EXITSET_METHOD_KEY, listType);
    return exits;
  }

  @Override
  public List<String> getPlayersHere() {
    Type listType = new TypeToken<ArrayList<String>>(){}.getType();
    List<String> players = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_PLAYERS_HERE_METHOD_KEY, listType);

    return players;
  }

  // TODO: Exercise - solve the 'wall-client' exercise
  @Override
  public void addMessage(String message) {
    requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.ADD_MESSAGE_METHOD_KEY, null, message);
  }

  // TODO: Exercise - solve the 'wall-client' exercise
  @Override
  public UpdateResult updateMessage(String messageId, String newContents) {
    Object[] merged = new String[2];
    merged[0] = messageId;
    merged[1] = newContents;

    UpdateResult updateResult = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.UPDATE_MESSAGE_METHOD_KEY, UpdateResult.class, merged);

    return updateResult;
  }

  // TODO: Exercise - solve the 'wall-client' exercise
  @Override
  public List<WallMessageDataTransferObject> getMessageList(int pageNumber) {
    Type listType = new TypeToken<ArrayList<WallMessageDataTransferObject>>(){}.getType();

    List<WallMessageDataTransferObject> contents = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.GET_MESSAGE_LIST_METHOD_KEY, listType, pageNumber);

    return contents;
  }

  @Override
  public List<String> execute(String commandName, String... parameters) {
    // Bit tedios, have to merge into one big array to avoid marshalling
    // errors
    Object[] merged = new String[parameters.length+1];
    merged[0] = commandName;
    System.arraycopy(parameters, 0, merged, 1, parameters.length);

    Type listType = new TypeToken<ArrayList<String>>(){}.getType();

    List<String> stringList = requestor.sendRequestAndAwaitReply(getMangledID(),
            MarshallingKeys.EXECUTE_METHOD_KEY, listType, merged);
    return stringList;
  }

  @Override
  public String toString() {
    return "(PlayerClientProxy: "+getID()+"/"+getName()+")";
  }

  public String getMangledID() {
    return Marshalling.manglePlayerIDAndAccessToken(playerID, accessToken);
  }

  /**
   * Decorator on the Requestor, which catches 401 UNAUTHORIZED IPC exceptions
   * from the server side and converts them to local PlayerSessionExpiredExceptions.
   */
  private class RequestorTranslatingSessionExceptionDecorator implements Requestor {
    private final Requestor delegate;

    public RequestorTranslatingSessionExceptionDecorator(Requestor requestor) {
      delegate = requestor;
    }

    @Override
    public <T> T sendRequestAndAwaitReply(String objectId, String operationName,
                                          Type typeOfReturnValue, Object... argument) {
      T returnValue;
      try {
        returnValue = delegate.sendRequestAndAwaitReply(objectId, operationName,
                typeOfReturnValue, argument);
      } catch (IPCException e) {
        if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
          throw new PlayerSessionExpiredException(e.getMessage());
        }
        throw e;
      }
      return returnValue;
    }

    @Override
    public void close() {
      delegate.close();
    }
  }
}
