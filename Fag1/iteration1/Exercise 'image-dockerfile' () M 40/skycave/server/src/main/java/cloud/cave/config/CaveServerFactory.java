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

import cloud.cave.domain.Cave;
import cloud.cave.domain.LoginResult;
import cloud.cave.domain.Player;
import cloud.cave.server.PlayerNameService;
import cloud.cave.service.*;
import cloud.cave.service.quote.QuoteService;
import frds.broker.ServerRequestHandler;

import java.lang.reflect.Type;

/**
 * Abstract factory (FRS, page 217) interface for creating delegates for the
 * server side cave. For production, use the implementation based
 * upon reading Chained Property Files (CPF).
 *
 * The factory creates objects/delegates at two levels:
 *
 * The ExternalService level: All create methods ends in
 * 'create...ServiceConnector()' and these methods creates the
 * connector/driver to some externally running service, like
 * the subscription service, the quote service, the cave storage
 * database, etc. All returned instances implements the
 * ExternalService interface.
 *
 * The internal level: These are more internal to the SkyCave
 * and these objects do NOT implement ExternalService. Examples
 * are the server request handler for the Broker interaction, as
 * well as creator methods for CaveServant and PlayerServant objects.
 * The latter two are NOT relevant for the first course of the
 * fagpakke but comes in handy in the second course.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface CaveServerFactory {

  /**
   * Create and return an initialized connector to the cave storage, the binding
   * to the database system that holds all data related to the cave: players,
   * rooms, etc.
   * <p>
   * In case of an external storage system (a database connection) the factory
   * will return a fully initialized and open connection.
   * 
   * @param objectManager the system wide manager of delegates
   * 
   * @return a connector to the storage system
   */
  CaveStorage createCaveStorageConnector(ObjectManager objectManager);

  /**
   * Create and return an initialized connector to the subscription service.
   * 
   * @param objectManager the system wide manager of delegates
   * 
   * @return a connector to the subscription service
   */
  SubscriptionService createSubscriptionServiceConnector(ObjectManager objectManager);

  /**
   * Create and return an initialized connector to the quote service.
   *
   * @param objectManager the system wide manager of delegates
   *
   * @return a connector to the quote service
   */
  QuoteService createQuoteServiceConnector(ObjectManager objectManager);

  /**
   * Create and return an initialized player name service connector.
   *
   * @param objectManager
   *          the objectManager that holds all delegates
   *
   * @return the connector to the name service for player objects
   */
  PlayerNameService createPlayerNameServiceConnector(ObjectManager objectManager);

  /**
   * Create and return the server request handler object that binds the server invoker
   * to the particular OS and the IPC system chosen.
   * 
   * @param objectManager
   *          the objectManager that holds all delegates
   * 
   * @return the server request handler
   */
  ServerRequestHandler createServerRequestHandler(ObjectManager objectManager);

  /** Create the cave servant.
   * NOT to be used until Course two of MSDO.
   * @param objectManager
   *        the objectManager that holds all delegates
   *
   * @return a new CaveServant implementation
   */
  Cave createCaveServant(ObjectManager objectManager);

  /** Create a PlayerServant object.
   * NOT to be used until Course two of MSDO.
   *
   * @param theResult the login result enum for the creation
   * @param playerID the ID of the player
   * @param objectManager the associated obj manager
   * @return an newly created player servant object
   */
  Player createPlayerServant(LoginResult theResult, String playerID, ObjectManager objectManager);

  /**
   * Create and return a generic external service, defined
   * by a node name (ala 'localhost:9999') and a local
   * connector java implementation.
   *
   * @param interfaceType the java.lang.Type of the interface
   *                      to create
   * @param propertyKeyPrefix the name of the service in the CPF file.
   * @param objectManager the object manager of skycave
   * @return an initialized service connector, that is,
   * service.initialize(...) has been called with the values
   * defined by propertyKeyPrefix in the CPF file that the factory
   * is equipped with. The returned object both implements
   * ExternalService as well as the provided interfaceType.
   */
  ExternalService createServiceConnector(Type interfaceType, String propertyKeyPrefix, ObjectManager objectManager);
}
