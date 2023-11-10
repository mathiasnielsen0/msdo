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

package cloud.cave.extension;

import cloud.cave.server.common.*;
import cloud.cave.service.CaveStorage;

import java.util.Arrays;
import java.util.List;

/**
 * An implementation of a command that 'flies the player home' to the entry room
 * (0,0,0).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class HomeCommand extends AbstractCommand implements Command {

  @Override
  public List<String> execute(String... parameters) {
    Point3 home = new Point3(0, 0, 0);
    CaveStorage storage = objectManager.getCaveStorage();
    PlayerRecord pRecord = storage.getPlayerByID(playerID);

    // Update position in the storage
    pRecord.setPositionAsString(home.getPositionString());
    storage.updatePlayerRecord(pRecord);

    String reply = null;
    reply = "You went home to position "+home.getPositionString();
    return Arrays.asList(reply);
  }


}
