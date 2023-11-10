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

package cloud.cave.doubles;

import cloud.cave.common.CaveConfigurationNotSetException;
import cloud.cave.common.Config;
import cloud.cave.config.*;
import cloud.cave.domain.Cave;
import cloud.cave.domain.LoginResult;
import cloud.cave.domain.Player;
import cloud.cave.server.CaveServant;
import cloud.cave.server.InMemoryNameService;
import cloud.cave.server.PlayerNameService;
import cloud.cave.server.PlayerServant;
import cloud.cave.service.*;
import cloud.cave.service.quote.QuoteService;
import frds.broker.ServerRequestHandler;

import java.lang.reflect.Type;

/**
 * Concrete factory specialized for testing purposes - most
 * service connectors are replaced with test doubles:
 * Storage is a FakeObject, Subscription is a test stub
 * (with Mikkel, Magnus, and Mathilde as subscribers),
 * Quote is a test stub (with two quotes only).
 * The PlayerNameService is a functionally correct
 * in memory implementation.
 * <p>
 * Other create methods return valid implementations for
 * CaveServant, PlayerServant.
 * <p>
 *   The generic createServiceConnector() handles a
 *   request to create a QuoteService, to support
 *   testing of StandardObjectManager's generic
 *   service connector feature.
 * <p>
 * The rest return either null or null implementations as they
 * are not used in the test suites.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class AllTestDoubleFactory implements CaveServerFactory {

  @Override
  public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
    CaveStorage storage = new FakeCaveStorage();
    storage.initialize(objMgr, null); // the fake storage needs no external delegates
    return storage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
    SubscriptionService service = new TestStubSubscriptionService();
    service.initialize(objMgr, null); // no config object required for the stub
    return service;
  }

  @Override
  public QuoteService createQuoteServiceConnector(ObjectManager objectManager) {
    // TODO: I advice permanently changing the returned service once you
    // solve the 'quote-double' exercise
    QuoteService service = new SaboteurQuoteService();
    service.initialize(objectManager, null);
    return service;
  }

  @Override
  public PlayerNameService createPlayerNameServiceConnector(ObjectManager objMgr) {
    PlayerNameService nameService = new InMemoryNameService();
    nameService.initialize(objMgr, null); // no config object required
    return nameService;
  }

  @Override
  public ServerRequestHandler createServerRequestHandler(ObjectManager objMgr) {
    // The SRH is not presently used in the test cases...
    return new NullServerRequestHandler();
  }

  @Override
  public Cave createCaveServant(ObjectManager objectManager) {
    return new CaveServant(objectManager);
  }

  @Override
  public Player createPlayerServant(LoginResult theResult, String playerID, ObjectManager objectManager) {
    return new PlayerServant(theResult, playerID, objectManager);
  }

  @Override
  public ExternalService createServiceConnector(Type interfaceType, String propertyKeyPrefix, ObjectManager objectManager) {
    throw new CaveConfigurationNotSetException("The AllTestDouble factory does not know property key: "
            + propertyKeyPrefix);
  }
}
