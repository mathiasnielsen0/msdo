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


import cloud.cave.common.CommonClientCaveTest;
import cloud.cave.common.HelperMethods;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
/**
 * Test the Cave proxy which has the ability to log a player in and out of the
 * cave.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestCaveProxy {

  private Cave cave;

  private Player p1, p2;

  @BeforeEach
  public void setup() {
    cave = CommonClientCaveTest.createCaveProxyForTesting();
  }

  @Test
  public void shouldAllowLoginOfMikkel() {
    // One player
    Player p1 = cave.login( TestConstants.MIKKEL_AARSKORT, TestConstants.MIKKEL_PASSWORD);
    // System.out.println(loginResult);
    assertThat( p1.getAuthenticationStatus(), is(LoginResult.LOGIN_SUCCESS));
    assertThat(p1, is(notNullValue()));

    assertThat(p1.getID(), is("user-001"));
    assertThat( p1.getName(), is("Mikkel"));
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
    
    // System.out.println(configString);
    assertThat(configString, containsString("CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(configString, containsString("SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(configString, containsString("PlayerNameService: cloud.cave.server.InMemoryNameService"));
  }
}
