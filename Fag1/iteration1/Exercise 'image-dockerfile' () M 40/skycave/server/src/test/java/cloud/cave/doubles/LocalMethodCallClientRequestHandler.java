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

import com.google.gson.Gson;
import frds.broker.ClientRequestHandler;
import frds.broker.Invoker;
import frds.broker.ReplyObject;
import frds.broker.RequestObject;

/** A fake object + Spy client request handler that avoids any
 * IPC and instead calls the server side invoker directly.
 */

public class LocalMethodCallClientRequestHandler implements ClientRequestHandler {
  private final Invoker invoker;

  private RequestObject lastSentRequestObject;
  private ReplyObject lastRecievedReplyObject;
  private Gson gson;

  public LocalMethodCallClientRequestHandler(Invoker invoker) {
    this.invoker = invoker;
    gson = new Gson();
  }

  @Override
  public String sendToServerAndAwaitReply(String request) {
    RequestObject requestObject =
            gson.fromJson(request, RequestObject.class);
    // System.out.println("--> FRDS CRH: "+ requestObject);
    lastSentRequestObject = requestObject;
    String reply = invoker.handleRequest(request);
    // System.out.println("<-- FRDS CRH: "+ reply);
    lastRecievedReplyObject = gson.fromJson(reply, ReplyObject.class);
    return reply;
  }

  @Override
  public void setServer(String hostname, int port) {
    // not used
  }

  @Override
  public void close() {
    // not applicable
  }

  public RequestObject lastSentRequestObject() {
    return lastSentRequestObject;
  }
}
