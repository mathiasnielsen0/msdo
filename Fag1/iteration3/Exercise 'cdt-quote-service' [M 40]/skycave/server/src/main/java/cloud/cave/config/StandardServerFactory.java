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

package cloud.cave.config;

import cloud.cave.common.Config;
import cloud.cave.domain.Cave;
import cloud.cave.domain.LoginResult;
import cloud.cave.domain.Player;
import cloud.cave.server.CaveServant;
import cloud.cave.server.PlayerNameService;
import cloud.cave.server.PlayerServant;
import cloud.cave.service.quote.QuoteService;
import com.baerbak.cpf.PropertyReaderStrategy;
import frds.broker.ServerRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.cave.common.ServerConfiguration;
import cloud.cave.service.*;

import java.lang.reflect.Type;

/**
 * Concrete ServerFactory that creates server side delegates based upon dynamic
 * class loading of classes whose qualified names are defined by a set of
 * properties. After creation, each service delegate is configured through their
 * 'initialize' method with their service end point configuration, again based
 * upon reading their respective properties.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 * 
 */
public class StandardServerFactory implements CaveServerFactory {

  private Logger logger;
  private PropertyReaderStrategy propertyReader;

  /**
   * Construct a new server factory, which creates delegates
   * by reading properties from the given reader strategy.
   * 
   * @param envReader
   *          the reader strategy for setting properties
   */
  public StandardServerFactory(PropertyReaderStrategy envReader) {
    logger = LoggerFactory.getLogger(StandardServerFactory.class);
    this.propertyReader = envReader;
  }

  @Override
  public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
    CaveStorage caveStorage = null;
    caveStorage = (CaveStorage)
            createServiceConnector(CaveStorage.class, Config.SKYCAVE_CAVESTORAGE, objMgr);
    return caveStorage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
    SubscriptionService subscriptionService = null;
    subscriptionService = (SubscriptionService)
            createServiceConnector(SubscriptionService.class, Config.SKYCAVE_SUBSCRIPTIONSERVICE, objMgr);
    return subscriptionService;
  }

  @Override
  public QuoteService createQuoteServiceConnector(ObjectManager objMgr) {
    QuoteService quoteService = null;
    quoteService =
            (QuoteService) createServiceConnector(QuoteService.class,
                    Config.SKYCAVE_QUOTESERVICE, objMgr);
    return quoteService;
  }

  @Override
  public PlayerNameService createPlayerNameServiceConnector(ObjectManager objMgr) {
    PlayerNameService nameService = null;
    nameService =
            (PlayerNameService) createServiceConnector(PlayerNameService.class,
                    Config.SKYCAVE_PLAYERNAMESERVICE, objMgr);
    return nameService;
  }

  @Override
  public ServerRequestHandler createServerRequestHandler(ObjectManager objMgr) {
    ServerRequestHandler srh = null; 
    srh = Config.loadAndInstantiate(propertyReader, 
        Config.SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION, srh);

    // Read in the server configuration, only port getNumber relevant
    ServerConfiguration config = 
        new ServerConfiguration(propertyReader, Config.SKYCAVE_APPSERVER);
    int port = config.get(0).getPortNumber();
    srh.setPortAndInvoker(port, objMgr.getInvoker());

    logger.info("method=createServerRequestHandler, implementationClass="
            + srh.getClass().getName()
            + ", serverAddress=" + config);

    return srh;
  }

  @Override
  public Cave createCaveServant(ObjectManager objectManager) {
    logger.info("method=createCaveServant, implementationClass=CaveServant");
    return new CaveServant(objectManager);
  }

  @Override
  public Player createPlayerServant(LoginResult theResult, String playerID, ObjectManager objectManager) {
    logger.info("method=createPlayerServant. implementationClass=PlayerServant");
    return new PlayerServant(theResult, playerID, objectManager);
  }

  @Override
  public ExternalService createServiceConnector(Type interfaceType, String propertyKeyPrefix, ObjectManager objMgr) {
    ExternalService serviceConnector = null;
    serviceConnector = Config.loadAndInstantiate(propertyReader,
            propertyKeyPrefix +Config.CONNECTOR_SUFFIX, serviceConnector);

    // Read in the serviceConnector configuration
    ServerConfiguration config =
            new ServerConfiguration(propertyReader, propertyKeyPrefix +Config.SERVER_ADDRESS_SUFFIX);
    serviceConnector.initialize(objMgr, config);

    logger.info("method=createServiceConnector, type="
            + interfaceType.getTypeName()
            + ", connectorImplementation="
            + serviceConnector.getClass().getName()
            + ", serverAddress=" + config);

    return serviceConnector;
  }

}
