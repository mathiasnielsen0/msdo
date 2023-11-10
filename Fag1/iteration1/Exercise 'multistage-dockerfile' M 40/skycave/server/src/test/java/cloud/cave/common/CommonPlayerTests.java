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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.*;

import cloud.cave.domain.*;
import cloud.cave.doubles.TestConstants;
import cloud.cave.server.common.Point3;

/**
 * A collection of tests regarding player that is repeated in both the server
 * side testing (TDD of the basic behaviour of an implementation of player) as
 * well as the client side proxy testing (TDD of the client proxy's marshalling
 * and demarshalling of request/reply objects from the server request handler).
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 *
 */

public class CommonPlayerTests {

  public static void shouldAccessSimpleAttributes(Player player) {
    assertEquals("Mikkel", player.getName());
    assertEquals("user-001", player.getID());
    assertEquals(Region.AARHUS, player.getRegion());
    assertEquals("(0,0,0)", player.getPosition());
  }

  public static void shouldTestCoordinateTranslations(Player player) {
    assertEquals("(0,0,0)", player.getPosition());

    player.move(Direction.NORTH);
    assertEquals("(0,1,0)", player.getPosition());
    player.move(Direction.SOUTH);
    assertEquals("(0,0,0)", player.getPosition());

    player.move(Direction.UP);
    assertEquals("(0,0,1)", player.getPosition());
    player.move(Direction.DOWN);
    assertEquals("(0,0,0)", player.getPosition());

    player.move(Direction.WEST);
    assertEquals("(-1,0,0)", player.getPosition());

    player.move(Direction.EAST);
    assertEquals("(0,0,0)", player.getPosition());
  }

  public static void shouldAllowPlayerToDigNewRooms(Player player) {
    UpdateResult updateResult = player.digRoom(Direction.DOWN, "Road Cellar");
    assertThat(updateResult, is(UpdateResult.UPDATE_OK));

    UpdateResult valid = player.move(Direction.DOWN);
    String roomDesc = player.getShortRoomDescription();
    assertThat(roomDesc, containsString("Road Cellar"));
  }

  public static void shouldNotAllowDigAtEast(Player player) {
    UpdateResult allowed = player.digRoom(Direction.EAST, "Santa's cave.");
    assertThat(allowed, is(not(UpdateResult.UPDATE_OK)));
  }

  public static void shouldShowExitsForPlayersPosition(Player player) {
    List<Direction> exits = player.getExitSet();
    assertThat(exits, hasItems(Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP));
    assertThat(exits, not(hasItem( Direction.SOUTH)));
    assertThat(exits.size(), is(4));

    // move east, which only as one exit, back west
    player.move(Direction.EAST);
    exits = player.getExitSet();
    assertThat(exits, hasItem(Direction.WEST));
    assertThat(exits.size(), is(1));
  }

  public static void shouldGetProperExitSet(Player player) {
    List<Direction> exitSet = player.getExitSet();
    assertThat(exitSet.size(), is(4));

    assertThat(exitSet, hasItem(Direction.NORTH));
    assertThat(exitSet, hasItem(Direction.WEST));
    assertThat(exitSet, hasItem(Direction.UP));
    assertThat(exitSet, hasItem(Direction.EAST));
  }

  public static void shouldBeAtPositionOfLastLogout(Cave cave, Player player) {
    Point3 pos = new Point3(0, 0, 0);
    assertThat(player.getPosition(), is(pos.getPositionString()));

    // Move mathilde
    player.move(Direction.EAST);
    String newPos = player.getPosition();

    // Log her out
    LogoutResult logoutResult = cave.logout(player.getID());

    assertThat(logoutResult, is(LogoutResult.SUCCESS));

    // Log her back in
    player = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);

    // and verify she is in the place she left
    assertThat(player.getPosition(), is(newPos));
  }

  public static void shouldSeeMathildeComingInAndOutOfRoomDuringSession(Cave caveProxy, Player playerAlreadyInRoom) {
    // Log in mathilde and verify that both persons are there
    Player m = HelperMethods.loginPlayer(caveProxy, TestConstants.MATHILDE_AARSKORT);

    List<String> playersInEntryRoom = m.getPlayersHere();
    assertThat(playersInEntryRoom.size(), is(2));
    assertThat(playersInEntryRoom, hasItems(m.getName(), playerAlreadyInRoom.getName()));

    // log mathilde out, and in again, and verify that
    // the list of players in room is still correct
    LogoutResult logoutResult = caveProxy.logout(m.getID());
    assertThat(logoutResult, is(LogoutResult.SUCCESS));

    Player m2 = HelperMethods.loginPlayer(caveProxy, TestConstants.MATHILDE_AARSKORT);

    // DO NOT USE 'm' from here, as it will throw a session expired exception!
    assertNotNull(m2);

    playersInEntryRoom = m2.getPlayersHere();
    assertThat(playersInEntryRoom.size(), is(2));
    assertThat(playersInEntryRoom, hasItems(m2.getName(), playerAlreadyInRoom.getName()));
  }
}
