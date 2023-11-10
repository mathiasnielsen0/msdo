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

import cloud.cave.common.CaveConfigurationNotSetException;
import cloud.cave.domain.Cave;
import cloud.cave.server.PlayerNameService;
import cloud.cave.service.*;
import cloud.cave.service.quote.QuoteService;
import frds.broker.Invoker;
import frds.broker.ServerRequestHandler;

import java.lang.reflect.Type;

/**
 * The Object Manager is responsible for providing a system wide lookup service
 * for central roles in the system, a 'yellow-pages' or 'dns'. This way only an
 * instance of object manager is needed to be passed along between roles, as all
 * relevant delegates can be accessed by requesting them. Also called 'Registry'
 * or 'Service Locator' by Fowler.
 * <p>
 * Please note that the default implementation, StandardObjectManager,
 * will instantiate all known service types upon construction, please
 * consult the javadoc for it.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 */
public interface ObjectManager {

  /** Return the cave instance.
   * 
   * @return the cave
   */
  Cave getCave();

  /** Return the factory associated with this object manager
   *
   * @return the abstract factory associated with this
   * object manager
   */
  CaveServerFactory getFactory();

  /** Return the server request handler
   * 
   * @return server request handler
   */
  ServerRequestHandler getServerRequestHandler();

  /** Return the cave storage / the database connector
   * 
   * @return cave storage
   */
  CaveStorage getCaveStorage();

  /** Return the subscription service connector
   * 
   * @return subscription service
   */
  SubscriptionService getSubscriptionService();

  /** Return the name service for players
   * 
   * @return name service of players
   */
  PlayerNameService getPlayerNameService();

  /** Return the invoker on the server side
   * 
   * @return invoker
   */
  Invoker getInvoker();

  /** Return the quote service connector
   *
   * @return quote service
   */
  QuoteService getQuoteService();

  /**
   * Return a service connector for the
   * provided type of service (which MUST
   * also implement ExternalService). If
   * the service connector is not yet
   * created it will be by calling the
   * associated factory.
   *
   * @param serviceInterfaceType The type of the service
   * @param propertyKeyPrefix    the CPF property key used for the factory
   *                             to lookup the server address and
   *                             implementation file
   * @param <T>                  the actual type of service connector; the
   *                             returned instance implements both T and ExternalService
   * @return a newly created service connector the first time and
   * after that the same instance (singleton behavior).
   * @throws CaveConfigurationNotSetException in case the propertyKeyPrefix
   *                                          defines server address and connector implementation keys which
   *                                          have not counterpart in the property/CPF reader associated with the
   *                                          factory
   */
  <T> T getServiceConnector(Type serviceInterfaceType, String propertyKeyPrefix)
          throws CaveConfigurationNotSetException;
}
