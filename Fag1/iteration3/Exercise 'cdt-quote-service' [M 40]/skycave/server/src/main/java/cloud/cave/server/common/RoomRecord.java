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

package cloud.cave.server.common;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * This is a record type (struct / PODO (Plain Old Data Object)) representing
 * the core data of a room. Note that external sources are expected to
 * set unique id and timestamp, typically by a database service.
 *
 * No Date class is used directly as IMO it usually leads to
 * marshalling/demarshalling issues, so date+time is expressed
 * as ISO8601 full format string which are unique and well
 * supported by the ZonedDateTime java class, and the
 * DateTimeFormatter.ISO_OFFSET_DATE_TIME formatting.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class RoomRecord {
  private String id;
  private String creationTimeISO8601;
  private String description;
  private String creatorId;

  /** Create a record for a room with a given description
   * and given creator (must equal a player.getID() known
   * to the SkyCave). The record is NOT complete in the
   * sense that creation time and record ID must be set
   * by some storage system upon committing the record.
   *
   * @param description the textual description of the room
   * @param creatorId the unique ID of the player creating
   *                  the room.
   */
  public RoomRecord(String description, String creatorId) {
    this.description = description;
    this.creatorId = creatorId;
    this.creationTimeISO8601 = "none";
    this.id = "none";
  }

  // Josh Bloch - a copy constructor
  public RoomRecord(RoomRecord newRoom) {
    this.description = newRoom.getDescription();
    this.creatorId = newRoom.getCreatorId();
    this.creationTimeISO8601 = newRoom.getCreationTimeISO8601();
    this.id = newRoom.getId();
  }

  /** Get unique ID of this room
   *
   * @return unique id
   */
  public String getId() {
    return id;
  }

  /** Set/overrwrite the unique id of
   * this room. Usually only the storage
   * system should do that.
   * @param id unique id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the textual description of the room
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /** Set the textual description of the room
   *
   * @param newDescription the updated description
   */
  public void setDescription(String newDescription) {
    description = newDescription;
  }

  /** Get ID of the creator of this room,
   * must be equal to a player.getID() known
   * by the system
   * @return creator ID
   */
  public String getCreatorId() {
    return creatorId;
  }

  /** Set the ID of the creator.
   *
   * @param id unique player identity of
   *           creator
   */
  public void setCreatorId(String id) {
    this.creatorId = id;
  }

  /**
   * Get the time when this record was
   * created (committed to storage)
   *
   * @return date-time of creation in
   * the full ISO8601 format including
   * timezone
   */
  public String getCreationTimeISO8601() {
    return creationTimeISO8601;
  }

  /** Set the creation time
   *
   * @param timeStamp a ZonedDateTime timestamp
   */
  public void setCreationTime(ZonedDateTime timeStamp) {
    creationTimeISO8601 = timeStamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RoomRecord.class.getSimpleName() + "[", "]")
            .add("id='" + id + "'")
            .add("creationTimeISO8601='" + creationTimeISO8601 + "'")
            .add("description='" + description + "'")
            .add("creatorId='" + creatorId + "'")
            .toString();
  }

}
