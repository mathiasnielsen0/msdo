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

package cloud.cave.doubles;

import cloud.cave.common.ServerConfiguration;
import cloud.cave.config.ObjectManager;
import cloud.cave.service.quote.QuoteHeader;
import cloud.cave.service.quote.QuoteRecord;
import cloud.cave.service.quote.QuoteService;
import org.apache.http.MethodNotSupportedException;

/** A Saboteur Quote Service. It has NO USE except as placeholder
 * until you solve the exercises that provides a real test double
 * for the quote service. Until then the 'factories' have to
 * instantiate something, and this is the placeholder.
 *
 * Will just through runtime exceptions.
 *
 * @author Henrik Bærbak Christensen, Aarhus University
 */
public class SaboteurQuoteService implements QuoteService {
  private ServerConfiguration config;

  @Override
  public QuoteRecord getQuote(int quoteIndex) {
    throw new RuntimeException("Solve 'quote-double' exercise, please.");
  }

  @Override
  public QuoteHeader getHeader() {
    throw new RuntimeException("Solve 'quote-double' exercise, please.");
  }

  @Override
  public void initialize(ObjectManager objectManager, ServerConfiguration config) {
    this.config = config;
  }

  @Override
  public void disconnect() {
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return config;
  }
}
