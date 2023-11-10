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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.service.ExternalService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

/** The standard object manager can handle lazy creation
 * of external services which are not known a priori.
 */

public class TestStandardObjectManagerGenericServiceHandling {

  public static final String SKYCAVE_FISKSERVICE = "SKYCAVE_FISKSERVICE";

  @Test
  public void shouldCreateServiceConnectorOfUnknownType() {
    // Given a factory that can create FiskService instances
    CaveServerFactory factory = new AllTestDoubleFactoryWithFiskServiceCreationAbility();

    // When a standard object manager is asked for the fisk service instance
    ObjectManager objMgr
            = new StandardObjectManager(factory);
    FiskService fiskService = null;
    fiskService = objMgr.getServiceConnector(FiskService.class, SKYCAVE_FISKSERVICE);
    // Then it is correctly configured
    assertThat(fiskService, is(notNullValue()));
    assertThat(fiskService.sayFisk(), is("fisk"));

    // ... and we get the same instance every time
    FiskService service2 = objMgr.getServiceConnector(FiskService.class, SKYCAVE_FISKSERVICE);
    assertThat(fiskService, org.hamcrest.CoreMatchers.is(service2));
  }

  private class AllTestDoubleFactoryWithFiskServiceCreationAbility extends AllTestDoubleFactory {
    @Override
    public ExternalService createServiceConnector(Type interfaceType, String propertyKeyPrefix, ObjectManager objectManager) {
      if (propertyKeyPrefix.equals(SKYCAVE_FISKSERVICE)) {
        FiskService fiskService = new StdFiskService();
        fiskService.initialize(objectManager, null);
        return fiskService;
      }
      return super.createServiceConnector(interfaceType, propertyKeyPrefix, objectManager);
    }
  }
}

