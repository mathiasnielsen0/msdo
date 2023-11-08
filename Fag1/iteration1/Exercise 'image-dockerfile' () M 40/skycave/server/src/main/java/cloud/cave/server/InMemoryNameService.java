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

import java.util.*;

import cloud.cave.common.ServerConfiguration;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.*;

/**
 * Implementation of the player name service using in-memory Map data
 * structures.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class InMemoryNameService implements PlayerNameService {

  private Map<String,Player> nameServiceMap;

  public InMemoryNameService() {
    nameServiceMap = new HashMap<>();
  }

  @Override
  public Player get(String playerID) {
    Player p = nameServiceMap.get(playerID);
    return p;
  }

  @Override
  public void add(String playerID, Player player) {
    nameServiceMap.put(playerID, player);
  }

  @Override
  public void remove(String playerID) {
    nameServiceMap.remove(playerID);
  }

  @Override
  public String toString() {
    return "InMemoryNameService";
  }

  // === ExternalService handling
  private ServerConfiguration serverConfiguration;

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.serverConfiguration = config;
  }

  @Override
  public void disconnect() {
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return serverConfiguration;
  }
}
