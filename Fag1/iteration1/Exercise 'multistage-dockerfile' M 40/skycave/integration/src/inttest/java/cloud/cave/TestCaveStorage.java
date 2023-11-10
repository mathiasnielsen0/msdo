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

package cloud.cave;

import cloud.cave.server.common.Point3;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.service.CaveStorage;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
public class TestCaveStorage {
  private CaveStorage storage;

  // TODO: Exercise 'integration-redis-connect' - start a redis container
  // using the TestContainers system

  // TODO: Uncomment and complete the container specification below
  // @Container
  // GenericContainer redis = [create the container]

  @BeforeEach
  public void setup() {
    // TODO: Get the host + port of the started redis container, and
    // configure your 'RedisCaveStorageConnector' to interact with that
  }

  @Test
  public void shouldTestSomethingBasedUponRunningServices() {
    // Demonstrate that even though this test is obviously
    // failing (storage==null), it is NOT executed as part of 'gradle test'
    // and thus does NOT invalidate normal unit testing.

    // You explicitly have to execute it as an integration
    // test (gradle itest) and add it (later) to your
    // build pipeline.


    // Given position 3-8-9
    Point3 position = new Point3(3,8,9);
    // And given a room record
    RoomRecord record = new RoomRecord("This is the JuleRoom", "MyCaveId57");
    // When I add a record
    storage.addRoom(position.getPositionString(), record);
    // Then I can retrieve it again
    RoomRecord retrieved = storage.getRoom(position.getPositionString());
    assertThat(retrieved.getDescription(), is("This is the JuleRoom"));
    // TODO assert other attributes of the record
  }

  // TODO - make a complete test suite on the CaveStorage connector interface
}

