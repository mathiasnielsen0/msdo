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

import java.util.*;

import cloud.cave.common.MarshallingKeys;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import frds.broker.Invoker;
import frds.broker.ReplyObject;
import frds.broker.RequestObject;
import org.slf4j.*;

import cloud.cave.config.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Standard implementation of the Invoker role of the Broker pattern.
 *
 * Uses the MultiType dispatching technique - that is this invoker
 * just determine the sub invoker to use and then delegate to it.
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 *
 */
public class SkyCaveRootInvoker implements Invoker {
  private final Gson gson;
  private Logger logger;
  private Invoker subInvoker;
  
  private Map<String,Invoker> mapRole2SubInvoker;
  private ObjectManager objManager;

  /**
   * Create an invoker that dispatches requests using default dispatching, that
   * is the set of methods known in the initial release of SkyCave.
   * 
   * @param objectManager
   *          object manager that holds delegates to use
   */
  public SkyCaveRootInvoker(ObjectManager objectManager) {
    // Create the map that maps from class prefix to
    // dispatcher for that particular class type,
    // see FRDS §5.5 or
    // Reactor pattern (POSA p 259) and 'identifyDispather' method.
    mapRole2SubInvoker = new HashMap<String, Invoker>();
    mapRole2SubInvoker.put(MarshallingKeys.CAVE_TYPE_PREFIX, new CaveInvoker(objectManager));
    mapRole2SubInvoker.put(MarshallingKeys.PLAYER_TYPE_PREFIX, new PlayerInvoker(objectManager));
    initialize(objectManager, mapRole2SubInvoker);
    gson = new Gson();
  }

  /** initialize the invoker
   * @param objectManager 
   * 
   * @param mapRole2SubInvoker dispatcher map to use
   */
  private void initialize(ObjectManager objectManager, Map<String, Invoker> mapRole2SubInvoker) {
    this.objManager = objectManager;
    this.mapRole2SubInvoker = mapRole2SubInvoker;
    logger = LoggerFactory.getLogger(SkyCaveRootInvoker.class);
  }
  

  /**
   * Identify the subinvoker appropriate for the given method. Corresponds to
   * the identify_handler(event) in Reactor pattern.
   * <p>
   * Presently relies on mangled method names, i.e. that a method on the player
   * starts with 'play' and a method on cave starts with 'cave'.
   * 
   * @param methodKey
   *          key of the method, see MarshallingKeys
   * @return the appropriate dispatcher for the class containing that particular
   *         method or null if the method key is ill-formed
   */
  private Invoker identifySubInvoker(String methodKey) {
    Invoker dsp = null;
    int firstDash = methodKey.indexOf("-");
    String key = methodKey.substring(0, firstDash+1);
    dsp = mapRole2SubInvoker.get(key);
    return dsp;
  }

  @Override
  public String handleRequest(String request) {
    String reply = null;

    try {
      RequestObject requestObject =
              gson.fromJson(request, RequestObject.class);
      String operationName = requestObject.getOperationName();

      // Dispatch the event (POSA vol 4 Reactor code)
      subInvoker = identifySubInvoker(operationName);

      // We may get a null object back if the method key is ill formed
      // thus guard the dispatch call
      if (subInvoker != null) {
        // Next, do the dispatching - based upon the parameters, call
        // the proper method on the proper object
        reply = subInvoker.handleRequest(request);
      }
      // UNHANDLED METHOD
      if (reply == null) {
        ReplyObject replyObject = new ReplyObject(HttpServletResponse.SC_BAD_REQUEST,
                "SkyCaveRootInvoker.handleRequest: Unhandled request, method key '" + operationName +
                        "' is unknown. Full request=" + request);
        reply = gson.toJson(replyObject);

        logger.warn(replyObject.errorDescription());
      }
    } catch (StringIndexOutOfBoundsException exc ) {
      ReplyObject replyObject = new ReplyObject(HttpServletResponse.SC_BAD_REQUEST,
              "SkyCaveRootInvoker.handleRequest: Unhandled request, objectId is not correctly mangled. Full request=" + request);
      reply = gson.toJson(replyObject);
      logger.warn(replyObject.errorDescription());
    } catch (JsonSyntaxException exc ) {
      ReplyObject replyObject = new ReplyObject(HttpServletResponse.SC_BAD_REQUEST,
              "SkyCaveRootInvoker.handleRequest: Unhandled request, payload is not a RequestObject. Full request=" + request);
      reply = gson.toJson(replyObject);
      logger.warn(replyObject.errorDescription());
    }

    return reply;
  }
}
