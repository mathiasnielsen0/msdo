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

import java.util.StringJoiner;

/** A data transfer object, DTO, for wall messages.
 * It contains the formatted output to print for the client, as well
 * as a unique ID for that message, to allow identification when
 * editing.
 */
public class WallMessageDataTransferObject {
  private final String id;
  private final String messageContents;

  public WallMessageDataTransferObject(String uniqueWallMessageId, String formattedWallMessage) {
    this.id = uniqueWallMessageId;
    messageContents = formattedWallMessage;
  }
  public String getMessage() {
    return messageContents;
  }
  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", WallMessageDataTransferObject.class.getSimpleName() + "[", "]")
            .add("id='" + id + "'")
            .add("messageContents='" + messageContents + "'")
            .toString();
  }
}
