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
 * A command pattern based implementation of a jump command, that allows a
 * player to instantly move to a specific room.
 *
 * Expects a single parameter in the () format like "(0,1,0)".
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class JumpCommand extends AbstractCommand implements Command {

  @Override
  public List<String> execute(String... parameters) {
    String positionString = parameters[0];
    
    CaveStorage storage = objectManager.getCaveStorage();

    // Validate that the position is known in the cave
    RoomRecord room = storage.getRoom(positionString);
    if ( room == null ) {
      return Arrays.asList("JumpCommand failed, room "
          + positionString + " does not exist in the cave.");
    }
    
    PlayerRecord pRecord = storage.getPlayerByID(playerID);

    // Update the position in storage
    pRecord.setPositionAsString(positionString);
    storage.updatePlayerRecord(pRecord);

    String reply = "You jumped to position: "+positionString;

    return Arrays.asList(reply);
  }

}
