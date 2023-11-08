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

package cloud.cave.config;

import cloud.cave.common.ServerConfiguration;

class StdFiskService implements FiskService {
  @Override
  public String sayFisk() {
    return "fisk";
  }
  @Override
  public void initialize(ObjectManager objectManager, ServerConfiguration config) {}
  @Override
  public void disconnect() {}
  @Override
  public ServerConfiguration getConfiguration() {
    return null;
  }
}
