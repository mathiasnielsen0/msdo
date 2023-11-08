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

package cloud.cave.server.common;

import cloud.cave.config.ObjectManager;

import java.util.List;

/**
 * The Command role in the Command pattern (Flexible Reliable Software, p 308.)
 * <p>
 * An instance of Player may take an instance of a command and execute it.
 * <p>
 * Any instance of this interface will be executed as a 'template method' call
 * where the methods 'setPlayerID' and 'setStorageService' has already been
 * called before the 'execute' method is invoked.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public interface Command {

  /**
   * This method is invoked just before the 'execute' method and defines the ID
   * of the player executing the command
   * 
   * @param playerID
   *          id of the player
   */
  void setPlayerID(String playerID);

  /**
   * This method is invoked just before the 'execute' method and defines the
   * object manager used by the SkyCave.
   * 
   * @param objMgr
   *          the object manager
   */
  void setObjectManager(ObjectManager objMgr);

  /**
   * Perform the execution of the command instance.
   * 
   * @param parameters
   *          a variable length list of string parameters to be interpreted by
   *          the actual command
   * @return a string that is formatted as a JSON object = the return value of
   *          executing the command.
   */
  List<String> execute(String... parameters);

}
