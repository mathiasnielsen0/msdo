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
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.server.PlayerNameService;
import cloud.cave.service.*;
import cloud.cave.service.quote.QuoteService;
import frds.broker.Invoker;
import frds.broker.ServerRequestHandler;

import java.lang.reflect.Type;

/** Null Object implementation, used in a few test cases.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class NullObjectManager implements ObjectManager {

  @Override
  public Cave getCave() {
    return null;
  }

  @Override
  public CaveServerFactory getFactory() {
    return null;
  }

  @Override
  public ServerRequestHandler getServerRequestHandler() {
    return null;
  }

  @Override
  public CaveStorage getCaveStorage() {
    return null;
  }

  @Override
  public SubscriptionService getSubscriptionService() {
    return null;
  }

  @Override
  public PlayerNameService getPlayerNameService() {
    return null;
  }

  @Override
  public Invoker getInvoker() {
    return null;
  }

  @Override
  public QuoteService getQuoteService() {
    return null;
  }

  @Override
  public <T> T getServiceConnector(Type serviceInterfaceType, String propertyKeyPrefix) throws CaveConfigurationNotSetException {
    return null;
  }

}
