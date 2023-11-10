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

import java.util.*;

import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.wall.MessageRecord;

/**
 * The storage service for the cave as a set of rooms and a set of players in positions in the cave.
 * <p>
 * The cave must be initialized with four rooms created by Will Crowther, whose playerID is given by the constant
 * below.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 */
public interface CaveStorage extends ExternalService {

    /**
     * Unique player ID of Will Crowther
     */
    public static final String WILL_CROWTHER_ID = "0";

    /**
     * Get the room for the given position
     *
     * @param positionString the (x,y,z) of the position encoded as a position string.
     * @return the room's properties in a record
     */
    RoomRecord getRoom(String positionString);

    /**
     * Add a room in a given position or fail if there is already a room there. The room record will be updated with
     * timestamp and unique ID.
     *
     * @param positionString the (x,y,z) of the position encoded as a position string.
     * @param roomRecord     a record (partially) filled with data for the room, like creator, room description, etc.
     * @return statusCode using HTTP vocabulary, 201 CREATED if the room was added, or 403 FORBIDDEN in case a room
     * already exists
     */
    int addRoom(String positionString, RoomRecord roomRecord);

    /**
     * Update an existing room, overwriting the existing record completely.
     *
     * @param positionString position in the cave (as string) for the room
     * @param updatedRoom    the record of the room to overwrite the existing
     * @return statusCode using the HTTP vocabulary: 200 OK or 404 NOT FOUND in case no room is present at the given
     * position; or 401 UNAUTHENTICATED in case the room was created by someone else.
     */
    int updateRoom(String positionString, RoomRecord updatedRoom);

    /**
     * Compute the set of valid exits leading out from a given position
     *
     * @param positionString position of the room whose exits is wanted
     * @return set of directions that leads to another room
     */
    List<Direction> getSetOfExitsFromRoom(String positionString);

    /**
     * Given a player ID get the record of that player's main attributes. Note that a record is provided even if the
     * player is not presently in the cave (= has no active session)
     *
     * @param playerID id of the player
     * @return the record for the player or null in case the player is not registered in the cave
     */
    PlayerRecord getPlayerByID(String playerID);

    /**
     * Given a player record, update the existing stored record with the contents of the provided one, or create a
     * record if none already exists.
     *
     * @param record the record to insert/overwrite the old one
     */
    void updatePlayerRecord(PlayerRecord record);

    /**
     * Compute a list of players that are located in a given room
     *
     * @param positionString position of the room
     * @return list of player records identify the players presently in the room
     */
    List<PlayerRecord> computeListOfPlayersAt(String positionString);

    /**
     * Add a message to the wall of specific room. Messages are to be ordered chronological in the storage, with a
     * notion of 'newest first', that is, the message is not appended as the last one to a list, rather like pushed as
     * the top one on a stack.
     *
     * @param positionInCave the position of the room in the cave
     * @param messageRecord  the message to add to the 'wall'
     */
    void addMessage(String positionInCave, MessageRecord messageRecord);

    /**
     * Update a wall message for a given position in the cave with the given id. Only the contents of 'newMessageRecord'
     * is used to overwrite the contents of the original message, if it is valid.
     *
     * @param positionInCave   the (x,y,z) position in the cave of the room that contains the message to update
     * @param messageId        the unique id of the message to update
     * @param newMessageRecord a messagerecord that must include the playerID of the player wanting to change the wall
     *                         message, as well as the new contents.
     * @return status code for whether it worked out or not, reusing the HTTP codes: 200 OK, 404 NOT FOUND if the
     * messageId was not found for any messages in this room; 401 UNAUTHORIZED if the message was not created by this
     * player.
     */
    int updateMessage(String positionInCave, String messageId, MessageRecord newMessageRecord);

    /**
     * Return a page of the messages in a given room. A page has a given size and it starts at the given startIndex. The
     * startIndex == 0 is the NEWEST message on the wall in the room. That is: addMessage("1"), addMessage("2"),
     * followed by getMessageList(0, 16) = get page 0, should return ["2", "1"]
     *
     * @param positionInCave the position of the room in the cave
     * @param startIndex     absolute index of message to start the page
     * @param pageSize       getNumber of messages to retrieve
     * @return list of messages starting at index 'startIndex' and of maximal length 'pageSize'. If no messages are
     * available on the page, return an empty list (not a null list).
     */
    List<MessageRecord> getMessageList(String positionInCave, int startIndex, int pageSize);
}
