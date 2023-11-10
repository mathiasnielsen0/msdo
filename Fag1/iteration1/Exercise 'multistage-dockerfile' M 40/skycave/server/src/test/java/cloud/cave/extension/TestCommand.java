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

package cloud.cave.extension;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.common.HelperMethods;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.doubles.TestConstants;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/** TDD the Command pattern used to define 'new commands' in
 * the SkyCave.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class TestCommand {
  private Cave cave;
  private Player player;

  @BeforeEach
  public void setup() {
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    cave = objMgr.getCave();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  @Test
  public void shouldExecuteHomeCommand() {
    // move north
    player.move(Direction.NORTH);
    String pos;
    String desc = player.getShortRoomDescription();

    // validate new position
    pos = player.getPosition();
    assertThat(pos, is("(0,1,0)"));

    // execute dynamic command 'home'
    List<String> result = player.execute("HomeCommand", "null");
    assertThat(result, is(notNullValue()));
    assertThat(result.get(0), is("You went home to position (0,0,0)"));

    // and validate home behaviour = position is reset to 0,0,0
    pos = player.getPosition();
    assertThat(pos, is("(0,0,0)"));

    // and the room description is not identical to the old one
    assertThat(player.getShortRoomDescription(), not(is(desc)));

    // and that it is the 'You are standing...'
    assertThat(player.getShortRoomDescription(), containsString("You are standing at the end of a road"));
  }

  @Test
  public void shouldExecuteJumpCommand() {
    String pos;
    // validate current position
    pos = player.getPosition();
    assertThat(pos, is("(0,0,0)"));

    // execute dynamic command 'jump'
    List<String> result = player.execute("JumpCommand", "(0,1,0)");
    assertThat(result, is(notNullValue()));

    // validate new position is also updated!
    pos = player.getPosition();
    assertThat(pos, is("(0,1,0)"));
    // and that indeed the description is of (0,1,0)
    assertThat(player.getShortRoomDescription(), containsString("open forest, with a deep valley"));

    // Validate that a jump to unknown locations is caught
    result = player.execute("JumpCommand", "(700,10,-42)");
    assertThat(result, is(notNullValue()));

    String errMsg = result.get(0);
    assertThat(errMsg,
            is("JumpCommand failed, room (700,10,-42) does not exist in the cave."));
  }

  @Test
  public void shouldExecuteUnknownCommand() {
    List<String> result = player.execute("BimseCommand", "really has not clue here", "more nonsense");
    assertThat(result, is(notNullValue()));

    String errMsg = result.get(0);
    assertThat(errMsg,
            is("Player.execute failed to load Command class: BimseCommand"));
  }

}
