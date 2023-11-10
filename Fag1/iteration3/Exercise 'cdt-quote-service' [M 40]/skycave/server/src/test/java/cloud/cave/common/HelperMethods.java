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

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Player;
import cloud.cave.doubles.FakeCaveStorage;
import cloud.cave.doubles.FixedNowStrategy;
import cloud.cave.doubles.TestConstants;

import java.time.ZonedDateTime;

public class HelperMethods {

  /** as we login the stub players over and over again in the test code, we
   * 'do not repeat ourselves'.
   * @param cave the cave instance to make the login into
   * @param loginName login name
   * @return a stub player
   */
  public static Player loginPlayer(Cave cave, String loginName) {
    String password = "unknown";
    if (loginName.equals(TestConstants.MIKKEL_AARSKORT)) password = TestConstants.MIKKEL_PASSWORD;
    if (loginName.equals(TestConstants.MAGNUS_AARSKORT)) password = TestConstants.MAGNUS_PASSWORD;
    if (loginName.equals(TestConstants.MATHILDE_AARSKORT)) password = TestConstants.MATHILDE_PASSWORD;
    Player player = cave.login(loginName, password);
    return player;
  }

  // Helper method to set the 'clock back in time' for the time stamping
  // strategy regarding the wall postings for the FakeStorage ONLY
  public static void configureFakeStorageToSetTimeNMinutesAgo(int value, ObjectManager objectManager) {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime sevenMinAgo = now.minusMinutes(value);
    FixedNowStrategy stubNowStragegy = new FixedNowStrategy(sevenMinAgo);

    FakeCaveStorage fakeStorage = (FakeCaveStorage) objectManager.getCaveStorage();
    fakeStorage.setNowStrategy(stubNowStragegy);
  }

}
