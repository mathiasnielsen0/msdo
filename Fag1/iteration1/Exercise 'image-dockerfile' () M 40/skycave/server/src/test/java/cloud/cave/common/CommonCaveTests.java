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

package cloud.cave.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.TestConstants;

/** Common set of test cases and fixture code for server side testing.
 */
public class CommonCaveTests {

  public static ObjectManager createTestDoubledConfiguredCave() {
    CaveServerFactory factory = new AllTestDoubleFactory();
    ObjectManager objMgr = new StandardObjectManager(factory);
    return objMgr;
  }

  public static void shouldAllowAddingPlayers(Cave cave) {
    // One player
    Player p1 = HelperMethods.loginPlayer(cave, TestConstants.MAGNUS_AARSKORT);
    assertNotNull(p1);
    assertEquals( "user-002", p1.getID());
    assertThat( p1.getName(), is("Magnus"));
    assertThat( p1.getRegion(), is(Region.COPENHAGEN));

    // Enter Mathilde
    Player p2 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
    assertNotNull(p2);
    assertEquals( "user-003", p2.getID());
    assertThat( p2.getName(), is("Mathilde"));
  }

  public static void shouldAllowLoggingOutMagnus(Cave cave, Player p1) {
    // log out p1
    LogoutResult result = cave.logout(p1.getID());
    assertNotNull(result);
    assertEquals(LogoutResult.SUCCESS, result);
  }

  public static void shouldNotAllowLoggingOutMathildeTwice(Cave cave, Player p2) {
    // log out Mathilde
    LogoutResult result = cave.logout(p2.getID());
    assertEquals(LogoutResult.SUCCESS, result);
    
    result = cave.logout(p2.getID());
    assertEquals(LogoutResult.PLAYER_NOT_IN_CAVE, result);
  }

  public static void shouldWarnIfMathildeLogsInASecondTime(Cave cave) {
    // Try to login mathilde a second time
    Player p = null;
    //PlayerDataTransferObject loginResult = cave.login( TestConstants.MATHILDE_AARSKORT, TestConstants.MATHILDE_PASSWORD);
    p = cave.login(TestConstants.MATHILDE_AARSKORT, TestConstants.MATHILDE_PASSWORD);
    // The login should be successfull but a warning should be issued of potentially
    // more than one client operating the played
    assertThat(p.getAuthenticationStatus(), is(LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN));

    assertThat(p, is(notNullValue()));
    assertThat( p.getID(), is("user-003"));
  }

  public static void shouldRejectUnknownSubscriptions(Cave cave) {
    Player p = cave.login( "bandit@cs.au.dk", "wrongkey");
    assertEquals( LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, p.getAuthenticationStatus() );

    p = cave.login( TestConstants.MAGNUS_AARSKORT, "wrongkey");
    assertThat(p, is(notNullValue()));
    assertEquals( LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION, p.getAuthenticationStatus() );
  }

}
