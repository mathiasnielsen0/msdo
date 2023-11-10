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

package cloud.cave.service;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.service.quote.QuoteRecord;
import cloud.cave.service.quote.QuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


/** Template for Test-Driven Development of the
 * QuoteService
 *
 */
public class TestQuoteService {
  QuoteService quoteService;
  private ObjectManager objMgr;

  @BeforeEach
  public void setup() {
    objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    quoteService = objMgr.getQuoteService();
  }


  // TODO: Solve the 'quote-double' exercise by removing this test, and make tests to TDD the test double quote service
  @Test
  public void shouldTestQuoteIdAPI() {
    QuoteRecord q = null;

    assertThrows(RuntimeException.class, () -> quoteService.getQuote(7));
    /*
    assertThat(q.getQuote(), is("The true sign of intelligence is not knowledge but imagination."));
    assertThat(q.getAuthor(), is("Albert Einstein"));
    assertThat(q.getNumber(), is(7));
    */
  }
}
