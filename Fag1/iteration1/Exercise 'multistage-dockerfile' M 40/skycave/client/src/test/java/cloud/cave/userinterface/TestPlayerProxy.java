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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import cloud.cave.doubles.TestConstants;

import cloud.cave.common.*;
import cloud.cave.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test that the PlayerClientProxy allows all behavior defined by the Player
 * interface to be successfully communicated with the server tier.
 * <p>
 *   As the test focus is on the Broker chain of roles, notably the
 *   PlayerClientProxy and the server side Invoker implementations, there
 *   is actually no need to test all methods thoroughly as it is already
 *   done on the PlayerServant testing. However, quite a few
 *   tests are simply reused by abstracting them into the
 *   'CommonPlayerTests' class.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestPlayerProxy {

  private Player player;
  private Cave cave;
  
  @BeforeEach
  public void setUp() throws Exception {
    // Given a Cave Proxy
    cave = CommonClientCaveTest.createCaveProxyForTesting();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  @Test
  public void shouldAccessSimpleAttributes() {
    CommonPlayerTests.shouldAccessSimpleAttributes(player);
  }

  @Test
  public void shouldGetShortRoomDescription() {
    String description = player.getShortRoomDescription();
    assertThat(description, is("You are standing at the end of a road before a small brick building."));
  }

  @Test
  public void shouldGetPosition() {
    CommonPlayerTests.shouldTestCoordinateTranslations(player);
  }

  @Test
  public void shouldHandleRemoteMoveAndDescription() {
    // move east 
    player.move(Direction.EAST);
    String description = player.getShortRoomDescription();
    // System.out.println(description);
    assertThat(description, is("You are inside a building, a well house for a large spring."));
  }

  @Test
  public void shouldProvideLongDescription() {
    List<String> longDescription = player.getLongRoomDescription();
    assertThat(longDescription.get(1), containsString("Creator: Will Crowther, just now."));
  }

  @Test
  public void shouldAllowPlayerToDigNewRooms() {
    CommonPlayerTests.shouldAllowPlayerToDigNewRooms(player);
  }
  
  @Test
  public void shouldNotAllowDigAtEast() {
    CommonPlayerTests.shouldNotAllowDigAtEast(player);
  }
  
  @Test
  public void shouldShowExitsForPlayersPosition() {
    CommonPlayerTests.shouldShowExitsForPlayersPosition(player);
  }

  @Test
  public void shouldSeePlayersInRoom() {
    CommonPlayerTests.shouldSeeMathildeComingInAndOutOfRoomDuringSession(cave, player);
  }
  
  @Test
  public void shouldShowValidExitsFromEntryRoom() {
    CommonPlayerTests.shouldGetProperExitSet(player);
  }

  @Test
  public void shouldBeAtPositionOfLastLogout() {
    // Log mathilde into the cave, initial position is 0,0,0
    // as the database is reset
    Player p1 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);

    CommonPlayerTests.shouldBeAtPositionOfLastLogout(cave, p1);
  }
  
  @Test
  public void shouldValidateToString() {
    assertThat( player.toString(), is("(PlayerClientProxy: user-001/Mikkel)"));
  }
  
  @Test
  public void shouldHaveAccessTokenAssigned() {
    assertThat(player.getAccessToken(), is(notNullValue()));
  }

  Player p1, p2;
  
  private void enterBothPlayers() {
    p1 = HelperMethods.loginPlayer(cave, TestConstants.MAGNUS_AARSKORT);
    p2 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
  }

  // Test that if a second client connects using the
  // same credentials as a first client is already
  // connected with, then the first client is
  // prevented from any actions ("disconnected" in
  // a sense). This is similar to the behavior of
  // Blizzard games (which is probably the standard).
  
  @Test
  public void shouldPreventCallsFromDualLogins() {
    enterBothPlayers();
    p2.move(Direction.EAST);

    // log in Mathilde a second time
    Player p2second = null;
    p2second = cave.login( TestConstants.MATHILDE_AARSKORT, TestConstants.MATHILDE_PASSWORD);
    assertThat( p2second.getAuthenticationStatus(), is(LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN));

    // just precautions - we have two objects representing same player, right?
    assertThat(p2, is(not(p2second)));
    
    // Verify that the second client logged in is in the same
    // room as the the first client moved to
    assertThat(p2second.getPosition(), is("(1,0,0)"));
    
    // Verify that the first client CANNOT move mathilde west even
    // though the topology of the cave would allow it, instead
    // throws an Exception
    try {
      p2.move(Direction.WEST);
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThatProperSessionExpiredInformationIsPresentInException(e);
    }
    
    // assert that the second session IS allowed to do it
    assertThat( p2second.move(Direction.WEST), is(UpdateResult.UPDATE_OK));

    // Verify a few other methods
    try {
      p2.getPosition();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThatProperSessionExpiredInformationIsPresentInException(e);
    }

    try {
      p2.getShortRoomDescription();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThatProperSessionExpiredInformationIsPresentInException(e);
    }

    try {
      p2.getLongRoomDescription();
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThatProperSessionExpiredInformationIsPresentInException(e);
    }
  }

  private void assertThatProperSessionExpiredInformationIsPresentInException(PlayerSessionExpiredException e) {
    assertThat( e.getMessage(), containsString("The session for player with ID user-003 has expired"));
  }

  @Test
  public void shouldHandleImmediateLogoutOfSecondSession() {
    enterBothPlayers();

    // log in Mikkel a second time
    Player pMikkel2ndSession;
    pMikkel2ndSession = cave.login( TestConstants.MIKKEL_AARSKORT, TestConstants.MIKKEL_PASSWORD);
    assertThat(pMikkel2ndSession.getAuthenticationStatus(), is(LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN));

    // And immediately log him out again
    LogoutResult logoutResult = cave.logout(pMikkel2ndSession.getID());
    assertThat(logoutResult, is(LogoutResult.SUCCESS));
  
    // Now ensure that the first session behaves correctly
    // i.e. throws a PlayerSessionExpiredException...
    try {
      player.move(Direction.DOWN);
      fail("The first client must throw an exception when attempting any further calls");
    } catch( PlayerSessionExpiredException e ) {
      assertThat(e.getMessage(), containsString("The session for player with ID user-001 has expired (Multiple logins made)"));
    }
  }

  @Test
  public void shouldHandleUpdatingRoomDescription() {
    // Given a room dug by this player
    player.digRoom(Direction.DOWN, "New Room");
    player.move(Direction.DOWN);
    
    UpdateResult status = player.updateRoom("Updated Room");
    assertThat(status, is(UpdateResult.UPDATE_OK));
  }

  @Test
  public void shouldHandleExecCommand() {
    // Given a player that has moved to the north
    player.move(Direction.NORTH);
    assertThat(player.getPosition(), is("(0,1,0)"));

    // When issuing the HomeCommand
    List<String> output = player.execute("HomeCommand", "null");
    // Then I am back in (0,0,0)
    assertThat(player.getPosition(), is("(0,0,0)"));
    assertThat(output.get(0), is("You went home to position (0,0,0)"));
  }
}
