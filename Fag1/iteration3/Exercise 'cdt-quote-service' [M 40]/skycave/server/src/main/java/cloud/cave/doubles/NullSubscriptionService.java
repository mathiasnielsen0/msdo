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

package cloud.cave.doubles;

import cloud.cave.common.ServerConfiguration;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * A Null Object Subscription service; all logins are granted, no matter what
 * the credentials are.
 * <p>
 * Of course, this is NOT the implementation to use for production; but it is
 * ideal for load testing where a lot of users needs to be logged into the
 * server with as little fuss as possible.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class NullSubscriptionService implements SubscriptionService {
  
  public NullSubscriptionService() {
  }
  
  @Override
  public SubscriptionRecord authorize(String loginName, String password) {
    String playerID = "id-"+loginName;
    SubscriptionRecord record = new SubscriptionRecord(playerID, loginName, "ALL", Region.AALBORG);
    record.setAccessToken(playerID); // Not used, but now a value has been set.
    return record;
  }
  
  public String toString() {
    return "NullSubscriptionRecord";
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return null;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
  }

  @Override
  public void disconnect() {
    // No op
  }
}