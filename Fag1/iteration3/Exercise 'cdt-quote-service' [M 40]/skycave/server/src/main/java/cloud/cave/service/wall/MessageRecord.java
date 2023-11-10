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

package cloud.cave.service.wall;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A Record type for a Message on a wall.
 * Note: Setters are provided for fields that are
 * usually set by the storage system: timestamp and
 * unique id.
 */
public class MessageRecord {
  private final String creatorId;
  private final String creatorName;

  private String contents;
  private String creatorTimeStampISO8601;
  private String id;

  public MessageRecord(String contents, String creatorId, String creatorName) {
    this.contents = contents;
    this.creatorId = creatorId;
    this.creatorName = creatorName;
    this.creatorTimeStampISO8601 = "none";
    this.id = "none";
  }

  // Josh Bloch - prefer a copy constructor over clone()
  public MessageRecord(MessageRecord messageRecord) {
    this.contents = messageRecord.getContents();
    this.creatorId = messageRecord.getCreatorId();
    this.creatorName = messageRecord.getCreatorName();
    this.creatorTimeStampISO8601 = messageRecord.getCreatorTimeStampISO8601();
    this.id = messageRecord.getId();
  }

  public String getContents() {
    return contents;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public String getCreatorTimeStampISO8601() {
    return creatorTimeStampISO8601;
  }

  public void setCreatorTimeStampISO8601(ZonedDateTime timestamp) {
    setCreatorTimeStampISO8601(timestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
  }

  public void setCreatorTimeStampISO8601(String creatorTimeStampISO8601) {
    this.creatorTimeStampISO8601 = creatorTimeStampISO8601;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /** raw overwriting of the contents string,
   * clients must ensure that overwriting is allowed
   * (i.e. same owner id!)
   * @param contents
   */
  public void setContents(String contents) {
    this.contents = contents;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MessageRecord.class.getSimpleName() + "[", "]")
            .add("contents='" + contents + "'")
            .add("creatorId='" + creatorId + "'")
            .add("creatorName='" + creatorName + "'")
            .add("creatorTimeStampISO8601='" + creatorTimeStampISO8601 + "'")
            .add("id='" + id + "'")
            .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageRecord that = (MessageRecord) o;
    return Objects.equals(contents, that.contents) &&
            Objects.equals(creatorId, that.creatorId) &&
            Objects.equals(creatorName, that.creatorName) &&
            Objects.equals(creatorTimeStampISO8601, that.creatorTimeStampISO8601) &&
            Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contents, creatorId, creatorName, creatorTimeStampISO8601, id);
  }

}
