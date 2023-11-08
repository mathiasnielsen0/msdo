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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.cave.config.ObjectManager;
import cloud.cave.doubles.TestConstants;

import cloud.cave.common.*;
import cloud.cave.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/** Test server side implementation of the Player abstraction.
 * 
 * On the server side, a player object directly communicate
 * with the storage layer in order to modify its state.
 * 
 * Most of these tests are the results of TDD. 
 * 
 * Many of the 'later' tests are abstracted into
 * static methods in CommonPlayerTests to allow
 * the same tests to be run using the client side
 * proxies as well to test the Broker handling
 * all method calls correctly.
 *
 * Some methods are tested in separate test classes
 * as they serve as exercises: Quote, Wall, Room handling.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestPlayerServant {

  private Cave cave;
  private Player player;
  
  private String description;

  @BeforeEach
  public void setup() {
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    cave = objMgr.getCave();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }
 
  // TDD of simple player attributes
  @Test
  public void shouldAccessSimpleAttributes() {
    CommonPlayerTests.shouldAccessSimpleAttributes(player);
  }

  // TDD room description
  @Test
  public void shouldHaveInitialLocation() {
    description = player.getShortRoomDescription();

    assertThat(description, is("You are standing at the end of a road before a small brick building."));
  }

  // TDD the movement of a player
  @Test
  public void shouldAllowNorthOneMove() {
    player.move(Direction.NORTH);
    description = player.getShortRoomDescription();
    assertThat(description, is("You are in open forest, with a deep valley to one side."));
    // Move back again
    player.move(Direction.SOUTH);
    description = player.getShortRoomDescription();
    assertThat(
        description, is("You are standing at the end of a road before a small brick building."));
  }
  
  // TDD movement of player
  @Test
  public void shouldAllowEastWestMoves() {
    player.move(Direction.EAST);
    description = player.getShortRoomDescription();
    assertThat(description,
            is("You are inside a building, a well house for a large spring."));
    
    player.move(Direction.WEST);
    description = player.getShortRoomDescription();
    assertThat(description,
            is("You are standing at the end of a road before a small brick building."));

    player.move(Direction.WEST);
    description = player.getShortRoomDescription();
    assertThat(description,
            is("You have walked up a hill, still in the forest."));

    player.move(Direction.EAST);
    description = player.getShortRoomDescription();
    assertThat(description,
            is("You are standing at the end of a road before a small brick building."));
  }

  // Handle illegal moves, trying to move to non-existing room
  @Test
  public void shouldNotAllowMovingSouth() {
    UpdateResult canMove = player.move(Direction.SOUTH);
    assertThat("It should not be possible to move south, no node there",
            canMove, is(UpdateResult.FAIL_AS_NOT_FOUND) );
  }
  
  // TDD the behavior for changing the (x,y,z) coordinates
  // during movement
  @Test
  public void shouldTestCoordinateTranslations() {
    assertEquals( "(0,0,0)", player.getPosition());

    player.move(Direction.NORTH);
    assertEquals( "(0,1,0)", player.getPosition());    
    player.move(Direction.SOUTH);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.UP);
    assertEquals( "(0,0,1)", player.getPosition());    
    player.move(Direction.DOWN);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.WEST);
    assertEquals( "(-1,0,0)", player.getPosition());    

    player.move(Direction.EAST);
    assertEquals( "(0,0,0)", player.getPosition());    
  }
  
  // TDD digging new rooms for a player
  @Test
  public void shouldAllowPlayerToDigNewRooms() {
    CommonPlayerTests.shouldAllowPlayerToDigNewRooms(player);
  }

  // Cannot dig a node in a direction where a node already exists
  @Test
  public void shouldNotAllowDigAtEast() {
    CommonPlayerTests.shouldNotAllowDigAtEast(player);
  }
  
  // TDD of get exits, a validate that the
  // the long description is correct
  @Test
  public void shouldShowExitsForPlayersPosition() {
    CommonPlayerTests.shouldShowExitsForPlayersPosition(player);
  }

  // TDD of get exits
  @Test
  public void shouldShowValidExitsFromEntryRoom() {
    CommonPlayerTests.shouldGetProperExitSet(player);
  }

  // TDD of the long room description
  @Test
  public void shouldProvideLongDescription() {
    List<String> longDescription = player.getLongRoomDescription();

    assertThat(longDescription.get(1), containsString("Creator: Will Crowther, just now."));

    assertThat(longDescription.get(2), containsString("There are exits in"));
    assertThat(longDescription.get(3), containsString("NORTH"));
    assertThat(longDescription.get(3), containsString("WEST"));
    assertThat(longDescription.get(3), containsString("EAST"));

    assertThat(longDescription.get(4), containsString("You see other players:"));
    assertThat(longDescription.get(5), containsString("[0] Mikkel"));
  }



  // Positions of players are stored across logins
  @Test
  public void shouldBeAtPositionOfLastLogout() {
    // Log mathilde into the cave, initial position is 0,0,0
    // as the database is reset
    Player p1 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);

    CommonPlayerTests.shouldBeAtPositionOfLastLogout(cave, p1);
  }

  // TDD of session id, later changed to an access token
  @Test
  public void shouldAssignUniqueAccessTokenForEveryLogin() {
    // The session id should be a new ID for every session 
    // (a session lasts from when a player logs in until he/she
    // logs out).
    String originalAccessToken = player.getAccessToken();
    String playerId = player.getID();
    assertThat(originalAccessToken, is(notNullValue()));
    
    // Do a second login and ensure that it gets a new session id
    Player p1 = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
    
    // It should be the same player, now double "logged in"
    // note, cannot call player.getID() as this will throw an
    // access control exception
    assertThat(playerId, is(p1.getID()));
    
    // But the session id is different
    assertThat(p1.getAccessToken(), is(not(originalAccessToken)));
  }
  
  // Test just to increase coverage :)
  @Test
  public void shouldReturnReasonableToString() {
    assertThat(player.toString(), containsString("storage=FakeCaveStorage"));
    assertThat(player.toString(), containsString("name='Mikkel'"));
    assertThat(player.toString(), containsString("ID='user-001'"));
    assertThat(player.toString(), containsString("region=AARHUS"));
  }
}
