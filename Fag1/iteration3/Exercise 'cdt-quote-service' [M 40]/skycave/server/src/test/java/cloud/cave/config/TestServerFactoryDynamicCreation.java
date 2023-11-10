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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cloud.cave.server.PlayerNameService;
import cloud.cave.service.quote.QuoteService;
import frds.broker.ServerRequestHandler;

import cloud.cave.common.*;
import cloud.cave.doubles.*;
import cloud.cave.common.ServerConfiguration;
import cloud.cave.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Validate the ServerFactory's ability to read in the properties and create
 * correctly configured delegates based upon their values.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestServerFactoryDynamicCreation {
  
  private CaveServerFactory factory;
  private StubPropertyReaderStrategy envReader;

  @BeforeEach
  public void setup() {
    envReader = new StubPropertyReaderStrategy();
    factory = new StandardServerFactory(envReader);
  }

  @Test
  public void shouldCreateProperCaveStorageInstances() {
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.CONNECTOR_SUFFIX,
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.SERVER_ADDRESS_SUFFIX,
        "192.168.237.130:27017");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    assertThat(storage.toString(), containsString("FakeCaveStorage"));
    
    ServerConfiguration config = storage.getConfiguration();
    assertThat(config, is(notNullValue()));
    assertThat(config.get(0).getHostName(), is("192.168.237.130"));
    assertThat(config.get(0).getPortNumber(), is(27017));
  }

  @Test
  public void shouldCreateProperSubscriptionInstances() {
    envReader.setExpectation(Config.SKYCAVE_SUBSCRIPTIONSERVICE + Config.CONNECTOR_SUFFIX,
        "cloud.cave.doubles.TestStubSubscriptionService");
    envReader.setExpectation(Config.SKYCAVE_SUBSCRIPTIONSERVICE + Config.SERVER_ADDRESS_SUFFIX,
        "subscription.baerbak.com:42042");
    SubscriptionService service = factory.createSubscriptionServiceConnector(null);
    assertThat(service.toString(), containsString("TestStubSubscriptionService"));
    ServerConfiguration config = service.getConfiguration();
    assertThat(config, is(notNullValue()));
    assertThat(config.get(0).getHostName(), is("subscription.baerbak.com"));
    assertThat(config.get(0).getPortNumber(), is(42042));
  }

  @Test
  public void shouldCreateProperPlayerNameService() {
    envReader.setExpectation(Config.SKYCAVE_PLAYERNAMESERVICE + Config.CONNECTOR_SUFFIX,
            "cloud.cave.server.InMemoryNameService");
    envReader.setExpectation(Config.SKYCAVE_PLAYERNAMESERVICE + Config.SERVER_ADDRESS_SUFFIX,
            "10.11.82.10:11211,10.11.82.12:11211");

    PlayerNameService nameService = factory.createPlayerNameServiceConnector(null);
    assertThat(nameService.toString(), containsString("InMemoryNameService"));

    ServerConfiguration config = nameService.getConfiguration();
    assertThat(config, is(notNullValue()));
    assertThat(config.get(0).getHostName(), is("10.11.82.10"));
    assertThat(config.get(0).getPortNumber(), is(11211));

    assertThat(config.get(1).getHostName(), is("10.11.82.12"));
    assertThat(config.get(1).getPortNumber(), is(11211));
  }

  @Test
  public void shouldCreateProperServerRequestHandlerInstances() {
    envReader.setExpectation(Config.SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION,
        "frds.broker.ipc.socket.SocketServerRequestHandler");
    envReader.setExpectation(Config.SKYCAVE_APPSERVER,
        "localhost:37126");
    ObjectManager objMgr = new NullObjectManager();
    ServerRequestHandler srh = factory.createServerRequestHandler(objMgr);
    assertThat(srh.toString(), containsString("frds.broker.ipc.socket.SocketServerRequestHandler"));
    assertThat(srh.toString(), containsString("37126"));
  }

  @Test
  public void shouldCreateProperCaveReplicaSet() {
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.CONNECTOR_SUFFIX,
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.SERVER_ADDRESS_SUFFIX,
        "192.168.237.130:27017,192.168.237.131:27018,192.168.237.132:27019");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    assertThat(storage.toString(), containsString("FakeCaveStorage"));
    
    ServerConfiguration config = storage.getConfiguration();
    assertThat(config, is(notNullValue()));
    assertThat(config.get(0).getHostName(), is("192.168.237.130"));
    assertThat(config.get(0).getPortNumber(), is(27017));
    
    assertThat(config.get(1).getHostName(), is("192.168.237.131"));
    assertThat(config.get(1).getPortNumber(), is(27018));
    
    assertThat(config.get(2).getHostName(), is("192.168.237.132"));
    assertThat(config.get(2).getPortNumber(), is(27019));
    
    assertThat(config.size(), is(3));
  }

  @Test
  public void shouldThrowExceptionForNonExistingCaveClass() {
    envReader = new StubPropertyReaderStrategy();
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.CONNECTOR_SUFFIX,
        "cloud.cave.doubles.SuperDuperNonExistingClass");
    factory = new StandardServerFactory(envReader);
    @SuppressWarnings("unused")
    ExternalService storage = null;
    assertThrows(CaveClassNotFoundException.class,
            () -> factory.createCaveStorageConnector(null));
  }

  @Test
  public void shouldThrowExceptionIfEnvVarNotSet() {
    envReader = new StubPropertyReaderStrategy();
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.CONNECTOR_SUFFIX,
        null);
    factory = new StandardServerFactory(envReader);
    @SuppressWarnings("unused")
    ExternalService storage = null;
    assertThrows(CaveConfigurationNotSetException.class,
            () -> factory.createCaveStorageConnector(null));
  }

  @Test
  public void shouldThrowExceptionIfIndexError() {
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.CONNECTOR_SUFFIX,
        "cloud.cave.doubles.FakeCaveStorage");
    envReader.setExpectation(Config.SKYCAVE_CAVESTORAGE + Config.SERVER_ADDRESS_SUFFIX,
        "192.168.237.130:27017,192.168.237.131:27018,192.168.237.132:27019");
    CaveStorage storage = factory.createCaveStorageConnector(null);
    
    ServerConfiguration config = storage.getConfiguration();
    // Only 3 configurations set
    assertThrows(CaveConfigurationNotSetException.class,
            () -> config.get(4));
  }
  
  @Test
  public void shouldCreateProperQuoteInstances() {
    envReader.setExpectation(Config.SKYCAVE_QUOTESERVICE + Config.CONNECTOR_SUFFIX,
            "cloud.cave.doubles.SaboteurQuoteService");
    envReader.setExpectation(Config.SKYCAVE_QUOTESERVICE + Config.SERVER_ADDRESS_SUFFIX,
            "quote.baerbak.com:6777");
    QuoteService service = factory.createQuoteServiceConnector(null);

    assertThat(service, is(notNullValue()));
    assertThat(service.toString(), containsString("SaboteurQuoteService"));
  }

  @Test
  public void shouldIncreaseCoverage() {
    // not really fun except for increasing the amount of green
    // paint in jacoco
    ServerConfiguration cfg = new ServerConfiguration("www.baerbak.com", 12345);
    assertThat(cfg.get(0).getHostName(), is("www.baerbak.com"));
    assertThat(cfg.get(0).getPortNumber(), is(12345));
    
    assertThat(cfg.toString(), containsString("www.baerbak.com:12345"));
  }

}
