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

package cloud.cave;

import cloud.cave.config.CaveClientFactory;
import cloud.cave.common.Config;
import cloud.cave.config.StandardClientFactory;
import cloud.cave.doubles.StubPropertyReaderStrategy;
import frds.broker.ClientRequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Validate the ClientFactory's ability to read in the properties and create
 * correctly configured delegates based upon their values.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */

public class TestClientFactoryDynamicCreation {

  StubPropertyReaderStrategy envReader;

  @BeforeEach
  public void setup() {
    envReader = new StubPropertyReaderStrategy();
  }

  @Test
  public void shouldCreateProperClientRequestHandler() {
    envReader.setExpectation(Config.SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION,
            "frds.broker.ipc.socket.SocketClientRequestHandler");
    envReader.setExpectation(Config.SKYCAVE_APPSERVER,
            "skycave.mycompany.com:37128");

    CaveClientFactory factory = new StandardClientFactory(envReader);
    ClientRequestHandler crh = factory.createClientRequestHandler();
    assertThat(crh.toString(), containsString("frds.broker.ipc.socket.SocketClientRequestHandler"));
    assertThat(crh.toString(), containsString("37128"));
  }
}
