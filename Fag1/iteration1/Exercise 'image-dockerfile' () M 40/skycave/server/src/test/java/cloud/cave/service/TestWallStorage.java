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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import cloud.cave.doubles.FakeCaveStorage;
import cloud.cave.service.wall.MessageRecord;
import cloud.cave.server.common.Point3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.IntStream;


/** TDD of the wall aspect of FakeCaveStorage, which incidentally have become
 * quite complex. However, these test cases can server as integration
 * test cases for testing other CaveStorage implementations.
 */
public class TestWallStorage {
  public static final String COMPLETELY_NEW_MESSAGE = "Completely new message";
  private CaveStorage storage;
  String position123 = new Point3(1,2,3).getPositionString();

  @BeforeEach
  public void setUp() throws Exception {
    storage = new FakeCaveStorage();
    storage.initialize(null, null);
  }

  @AfterEach
  public void tearDown() {
    storage.disconnect();
  }

  // TDD of the wall/messages storage and retrieval
  @Test
  public void shouldAddAndReadMessages() {
    MessageRecord m1 = new MessageRecord("Message 1", "mikkel_aarskort", "Mikkel");
    MessageRecord m2 = new MessageRecord("Message 2", "mathilde_aarskort", "Mathilde");
    MessageRecord m3 = new MessageRecord("Message 3", "magnus_aarskort", "Magnus");

    storage.addMessage(position123, m1);
    storage.addMessage(position123, m2);
    storage.addMessage(position123, m3);

    // Get list of messages with NEWEST in location 0
    List<MessageRecord> list = storage.getMessageList(position123,0, 16);

    assertThat(list.get(2).getContents(), is(m1.getContents()));
    assertThat(list.get(2).getCreatorId(), is(m1.getCreatorId()));
    assertThat(list.get(2).getCreatorName(), is(m1.getCreatorName()));
    assertThat(list.get(2).getCreatorTimeStampISO8601(), is(not("none")));

    assertThat(list.get(1).getContents(), is(m2.getContents()));

    assertThat(list.get(0).getContents(), is(m3.getContents()));
    assertThat(list.get(0).getCreatorId(), is(m3.getCreatorId()));
    assertThat(list.get(0).getCreatorName(), is(m3.getCreatorName()));

    // No contents on a wall in a room where none have written anything (but not null!)
    String position321 = new Point3(3,2,1).getPositionString();
    List<MessageRecord> list321 = storage.getMessageList(position321,0, 16);

    assertThat(list321.size(), is(0));

    // Now post exactly the same as message m1 in room 3,2,1
    storage.addMessage(position321, m1);
    list321 = storage.getMessageList(position321,0, 16);

    assertThat(list321.size(), is(1));

    MessageRecord m1In123 =
            storage.getMessageList(position123, 0, 16).get(0);
    MessageRecord m1In321 =
            storage.getMessageList(position321, 0, 16).get(0);

    // assert that it is the storage system, not the MessageRecord that assigns unique ids
    assertThat(m1In321.getId(), not(is(m1In123.getId())));
  }

  @Test
  public void shouldReadPaginated() {
    String position123 = create100MessagesAtPosition123();

    // Again, NEWEST messages first
    List<MessageRecord> list = storage.getMessageList(position123,0, 16);
    assertThat(list.size(), is(16));
    assertThat(list.get(0).getContents(), is("M 99"));
    assertThat(list.get(15).getContents(), is("M 84"));

    list = storage.getMessageList(position123,3*16, 16);
    assertThat(list.size(), is(16));
    assertThat(list.get(0).getContents(), is("M " + (99 - 3*16)));
    assertThat(list.get(15).getContents(), is("M " + (99 - 3*16 - 15)));

    list = storage.getMessageList(position123,97, 10);
    assertThat(list.size(), is(3));
    assertThat(list.get(2).getContents(), is("M 0"));

    list = storage.getMessageList(position123,100, 10);
    assertThat(list.size(), is(0));

  }

  public String create100MessagesAtPosition123() {
    for (int i = 0; i < 100; i++) {
      MessageRecord m1 = new MessageRecord("M " + i, "mikkel_aarskort", "Mikkel");
      storage.addMessage(position123, m1);
    }
    return position123;
  }

  @Test
  public void shouldUpdateMessageIfExistingAndCreator() {
    create100MessagesAtPosition123();
    List<MessageRecord> list = storage.getMessageList(position123,2*7, 7);
    assertThat(list.size(), is(7));

    MessageRecord msg3OnPage2 = new MessageRecord(list.get(3));
    assertThat(msg3OnPage2.getContents(), is("M "+ (99-2*7-3)));

    // Mikkel overwrites a message that he himself has created
    MessageRecord newMsg = new MessageRecord(COMPLETELY_NEW_MESSAGE, "mikkel_aarskort", "Mikkel");
    int status = storage.updateMessage(position123, msg3OnPage2.getId(), newMsg);
    assertThat(status, is(HttpServletResponse.SC_OK));

    // retrieve the list again
    list = storage.getMessageList(position123,2*7, 7);
    assertThat(list.size(), is(7));
    assertThat(list.get(2).getContents(), is("M "+ (99-2*7-2)));
    assertThat(list.get(4).getContents(), is("M "+ (99-2*7-4)));

    // Assert message is overwritten, but other attributes are kept
    assertThat(list.get(3).getContents(), is(COMPLETELY_NEW_MESSAGE));
    assertThat(list.get(3).getCreatorId(), is(msg3OnPage2.getCreatorId()));
    assertThat(list.get(3).getId(), is(msg3OnPage2.getId()));
    assertThat(list.get(3).getCreatorName(), is(msg3OnPage2.getCreatorName()));
    assertThat(list.get(3).getCreatorTimeStampISO8601(), is(msg3OnPage2.getCreatorTimeStampISO8601()));
  }

  @Test
  public void shouldNotChangeNonExistingMessage() {
    create100MessagesAtPosition123();

    MessageRecord newMsg = new MessageRecord(COMPLETELY_NEW_MESSAGE, "mikkel_aarskort", "Mikkel");
    int status = storage.updateMessage(position123, "fisk", newMsg);
    assertThat(status, is(HttpServletResponse.SC_NOT_FOUND));
    assertThatAllWallMessagesAreUnaltered();

  }

  public void assertThatAllWallMessagesAreUnaltered() {
    // Assert that all messages are intact by finding all that match their
    // expected contents,
    List<MessageRecord> all = storage.getMessageList(position123, 0, 100);
    long numElements = IntStream.range(0, all.size())
            .filter(i -> all.get(i).getContents().equals("M " + (99-i) ))
            .count();
    // and verify that it is indeed them all
    assertThat(numElements, is(100L));
  }

  @Test
  public void shouldNotChangeMessageIfNotCreator() {
    create100MessagesAtPosition123();
    List<MessageRecord> list = storage.getMessageList(position123,2*7, 7);
    MessageRecord msg3OnPage2 = list.get(3);

    // Magnus creates a correction to message contents and tries to overwrite
    MessageRecord newMsg = new MessageRecord(COMPLETELY_NEW_MESSAGE, "magnus_aarskort", "Magnus");
    int status = storage.updateMessage(position123, msg3OnPage2.getId(), newMsg);
    assertThat(status, is(HttpServletResponse.SC_UNAUTHORIZED));
  }
}
