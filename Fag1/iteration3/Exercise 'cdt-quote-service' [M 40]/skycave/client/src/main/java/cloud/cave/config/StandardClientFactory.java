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
import cloud.cave.common.ServerConfiguration;
import com.baerbak.cpf.PropertyReaderStrategy;
import frds.broker.ClientRequestHandler;

/**
 * Concrete ClientFactory that uses a property reader to create
 * delegates for the client side. After creation, each service delegate is
 * configured through their 'initialize' method with their service end point
 * configuration, again based upon reading their respective properties.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class StandardClientFactory implements CaveClientFactory {

  private PropertyReaderStrategy propertyReader;

  public StandardClientFactory(PropertyReaderStrategy envReader) {
    propertyReader = envReader;
  }

  @Override
  public ClientRequestHandler createClientRequestHandler() {
    ClientRequestHandler crh = null; 
    crh = Config.loadAndInstantiate(propertyReader,
        Config.SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION, crh);

    // Read in the configuration for (host,port) of server
    ServerConfiguration config =
        new ServerConfiguration(propertyReader, Config.SKYCAVE_APPSERVER);
    String hostname = config.get(0).getHostName();
    int port = config.get(0).getPortNumber();
    crh.setServer(hostname, port);

    return crh;
  }
}
