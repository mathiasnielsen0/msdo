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

package cloud.cave.server;

import cloud.cave.invoker.CaveIPCException;
import cloud.cave.service.quote.QuoteService;
import frds.broker.Servant;
import org.slf4j.*;

import cloud.cave.common.*;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

import javax.servlet.http.HttpServletResponse;

/**
 * The servant implementation of the Cave (Servant role in Broker). Just as the servant player, this
 * implementation communicates directly with the storage layer to achieve its
 * behavior.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class CaveServant implements Cave, Servant {

  private ObjectManager objectManager;
  
  private Logger logger;

  /**
   * Construct the Cave servant object with the delegates/dependencies given by
   * the object manager.
   * 
   * @param objectManager
   *          object manager holding all delegates to collaborate with
   */
  public CaveServant(ObjectManager objectManager) {
    this.objectManager = objectManager;
    logger = LoggerFactory.getLogger(CaveServant.class);
  }

  @Override
  public Player login(String loginName, String password) {
    SubscriptionService subscriptionService = objectManager.getSubscriptionService();

    // Fetch the subscription for the given loginName
    SubscriptionRecord subscription = null;
    String errorMsg = null;
    try {
      subscription = subscriptionService.authorize(loginName, password);
    } catch (CaveIPCException e) {
      errorMsg="Lookup failed on subscription service due to IPC exception:"+e.getMessage();
      logger.error(errorMsg);
    }

    if (subscription == null)
      return new NotAuthenticatedPlayer(LoginResult.LOGIN_FAILED_SERVER_ERROR);

    if (subscription.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      return new NotAuthenticatedPlayer(LoginResult.LOGIN_FAILED_SERVER_ERROR);

    // Check all the error conditions and 'fail fast' on them...
    if (subscription.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
      return new NotAuthenticatedPlayer(LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION);
    }

    // Now the subscription is assumed to be a valid player

    // Enter the player, creating the player's session in the cave
    // (which may overwrite an already ongoing session which is then
    // implicitly invalidated).
    LoginResult theResult = createSessionAndUpdatePlayerInCaveStorage(subscription);

    boolean validLogin = LoginResult.isValidLogin(theResult);
    if ( ! validLogin ) {
      return new NotAuthenticatedPlayer(theResult);
    }

    // Create player domain object. To ensure dependency injection,
    // we have to let the factory create it
    CaveServerFactory factory = objectManager.getFactory();

    Player player = factory.createPlayerServant(theResult, subscription.getPlayerID(), objectManager);
    // new PlayerServant(theResult, subscription.getPlayerID(), objectManager);

    // Enter the player object reference into the name service
    objectManager.getPlayerNameService().add(player.getID(), player);

    return player;
  }

  /** Initialize a player session by updating/preparing the storage system
   * and potentially clear the cache of previous sessions.
   * @param subscription the record of the subscription to start a session on
   * @return result of the login which is always a valid login, but
   * may signal a 'second login' that overrules a previous one.
   */
  private LoginResult createSessionAndUpdatePlayerInCaveStorage(SubscriptionRecord subscription) {
    LoginResult result = LoginResult.LOGIN_SUCCESS; // Assume success

    CaveStorage storage = objectManager.getCaveStorage();
    
    // get the record of the player from storage
    PlayerRecord playerRecord = storage.getPlayerByID(subscription.getPlayerID());
    
    if (playerRecord == null) {
      // Apparently a newly registered player, so create the record
      // and add it to the cave storage
      String position = new Point3(0, 0, 0).getPositionString();
      playerRecord = new PlayerRecord(subscription, position);
    } else {
      // Player has been seen before; if he/she has an existing
      // accessToken ("= is in cave") we flag this as a warning,
      // and clear the cache entry
      if (playerRecord.isInCave()) {
        result = LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN;
      }
      // update the access token in the storage system
      playerRecord.setAccessToken(subscription.getAccessToken());
    }
    storage.updatePlayerRecord(playerRecord);

    return result;
  }

  @Override
  public LogoutResult logout(String playerID) {
    CaveStorage storage = objectManager.getCaveStorage();
    
    // ensure that the player is known by and in the cave
    PlayerRecord player = storage.getPlayerByID(playerID);
    
    if (!player.isInCave()) {
      return LogoutResult.PLAYER_NOT_IN_CAVE;
    }

    // reset the session  to indicate the player is no longer around
    player.setAccessToken(null);

    // and update the record in the storage
    storage.updatePlayerRecord(player);

    // and clear the name service
    objectManager.getPlayerNameService().remove(playerID);
    
    return LogoutResult.SUCCESS;
  }

  @Override
  public String describeConfiguration() {
    CaveStorage storage = objectManager.getCaveStorage();
    SubscriptionService subscriptionService = objectManager.getSubscriptionService();
    PlayerNameService playerNameService = objectManager.getPlayerNameService();
    QuoteService quoteService = objectManager.getQuoteService();

    String cfg = "CaveServant configuration:\n";
    cfg += "  CaveStorage: " + storage.getClass().getName() + "\n";
    cfg += "   - cfg: " + storage.getConfiguration() + "\n";
    cfg += "  SubscriptionService: "+ subscriptionService.getClass().getName() + "\n";
    cfg += "   - cfg: " + subscriptionService.getConfiguration() + "\n";
    cfg += "  PlayerNameService: "+ playerNameService.getClass().getName() + "\n";
    cfg += "   - cfg: " + playerNameService.getConfiguration() + "\n";
    cfg += "  QuoteService: "+ quoteService.getClass().getName() + "\n";
    cfg += "   - cfg: " + quoteService.getConfiguration() + "\n";
    return cfg;
  }
}
