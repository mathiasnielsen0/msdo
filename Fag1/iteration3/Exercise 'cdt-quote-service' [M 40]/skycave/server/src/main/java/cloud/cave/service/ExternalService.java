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

package cloud.cave.service;

import cloud.cave.invoker.CaveIPCException;
import cloud.cave.config.ObjectManager;
import cloud.cave.common.ServerConfiguration;

/**
 * Interface for external services, i.e. services that are accessed through
 * remote APIs like a database system or a web service.
 * <p>
 * This interface is used both for connection based services (like a database
 * that must be opened at startup and then closed at shutdown) as well as
 * connection-less services (like a web service where the connector is opened,
 * request and reply executed, and then closed).
 * <p>
 * For both types, the 'initialize' method must provide the configuration of the
 * server endpoint(s).
 * <p>
 * For the former, the 'disconnect' method must be invoked to close the
 * connection gracefully; for the latter the 'disconnect' method has no
 * behaviour.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */

public interface ExternalService {

  /**
   * As a service is created dynamically by the StandardServerFactory, we
   * cannot provide the server end point configuration through the constructor.
   * Instead the initialize method must be called (as it is by the
   * StandardServerFactory) with the proper configuration.
   * <p>
   * For connection-oriented services, like databases, the connection is opened
   * as well.
   * 
   * @param objectManager
   *          the object manager of the system
   * @param config
   *          the configuration of the underlying database system.
   * 
   * @throws CaveIPCException
   *           in case of connection or initialization failures
   */
  void initialize(ObjectManager objectManager, ServerConfiguration config);

  /**
   * For a connection oriented service, disconnect the connection to it. For a
   * connection-less service, this method has no behavior.
   * 
   * @throws CaveIPCException
   *           in case of disconnection failures
   */
  void disconnect();

  /**
   * Get the configuration of this service, i.e. the
   * IP end point(s).
   * 
   * @return the server configuration
   */
  ServerConfiguration getConfiguration();
}