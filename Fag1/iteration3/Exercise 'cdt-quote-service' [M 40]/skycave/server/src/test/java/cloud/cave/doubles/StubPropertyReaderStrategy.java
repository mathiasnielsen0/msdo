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

import cloud.cave.common.CaveConfigurationNotSetException;
import com.baerbak.cpf.PropertyReaderStrategy;

import java.util.*;

/**
 * Test stub (FRS, chapter 12) AND a spy (FRS, sidebar 12.1) for reading
 * properties and for verifying that the proper values are read.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StubPropertyReaderStrategy implements PropertyReaderStrategy {

  Map<String,String> properties;
  public StubPropertyReaderStrategy() {
    properties = new HashMap<>();
  }

  @Override
  public String getValue(String key) {
    // Verify that UnitUnderTest is trying to access the expected
    // property (Spy behavior)
    String expected = properties.get(key);
    if (expected == null) {
      throw new CaveConfigurationNotSetException("StubPropertyReaderStrategy: Unknown key="+key+" was attemted to be read.");
    }
    return expected;
  }


  public void setExpectation(String key, String value) {
    properties.put(key, value);
  }

}
