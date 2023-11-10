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

/** Record type for a single quote from the quote service.
 * JSON format: {number: int, author: string, quote: string, statusCode: http status code}
 *
 * statusCode == 200 tells that the record is valid, while 404 signals that the
 * fetched quote does not represent a quote that could be found.
 */

public class QuoteRecord {
  private final String quote;
  private final String author;
  private final int number;
  private final int statusCode;

  public QuoteRecord(int number, String quote, String author, int statusCode) {
    this.number = number;
    this.quote = quote;
    this.author = author;
    this.statusCode = statusCode;
  }

  public String getQuote() {
    return quote;
  }

  public String getAuthor() {
    return author;
  }

  public int getNumber() {
    return number;
  }

  public int getStatusCode() {
    return statusCode;
  }
}

