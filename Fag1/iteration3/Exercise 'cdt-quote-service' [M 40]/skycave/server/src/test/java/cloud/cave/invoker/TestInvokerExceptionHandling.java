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

package cloud.cave.invoker;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.common.MarshallingKeys;
import cloud.cave.config.ObjectManager;
import com.google.gson.Gson;
import frds.broker.Invoker;
import frds.broker.ReplyObject;
import frds.broker.RequestObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

public class TestInvokerExceptionHandling {
  private ObjectManager objMgr;
  private Invoker invoker;
  private Gson gson;

  @BeforeEach
  public void setup() {
    objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    invoker = new SkyCaveRootInvoker(objMgr);
    gson = new Gson();
  }

  @Test
  public void shouldHandleParsingExceptionsInInvokers() {
    // Given a root invoker
    RequestObject request;
    ReplyObject reply;

    // When an objectId that is NOT following the mangling strategy is provided
    // Construct the request object

    request = new RequestObject("objectIdWithoutMangling",
            MarshallingKeys.GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY, "{}");
    String asJson = gson.toJson(request);
    String replyAsJson = invoker.handleRequest(asJson);
    reply = gson.fromJson(replyAsJson, ReplyObject.class);
    // Then a reply with error code BAD REQUEST
    assertThat(reply.getStatusCode(), is(HttpServletResponse.SC_BAD_REQUEST));
    assertThat(reply.errorDescription(), containsString("objectId is not correctly mangled"));

    // When method key is unknown
    request = new RequestObject("fisk##thingy",
            "weird_method", "{}");
    asJson = gson.toJson(request);
    replyAsJson = invoker.handleRequest(asJson);
    reply = gson.fromJson(replyAsJson, ReplyObject.class);
    // Then a reply with error code BAD REQUEST
    assertThat(reply.getStatusCode(), is(HttpServletResponse.SC_BAD_REQUEST));
    assertThat(reply.errorDescription(),
            containsString("Unhandled request, method key 'weird_method' is unknown"));

    // When request is not a request object
    replyAsJson = invoker.handleRequest("Mokka er en fin kat");
    reply = gson.fromJson(replyAsJson, ReplyObject.class);
    // Then a reply with error code BAD REQUEST
    assertThat(reply.getStatusCode(), is(HttpServletResponse.SC_BAD_REQUEST));
    assertThat(reply.errorDescription(),
            containsString("payload is not a RequestObject"));

    // When payload is not proper JSON
    request = new RequestObject("fisk##thingy",
            MarshallingKeys.DIG_ROOM_METHOD_KEY, "{ this is not proper json");
    asJson = gson.toJson(request);
    replyAsJson = invoker.handleRequest(asJson);
    reply = gson.fromJson(replyAsJson, ReplyObject.class);
    assertThat(reply.getStatusCode(), is(HttpServletResponse.SC_BAD_REQUEST));
    assertThat(reply.errorDescription(),
            containsString("payload is not a RequestObject"));
  }

}
