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

import java.util.List;

/* Record type for the quote service header whose
JSON format is:
        {
        "published": datetime,
        "url": string,
        "title": string,
        "authors": [ string, string, ... ]
        "totalItems" : integer
        }

        */
public class QuoteHeader {
  private final int totalItems;
  private final List<String> authors;
  private final String url;
  private final String published;
  private final String title;

  public QuoteHeader(String publishedTimeAsISO8601String,
                     String url,
                     String title,
                     List<String> authors,
                     int totalItems) {
    this.totalItems = totalItems;
    this.authors = authors;
    this.title = title;
    this.url = url;
    this.published = publishedTimeAsISO8601String;
  }

  public int totalItems() {
    return totalItems;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public String getUrl() {
    return url;
  }

  public String getPublished() {
    return published;
  }

  public String getTitle() {
    return title;
  }

}
