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

import cloud.cave.client.CaveProxy;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import frds.broker.ClientRequestHandler;
import frds.broker.Invoker;
import frds.broker.Requestor;
import frds.broker.marshall.json.StandardJSONRequestor;

public class CommonClientCaveTest {
  /** Create a CaveProxy bound to all-test-doubled
   * cave servant via a local method call IPC broker layer.
   * @return cave proxy with all test doubles serving
   * server side roles
   */
  public static Cave createCaveProxyForTesting() {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

    Invoker invoker = objMgr.getInvoker();

    ClientRequestHandler crh = new LocalMethodCallClientRequestHandler(invoker);
    Requestor requestor = new StandardJSONRequestor(crh);

    // Create the cave proxy
    return new CaveProxy(requestor);
  }

}
