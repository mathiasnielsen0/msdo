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

import cloud.cave.common.ServerConfiguration;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.*;
import cloud.cave.service.ExternalService;

/**
 * The role of name service for the player object.
 * <p>
 * It is an ExternalService in preparation for implementing
 * a distributed name service.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface PlayerNameService extends ExternalService {

  /**
   * Get the player object corresponding to the given player id.
   * 
   * @param playerID
   *          the id of the player
   * @return null if no player id is stored in the name service, otherwise the player
   *         object
   */
  Player get(String playerID);

  /**
   * Add a player instance under the given player id
   * 
   * @param playerID
   *          id to store player instance under
   * @param player
   *          the player instance to add to service
   */
  void add(String playerID, Player player);

  /**
   * Remove the player instance for the given player id from the cache
   * 
   * @param playerID
   *          player id of player instance to remove
   */
  void remove(String playerID);

  /**
   * Initialize the name service. Must be run before any other method.
   * 
   * @param objectManager
   *          the object manager of SkyCave
   * 
   * @param config
   *          the configuration of the name service if using external services
   * 
   */
  void initialize(ObjectManager objectManager, ServerConfiguration config);

  /** Get the configuration of this cache.
   * 
   * @return the configuration
   */
  ServerConfiguration getConfiguration();
}
