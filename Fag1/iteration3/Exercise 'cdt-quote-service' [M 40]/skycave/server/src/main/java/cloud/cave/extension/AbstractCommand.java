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

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.Command;
import com.google.gson.Gson;

/**
 * Abstract base class for command instances. Simply stores the playerID and
 * storage service reference, and provides a Gson instance for JSON handling.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public abstract class AbstractCommand implements Command {

  protected final Gson gson;
  protected ObjectManager objectManager;
  protected String playerID;

  public AbstractCommand() {
    super();
    gson = new Gson();
  }

  @Override
  public void setObjectManager(ObjectManager storage) {
    this.objectManager = storage;
  }

  @Override
  public void setPlayerID(String playerID) {
    this.playerID = playerID;
  }

}