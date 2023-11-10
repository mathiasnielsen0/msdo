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

package cloud.cave.userinterface;

import cloud.cave.domain.*;
import cloud.cave.invoker.CaveIPCException;
import cloud.cave.client.CaveProxy;
import cloud.cave.client.PlayerProxy;
import cloud.cave.common.*;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.doubles.SaboteurCRHDecorator;
import cloud.cave.doubles.TestConstants;
import cloud.cave.server.CaveServant;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;
import frds.broker.ClientRequestHandler;
import frds.broker.IPCException;
import frds.broker.Invoker;
import frds.broker.Requestor;
import frds.broker.marshall.json.StandardJSONRequestor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testing unhappy paths, ie. scenarios where there are network problems,
 * malformed requests, etc. It is an incomplete suite but does
 * show how test double saboteurs can be used in various places
 * in the architecture to create 'safe failure modes' (Nygard)
 * in the face of individual service failures.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class TestUnhappyPath {

  private PlayerProxy player;
  private Cave cave;
  private Requestor requestor;
  private SaboteurCRHDecorator saboteur;

  @BeforeEach
  public void setUp() throws Exception {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

    // And create the client tier's role implementations
    Invoker invoker = objMgr.getInvoker();

    ClientRequestHandler properCrh = new LocalMethodCallClientRequestHandler(invoker);

    // Decorate the proper CRH with one that simulate errors, i.e. a Saboteur
    saboteur = new SaboteurCRHDecorator(properCrh);
    requestor = new StandardJSONRequestor(saboteur);

    // Create the cave proxy
    cave = new CaveProxy(requestor);
    player = (PlayerProxy) HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  /** Try to make an ill formed request (not using the proxy)
   * and ensure that the server invoker makes an
   * appropriate reply.
   * @throws IOException
   */
  @Test
  public void shouldReplyWithErrorInCaseRequestIsMalformed() throws CaveIPCException {
    String asString;

    try {
      asString = requestor.sendRequestAndAwaitReply(player.getMangledID(),
              "unknown-method-key", String.class);
      fail("A request with unknown method key must throw an exception in the FRDS.Broker library");
    } catch(IPCException exc) {
      assertThat(exc.getMessage(), containsString("Unhandled request"));
    }
  }

  @Test
  public void shouldThrowIPCExceptionForTimeOut() {
    // One player
    player = (PlayerProxy) cave.login( TestConstants.MIKKEL_AARSKORT, TestConstants.MIKKEL_PASSWORD);

    assertThat(player.getName(), is("Mikkel"));
    
    UpdateResult isValid = player.move(Direction.NORTH);
    assertThat(isValid, is(UpdateResult.UPDATE_OK));
    
    // Tell the saboteur to throw exception on next IPC
    saboteur.throwNextTime("Could Not Connect to server");
    
    // And check that the exception is propagated all the way
    // to the top level so a client user interface can do
    // something that makes sense to the user...
    try {
      isValid = player.move(Direction.SOUTH);
      fail("player.move should have thrown an CaveIPCException, but did not.");
    } catch (CaveIPCException e) {
      // Correct...
    }
  }

  // Make the daemon server unstable internally
  @Test
  public void shouldReportOnTimeoutErrorOnSubscriptionService() {

    CaveServerFactory factory = new AllTestDoubleFactory() {
    
      public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {

        // Heavy setup to introduce IPC errors on the server side using
        // a Meszaros Saboteur
        SubscriptionService saboteurSubscriptionService = new SubscriptionService() {
          @Override
          public SubscriptionRecord authorize(String loginName, String password) {
            throw new CaveIPCException("SubscriptionService: Timeout in connecting to the service", null);
          }
          @Override
          public void initialize(ObjectManager objMgr, ServerConfiguration config) {
          }
          @Override
          public ServerConfiguration getConfiguration() {
            return null;
          }
          @Override
          public void disconnect() {
          }
        };
    
        return saboteurSubscriptionService;
      }
    };
    ObjectManager objMgr = new StandardObjectManager(factory);
    cave = new CaveServant(objMgr);
    
    // Try out the login, will result in a internal server error as
    // the connection to the subscription fails
    Player p2 = cave.login( TestConstants.MATHILDE_AARSKORT, TestConstants.MATHILDE_PASSWORD);
    assertThat(p2.getAuthenticationStatus(), is(LoginResult.LOGIN_FAILED_SERVER_ERROR));
  }
}
