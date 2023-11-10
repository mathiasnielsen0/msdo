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

package cloud.cave.server;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.TestStubSubscriptionService;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import cloud.cave.doubles.TestConstants;

import cloud.cave.common.*;
import cloud.cave.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

/** Test cases for the server side implementation of the
 * Cave. Heavy use of test doubles to avoid all dependencies
 * to external services.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestCaveServant {
  
  private Cave cave;
  
  private Player p1, p2;

  @BeforeEach
  public void setup() {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
  }

  @Test
  public void shouldAllowAddingPlayers() {
    CommonCaveTests.shouldAllowAddingPlayers(cave);
  }
  
  @Test
  public void shouldRejectUnknownSubscriptions() {
    CommonCaveTests.shouldRejectUnknownSubscriptions(cave);
  }
  
  @Test
  public void shouldAllowLoggingOutMagnus() {
    enterBothPlayers();
    CommonCaveTests.shouldAllowLoggingOutMagnus(cave, p1);
  }

  @Test
  public void shouldNotAllowLoggingOutMathildeTwice() {
    enterBothPlayers();
    CommonCaveTests.shouldNotAllowLoggingOutMathildeTwice(cave,p2);
  }
  
  @Test
  public void shouldWarnIfMathildeLogsInASecondTime() {
    enterBothPlayers();
    CommonCaveTests.shouldWarnIfMathildeLogsInASecondTime(cave);
  }
  
  private void enterBothPlayers() {
    p1 = HelperMethods.loginPlayer(cave, TestConstants.MAGNUS_AARSKORT);
    p2 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
  }

  @Test
  public void shouldDescribeConfiguration() {
    String configString = cave.describeConfiguration();
    assertThat(configString, is(notNullValue()));
    assertThat(configString, containsString("CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(configString, containsString("SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(configString, containsString("PlayerNameService: cloud.cave.server.InMemoryNameService"));
  }

  @Test
  public void shouldNotLoginWhenInternalDaemonErrorOccurs() {
    // Given a Factory whose SubscriptionConnector is a Saboteur that
    // throws 500 internal server error
    CaveServerFactory factory = new AllTestDoubleFactory() {
      public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
        SubscriptionService service = new TestStubSubscriptionService() {

          @Override
          public SubscriptionRecord authorize(String loginName, String password) {
            return new SubscriptionRecord(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          }
        };
        service.initialize(objMgr, null); // no config object required for the stub
        return service;
      }};
    ObjectManager objMgr = new StandardObjectManager(factory);
    // Given a Cave that will error internally on any login attempt
    cave = objMgr.getCave();

    // When Mikkel tries to log in
    Player p1 = cave.login(TestConstants.MIKKEL_AARSKORT, TestConstants.MIKKEL_PASSWORD);
    // Then a non-null player object is returned
    assertThat(p1, is(notNullValue()));
    // Then its auth status is 'failed due to server error'
    assertThat(p1.getAuthenticationStatus(), is(LoginResult.LOGIN_FAILED_SERVER_ERROR));
  }
}
