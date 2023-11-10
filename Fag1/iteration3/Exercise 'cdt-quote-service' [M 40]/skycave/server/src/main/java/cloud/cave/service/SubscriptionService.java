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

import cloud.cave.server.common.SubscriptionRecord;

/**
 * Interface for the service that handles authentication of
 * players wanting to enter the cave.
 *
 * It is loosely modelled over the OAuth 2 protocol but
 * simplified and adapted, as SkyCave is not a web based
 * system. Simplifications entail:
 *
 * a) GET /authorize and POST /token are merged into
 * a single operation 'authorize()' which returns
 * the access token/access token along with player information
 * in the SubscriptionRecord.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface SubscriptionService extends ExternalService {

  /**
   * Authorize the given loginName and with the provided
   * password.
   * <p>
   * Note: A subscription is ALWAYS returned, it is the error code of the
   * subscription object that defines rejected authorizations.
   * 
   * @param loginName
   *          login name of the player to lookup
   * @param password
   *          the password of the player
   * @return a record type with the properties of the subscription,
   * may be a rejected authorization which must be determined by
   * the error code of it.
   */
  SubscriptionRecord authorize(String loginName, String password);
}
