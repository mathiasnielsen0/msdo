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

import frds.broker.ClientRequestHandler;
import frds.broker.Invoker;
import frds.broker.ReplyObject;
import frds.broker.RequestObject;

import cloud.cave.config.*;

/**
 * A client request handler that is pre-configured to abstract
 * a server completely away, and replace everything by fake objects
 * and test doubles. Is used in the Cmd tool.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class AllTestDoubleClientRequestHandler implements ClientRequestHandler {

  private Invoker invoker;

  public AllTestDoubleClientRequestHandler() {
    CaveServerFactory factory = new AllTestDoubleFactory();
    ObjectManager objMgr = new StandardObjectManager(factory);
    invoker = objMgr.getInvoker();
  }

  public String toString() {
    return "TestDoubleClientRequestHandler: Configured with pure test doubles for all server side abstractions.";
  }

  @Override
  public String sendToServerAndAwaitReply(String request) {
    // System.out.println("--> FRDS CRH: "+ request);
    String reply = invoker.handleRequest(request);
    // System.out.println("<-- FRDS CRH: "+ reply);
    return reply;
  }

  @Override
  public void setServer(String hostname, int port) {
    // not used...
  }

  @Override
  public void close() {

  }
}
