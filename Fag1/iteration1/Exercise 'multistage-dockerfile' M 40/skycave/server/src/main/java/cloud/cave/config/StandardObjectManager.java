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

import cloud.cave.invoker.SkyCaveRootInvoker;
import cloud.cave.common.CaveConfigurationNotSetException;
import cloud.cave.domain.Cave;
import cloud.cave.server.*;
import cloud.cave.service.*;
import cloud.cave.service.quote.QuoteService;
import frds.broker.Invoker;
import frds.broker.ServerRequestHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of the ObjectManager role.
 * Uses a factory to read all delegate references and provides
 * access to these.
 * <p>
 * This object manager is a 'fail-fast' manager which tries to
 * create and store all known service connectors and delegates
 * upon construction (= in the constructor) in order to catch
 * any misconfiguration right away.
 * <p>
 * Later in the course we need the object manager to create
 * and store generic service connectors (to micro services
 * defined as part of exercises). These are not known
 * in advance and hence have to be created lazily - that
 * is, they are created upon the first call to 'getServiceConnector()'.
 */
public class StandardObjectManager implements ObjectManager {

  private final Cave caveServant;
  private final SkyCaveRootInvoker serverInvoker;
  private final ServerRequestHandler serverRequestHandler;

  private final CaveStorage storage;
  private final SubscriptionService subscriptionService;
  private final PlayerNameService nameService;
  private final QuoteService quoteService;

  private final CaveServerFactory factory;

  public StandardObjectManager(CaveServerFactory factory) {
    this.factory = factory;

    // Create database connection
    storage = factory.createCaveStorageConnector(this);
    
    // Create connector to subscription service
    subscriptionService = factory
        .createSubscriptionServiceConnector(this);

    // Create connector to quote service
    quoteService = factory.createQuoteServiceConnector(this);

    // Create connector to name service for player
    nameService = factory.createPlayerNameServiceConnector(this);
    
    // Create the server side cave instance
    caveServant = factory.createCaveServant(this);

    // Create the invoker on the server side, and bind it to the cave
    serverInvoker = new SkyCaveRootInvoker(this);
    
    // Create the server side SRH... 
    serverRequestHandler = factory.createServerRequestHandler(this); 
  }

  @Override
  public Cave getCave() {
    return caveServant;
  }

  @Override
  public CaveServerFactory getFactory() {
    return factory;
  }

  @Override
  public ServerRequestHandler getServerRequestHandler() {
    return serverRequestHandler;
  }

  @Override
  public CaveStorage getCaveStorage() {
    return storage;
  }

  @Override
  public SubscriptionService getSubscriptionService() {
    return subscriptionService;
  }

  @Override
  public PlayerNameService getPlayerNameService() {
    return nameService;
  }

  @Override
  public Invoker getInvoker() {
    return serverInvoker;
  }

  @Override
  public QuoteService getQuoteService() {
    return quoteService;
  }

  private Map<String, ExternalService> mapService = new HashMap<>();

  @Override
  public <T> T getServiceConnector(Type serviceInterfaceType, String propertyKeyPrefix)
          throws CaveConfigurationNotSetException {
    ExternalService serviceConnector;
    // Implement Singleton behavior: if no object present, create it.
    serviceConnector = mapService.get(propertyKeyPrefix);

    if (serviceConnector == null) {
      //Create it
      serviceConnector = factory.createServiceConnector(serviceInterfaceType, propertyKeyPrefix, this);
      mapService.put(propertyKeyPrefix, serviceConnector);
    }
    @SuppressWarnings("unchecked")
    T serviceConnectorCastedToT = (T) serviceConnector;
    return serviceConnectorCastedToT;
  }
}
