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

import java.util.List;

import cloud.cave.doubles.TestConstants;

import cloud.cave.common.*;
import cloud.cave.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testing that the cave can handle having multiple
 * players in the cave at the same time.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestMultiplePlayers {

  private Cave cave;
  
  private Player p1, p2;

  @BeforeEach
  public void setup() {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
  }
 
  private void enterBothPlayers() {
    p1 = HelperMethods.loginPlayer(cave, TestConstants.MAGNUS_AARSKORT);
    p2 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
  }

  @Test
  public void shouldAllowIndependentMoves() {
    enterBothPlayers();
    
    // p1 moves west, only one left in entry room
    p1.move(Direction.WEST);

    // p2 moves east, none left in entry room
    p2.move(Direction.EAST);

    // The room descriptions are different
    assertThat(p1.getShortRoomDescription(), is(not(p2.getShortRoomDescription())));
    
    p2.move(Direction.WEST);
    assertThat(p1.getShortRoomDescription(), is(not(p2.getShortRoomDescription())));
    
    p2.move(Direction.WEST);
    assertThat(p1.getShortRoomDescription(), is(p2.getShortRoomDescription()));
  }
  
  @Test
  public void shouldSeeOtherPlayersInSameLocation() {
    enterBothPlayers();
    p1.move(Direction.WEST);
    
    // only myself
    assertThat(p1.getPlayersHere().size(), is(1));
    List<String> playersHere;
    playersHere = p1.getPlayersHere();
    assertThat( playersHere, hasItem(p1.getName()));
    
    // move p2 there
    p2.move(Direction.WEST);
    assertThat( p2.getPlayersHere().size(), is(2));
    playersHere = p2.getPlayersHere();
    assertThat( playersHere, hasItems(p1.getName(), p2.getName()));
  }
  
  @Test
  public void shouldReturnMultiplePlayersInRoom() {
    enterBothPlayers();
    List<String> playersHere = p1.getPlayersHere();

    assertThat(playersHere, hasItems("Magnus", "Mathilde"));
    assertThat(playersHere.size(), is(2));
    
    // move Mathilde out of the room
    p2.move(Direction.WEST);
    
    // and let Magnus inspect the room again
    playersHere = p1.getPlayersHere();

    assertThat(playersHere, hasItem("Magnus"));
    assertThat(playersHere.size(), is(1));
  }
  
  @Test
  public void shouldReturnPlayerInRoomOverPlayerLogOut() {
    enterBothPlayers();
    // Now log out Magnus
    CommonCaveTests.shouldAllowLoggingOutMagnus(cave, p1);
    
    // Only Mathilde appears in players here list
    List<String> playersHere = p2.getPlayersHere();

    assertThat(playersHere, hasItems("Mathilde"));
    assertThat(playersHere.size(), is(1));
  }
  
  @Test
  public void shouldSeePlayersInRoom() {
    p1 = HelperMethods.loginPlayer(cave, TestConstants.MAGNUS_AARSKORT);

    CommonPlayerTests.shouldSeeMathildeComingInAndOutOfRoomDuringSession(cave, p1);
  }

}


