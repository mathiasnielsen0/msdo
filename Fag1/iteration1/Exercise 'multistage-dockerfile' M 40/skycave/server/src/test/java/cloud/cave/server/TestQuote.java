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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.common.HelperMethods;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Player;
import cloud.cave.doubles.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testing the player's ability to get quotes.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestQuote {

  private Cave cave;
  private Player player;

  @BeforeEach
  public void setUp() throws Exception {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  // TODO: Update to solve the 'quote-double' and 'random-quote-double' exercises
  @Test
  public void shouldReturnCorrectlyFormattedQuotes() {
    String quote;

    quote = player.getQuote(1);
    assertThat( quote, is("Take small steps - use the ladder, not the vaulting pole. - Henrik Bærbak Christensen"));
    quote = player.getQuote(7);
    assertThat( quote, is("The true sign of intelligence is not knowledge but imagination. - Albert Einstein"));
    quote = player.getQuote(13);
    assertThat( quote, is("Education is what remains after one has forgotten what one has learned in school. - Albert Einstein"));
    quote = player.getQuote(16);
    assertThat( quote, is("*The requested quote was not found*"));
  }
}
