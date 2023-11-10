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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import cloud.cave.common.HelperMethods;
import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.config.ObjectManager;
import cloud.cave.doubles.TestConstants;

import java.util.List;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Initial template of TDD of students' exercises
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestWall {

  private ObjectManager objMgr;
  private Cave cave;
  private Player player;

  @BeforeEach
  public void setUp() throws Exception {
    objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

    cave = objMgr.getCave();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  @Test
  public void shouldAddAndReadWallMessages() {
    // Add four messages at various times in the past
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(14, objMgr);
    player.addMessage("This is message 1");
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(8, objMgr);
    player.addMessage("This is message 2");
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(2, objMgr);
    player.addMessage("This is message 3");
    Player player2 = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(0, objMgr);
    player2.addMessage("This is message 4");

    List<WallMessageDataTransferObject> msg = player.getMessageList(0);

    // Debug: msg.stream().forEach(System.out::println);

    // Validate that newest message is at location 0
    assertThat(msg.get(0).getMessage(), is("[Mathilde, just now] This is message 4"));
    assertThat(msg.get(1).getMessage(), is("[Mikkel, 2 minutes ago] This is message 3"));
    assertThat(msg.get(2).getMessage(), is("[Mikkel, 8 minutes ago] This is message 2"));
    assertThat(msg.get(3).getMessage(), is("[Mikkel, 14 minutes ago] This is message 1"));
  }

  @Test
  public void shouldHandlePaginationSoPage0IsNewest() {
    addFiftyMessagesToWall();

    // This test case unfortunately has a strong coupling to the value of the
    // WALL_PAGE_SIZE constant
    List<WallMessageDataTransferObject> msg = player.getMessageList(0);
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 1 minutes ago] Message: 49"));
    assertThat(msg.get(PlayerServant.WALL_PAGE_SIZE - 1).getMessage(), is("[Mikkel, 8 minutes ago] Message: 42"));

    msg = player.getMessageList(1);
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 9 minutes ago] Message: 41"));
    assertThat(msg.get(PlayerServant.WALL_PAGE_SIZE - 1).getMessage(), is("[Mikkel, 16 minutes ago] Message: 34"));

    msg = player.getMessageList(2);
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 17 minutes ago] Message: 33"));
    assertThat(msg.get(PlayerServant.WALL_PAGE_SIZE - 1).getMessage(), is("[Mikkel, 24 minutes ago] Message: 26"));

    msg = player.getMessageList(6);
    assertThat(msg.size(), is(2));
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 49 minutes ago] Message: 1"));
    assertThat(msg.get(1).getMessage(), is("[Mikkel, 50 minutes ago] Message: 0"));

  }

  public void addFiftyMessagesToWall() {
    for (int i = 0; i < 50; i++) {
      // add messages in chronological order
      HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(50-i, objMgr);
      player.addMessage("Message: " +i);
    }
  }

  @Test
  public void shouldHandleOutOfBoundsWallPages() {
    List<WallMessageDataTransferObject> msg = player.getMessageList(0);
    assertThat(msg.size(), is(0)); // never get a null list back

    msg = player.getMessageList(16254);
    assertThat(msg.size(), is(0)); // nothing on page 16254
  }

  @Test
  public void shouldHaveUniqueIdOnEachWallMessage() {
    addFiftyMessagesToWall();
    List<WallMessageDataTransferObject> msg = player.getMessageList(0);
    String uid00 = msg.get(0).getId();
    assertThat(uid00, is(not("none")));
    String uid01 = msg.get(1).getId();
    assertThat(uid01, is(not("none")));
    assertThat(uid00, is(not(uid01)));
  }

  @Test
  public void shouldEditWallMessage() {
    addFiftyMessagesToWall();
    List<WallMessageDataTransferObject> msg = player.getMessageList(0);
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 1 minutes ago] Message: 49"));
    String messageId = msg.get(0).getId();

    // Update the message
    UpdateResult statusCode = player.updateMessage(messageId, "Message: edited");
    assertThat(statusCode, is(UpdateResult.UPDATE_OK));

    msg = player.getMessageList(0);
    // Message is changed but not the timestamp
    assertThat(msg.get(0).getMessage(), is("[Mikkel, 1 minutes ago] Message: edited"));
    // Nor the id
    assertThat(msg.get(0).getId(), is(messageId));
  }

  @Test
  public void shouldNotEditNonAuthoredWallMessage() {
    // Given: Two messages by two authors
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(2, objMgr);
    player.addMessage("This is message 1 two minutes ago");

    Player playerMathilde = HelperMethods.loginPlayer(cave, TestConstants.MATHILDE_AARSKORT);
    HelperMethods.configureFakeStorageToSetTimeNMinutesAgo(1, objMgr);
    playerMathilde.addMessage("This is message 2 one minute ago");

    List<WallMessageDataTransferObject> msg = player.getMessageList(0);
    assertThat(msg.size(), is(2));

    assertThat(msg.get(1).getMessage(), containsString("Mikkel")); // Author

    // When: Mathilde tries to edit Mikkel's wall message
    UpdateResult statusCode = playerMathilde.updateMessage(msg.get(1).getId(), "Overwriting contents");

    // Then: the status code is 'Unauthorized' and the message is not changed
    assertThat(statusCode, is(UpdateResult.FAIL_AS_NOT_CREATOR));

    msg = player.getMessageList(0);
    assertThat(msg.get(1).getMessage(), containsString("This is message 1 two minutes ago"));

  }
}
