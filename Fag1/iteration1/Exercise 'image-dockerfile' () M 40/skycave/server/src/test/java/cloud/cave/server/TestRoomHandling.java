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

package cloud.cave.server;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.common.HelperMethods;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.domain.UpdateResult;
import cloud.cave.doubles.TestConstants;

import cloud.cave.server.common.RoomRecord;
import cloud.cave.service.CaveStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/** TDD of the 2020 course's extended room handling in
 * SkyCave. A room includes attributes like creator,
 * creation time, and more.
 *
 */
public class TestRoomHandling {

  private ObjectManager objectManager;
  private Cave cave;
  private Player player;

  @BeforeEach
  public void setup() {
    objectManager = CommonCaveTests.createTestDoubledConfiguredCave();
    cave = objectManager.getCave();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  @Test
  public void shouldHandleRoomAttributes() {
    RoomRecord rr = new RoomRecord("A location", player.getID());
    assertThat(rr.getDescription(), is("A location"));
    assertThat(rr.getCreatorId(), is(player.getID()));
  }

  @Test
  public void shouldShowAttributesForNewlyDugRoom() {
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(8, objectManager);
    player.digRoom(Direction.DOWN, "This is a room down from here.");
    player.move(Direction.DOWN);
    List<String> desc = player.getLongRoomDescription();
    assertThat(desc.get(1), containsString("Creator: Mikkel, 8 minutes ago."));
  }
  @Test
  public void shouldHandleCreatorForInitialRooms() {
    List<String> longDescription = player.getLongRoomDescription();
    assertThat(longDescription.get(0), containsString("You are standing at the end of a road"));
    assertThat(longDescription.get(1), containsString("Creator: Will Crowther, just now."));
    assertThat(longDescription.get(3), containsString("NORTH   EAST   WEST   UP"));
  }

  // TDD of the 2020 extension to update room descriptions
  @Test
  public void shouldAllowCreatorToUpdateRoomDescription() {
    // Given: A player who has dug a new room
    UpdateResult result = player.digRoom(Direction.DOWN, "This is AN new room.");
    assertThat(result, is(UpdateResult.UPDATE_OK));

    UpdateResult isValid = player.move(Direction.DOWN);
    assertThat(isValid, is(UpdateResult.UPDATE_OK));
    assertThat(player.getShortRoomDescription(), is("This is AN new room."));

    // Get actual stored record for later comparison
    CaveStorage storage = objectManager.getCaveStorage();
    RoomRecord copyOfOriginal = new RoomRecord(storage.getRoom(player.getPosition()));

    // When: the player modifies the room to fix spelling
    UpdateResult status = player.updateRoom("This is A new room.");

    // Then: the update operation is valid, status OK
    assertThat(status, is(UpdateResult.UPDATE_OK));
    // and the description is updated
    assertThat(player.getShortRoomDescription(), is("This is A new room."));

    // Then also: the contents of the stored record in storage is
    // identical to the original except for the description
    RoomRecord copyOfNew = new RoomRecord(storage.getRoom(player.getPosition()));
    assertThat(copyOfNew.getDescription(), is(not(copyOfOriginal.getDescription())));
    assertThat(copyOfNew.getCreatorId(), is(copyOfOriginal.getCreatorId()));
    assertThat(copyOfNew.getCreationTimeISO8601(), is(copyOfOriginal.getCreationTimeISO8601()));
    assertThat(copyOfNew.getId(), is(copyOfOriginal.getId()));
  }

  @Test
  public void shouldNotAllowNonCreatorToUpdateRoomDescription() {
    // Given: Mikkel who has dug a new room
    UpdateResult notUsed = player.digRoom(Direction.DOWN, "This is AN new room.");
    // and mathilde going to that room
    Player mathilde =  HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
    mathilde.move(Direction.DOWN);
    assertThat(mathilde.getShortRoomDescription(), containsString("AN new room"));

    // When: Mathilde tries to change room description
    UpdateResult status = mathilde.updateRoom("This is utterly bullshit");
    // Then: the status code shows that she was not the creator of the room
    assertThat(status, is(UpdateResult.FAIL_AS_NOT_CREATOR));
  }

}
