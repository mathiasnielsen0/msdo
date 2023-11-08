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

package cloud.cave.userinterface;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import cloud.cave.common.CommonClientCaveTest;
import cloud.cave.common.HelperMethods;
import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.domain.Cave;
import cloud.cave.domain.UpdateResult;
import cloud.cave.doubles.TestConstants;

import cloud.cave.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testing of the wall behavior on the client side.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class TestWallClient {

  private Player player;
  private Cave cave;

  @BeforeEach
  public void setup() {
    cave = CommonClientCaveTest.createCaveProxyForTesting();
    player = HelperMethods.loginPlayer(cave, TestConstants.MIKKEL_AARSKORT);
  }

  // TODO: Exercise - solve the 'wall-client' exercise
  @Test
  public void shouldWriteToAndReadWall() {
    player.addMessage("This is message no. 1");
    List<WallMessageDataTransferObject> wallContents = player.getMessageList(0);
    assertThat(wallContents.size(), is(1));
    assertThat(wallContents.get(0).getMessage(), containsString("[Mikkel, just now] This is message no. 1"));
  }

  @Test
  public void shouldIncreaseMessageListSizeOnAddMessageOnWall() {
    List<WallMessageDataTransferObject> initialWallContents = player.getMessageList(0);
    player.addMessage("This is message no. 1");
    player.addMessage("This is message no. 2");
    List<WallMessageDataTransferObject> wallContents = player.getMessageList(0);
    assertThat(wallContents.size(), is(initialWallContents.size() + 2));
    assertThat(wallContents.get(0).getMessage(), containsString("[Mikkel, just now] This is message no. 2"));
  }


  @Test
  public void shouldHandleOutOfBoundsWallPages() {
    try {
      List<WallMessageDataTransferObject> initialWallMessages = player.getMessageList(-715);
    } catch (Exception e) {
      assertThat(e, instanceOf(IndexOutOfBoundsException.class));
    }
  }

  @Test
  public void shouldEditWallMessage() {
    player.addMessage("This is the initial message");
    List<WallMessageDataTransferObject> initialWallMessages = player.getMessageList(0);
    WallMessageDataTransferObject initialWallMessage = initialWallMessages.get(0);
    assertThat(initialWallMessage.getMessage(), containsString("[Mikkel, just now] This is the initial message"));

    player.updateMessage(initialWallMessage.getId(), "This is the updated message");
    List<WallMessageDataTransferObject> updatedWallMessages = player.getMessageList(0);
    WallMessageDataTransferObject updatedWallMessage = updatedWallMessages.get(0);
    assertThat(updatedWallMessage.getMessage(), containsString("[Mikkel, just now] This is the updated message"));

    assertThat(updatedWallMessages.size(), is(initialWallMessages.size()));
  }
}