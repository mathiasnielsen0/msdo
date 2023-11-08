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

package cloud.cave.service;

import cloud.cave.doubles.TestStubSubscriptionService;
import cloud.cave.server.common.SubscriptionRecord;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A set of Learning Tests to demonstrate formats and encodings surrounding the authorization in SkyCave.
 */
public class TestAuthorization {
  @Test
  public void shouldDemonstrateAuthorization() {
    SubscriptionService ss = new TestStubSubscriptionService();
    SubscriptionRecord sr = ss.authorize("mathilde_aarskort", "333");
    Gson gson = new Gson();
    String asJSON = gson.toJson(sr);
    // System.out.println(asJSON);
    assertThat(asJSON, containsString("region\":\"AALBORG"));

    sr = ss.authorize("bimse", "wrongpwd");
    asJSON = gson.toJson(sr);
    // System.out.println(asJSON);
    assertThat(asJSON, is("{\"httpStatusCode\":401}"));
  }

  @Test
  public void shouldDemonstrateBase64Encoding() {
    /*
    In the context of an HTTP transaction, basic access authentication is a method for an
    HTTP user agent (e.g. a web browser) to provide a user name interface and password
    when making a request.
    In basic HTTP authentication, a request contains a header field in the form of
    Authorization: Basic <credentials> credentials is the
    Base64 encoding of ID and password joined by a single colon
     */
    String idpwd = "mathilde_aarskort" + ":" + "333";
    String encodedString = Base64.getEncoder().encodeToString(idpwd.getBytes(StandardCharsets.UTF_8));
    assertThat(encodedString, is("bWF0aGlsZGVfYWFyc2tvcnQ6MzMz"));

    byte[] decodedByteArray = Base64.getDecoder().decode(encodedString);
    String decodedString = new String(decodedByteArray, StandardCharsets.UTF_8);
    assertThat(decodedString, is(idpwd));
  }
}
