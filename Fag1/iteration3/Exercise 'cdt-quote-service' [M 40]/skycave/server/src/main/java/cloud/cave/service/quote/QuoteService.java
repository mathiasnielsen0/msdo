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

package cloud.cave.service.quote;

import cloud.cave.service.ExternalService;

/**
 * Represents an external service which can request quotes from
 * a database of famous peoples' quotes.
 *
 * The service has two resource types: the header resource
 * and the individual quote resources. Using the syntax
 * of FRDS §7.7, they look like
 *
 * GET quote header
 * ----------------
 *
 * GET /msdo/v1/quotes
 *   (none)
 *
 * Response
 *   Status: 200 OK
 *  {
 *    "authors": [
 *     "Albert Einstein",
 *     "Søren Kierkegaard",
 *     ],
 *     "published": "2019-06-28T09:35:19.133Z",
 *     "title": "MSDO Quote Service",
 *     "totalItems": 57,
 *     "url": "http://moja.st.client.au.dk:6777/msdo/v1/quotes"
 *  }
 *
 *
 * GET individual quote
 * -------------------
 *
 * GET /msdo/v1/quotes/{quoteIndex}
 *
 * Response
 *   Status: 200 OK
 *  {
 *  "author": "Albert Einstein",
 *  "number": 1,
 *  "quote": "Logic will get you from A to B. Imagination will take you everywhere."
 *  }
 *
 *  Status: 404 NOT FOUND
 *
 *  Status: 400 BAD REQUEST
 *
 * 404 is returned in case the quoteIndex is out of range. 400 is
 * returned in case the quoteIndex is not well formed (not integer)
 *
 *
 */

public interface QuoteService extends ExternalService {
  /**
   * Return a quote of the given index
   * @param quoteIndex index of the given quote,
   *                   PRECONDITON: quoteIndex >= 1
   * @return the quote record itself
   */
  QuoteRecord getQuote(int quoteIndex);

  /**
   * Return the header information about the quote
   * service
   * @return quote header
   */
  QuoteHeader getHeader();
}
