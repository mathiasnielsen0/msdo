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
import cloud.cave.domain.Cave;
import cloud.cave.domain.Player;
import cloud.cave.doubles.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/** Testing the quote method of Player on the client side.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestQuoteClient {
  private Cave cave;
  private Player player;

  @BeforeEach
  public void setUp() throws Exception {
    cave = CommonClientCaveTest.createCaveProxyForTesting();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  @Test
  public void shouldGetQuoteOnClientSide() {
    String quote = player.getQuote(7);
    assertThat(quote, is("The true sign of intelligence is not knowledge but imagination. - Albert Einstein"));
  }

}
