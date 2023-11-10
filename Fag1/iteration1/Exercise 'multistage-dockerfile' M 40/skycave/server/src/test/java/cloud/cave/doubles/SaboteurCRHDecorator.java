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

import cloud.cave.invoker.CaveIPCException;
import frds.broker.ClientRequestHandler;
import frds.broker.ReplyObject;
import frds.broker.RequestObject;

/**
 * A Saboteur (Meszaros, 2007) test double, i.e. a test double that tries to
 * sabotage an operation - here simulating network connection exceptions.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class SaboteurCRHDecorator implements ClientRequestHandler {

  private ClientRequestHandler decoratee;
  private String exceptionMsg;
  
  public SaboteurCRHDecorator(ClientRequestHandler decoratee) {
    this.decoratee = decoratee;
    exceptionMsg = null;
  }

  public void throwNextTime(String exceptionMessage) {
    exceptionMsg = exceptionMessage;
  }

  @Override
  public String sendToServerAndAwaitReply(String request) {
    if ( exceptionMsg != null ) { throw new CaveIPCException(exceptionMsg, null); }
    return decoratee.sendToServerAndAwaitReply(request);
  }

  @Override
  public void setServer(String hostname, int port) {
    // not used
  }

  @Override
  public void close() {
    // not used
  }
}
