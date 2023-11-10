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

import cloud.cave.common.Config;
import org.junit.jupiter.api.Test;

/** TDD of the Config's ability to generate the
 * 'proper' filename for cpf loading.
 *
 */
public class TestCPFReading {
  @Test
  public void shouldPrependCPFFolderIfJustABaseName() {
    assertThat(Config.prependDefaultFolderForNonPathFilenames("http.cpf"),
            is(Config.CPF_RESOURCE_FOLDER+"/http.cpf"));
  }

  @Test
  public void shouldPrependNothingIfAPathIsIncluded() {
    assertThat(Config.prependDefaultFolderForNonPathFilenames("opt/quote-service.cpf"),
            is("opt/quote-service.cpf"));
  }
  @Test
  public void shouldPrependNothingIfAbsoluteName() {
    assertThat(Config.prependDefaultFolderForNonPathFilenames("/root/opt/quote-service.cpf"),
            is("/root/opt/quote-service.cpf"));
  }
}
