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

import java.util.*;

import cloud.cave.common.ServerConfiguration;
import org.mindrot.jbcrypt.BCrypt;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * A test stub implementation of the subscription storage. It initially knows
 * only three loginNames, and their associated passwords and playerNames.
 * <p>
 * Note that the implementation here does NOT store passwords but uses jBCrypt
 * to store password hashes, which is a standard technique to guard
 * passwords safely even if a database's contents is stolen
 *  
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestStubSubscriptionService implements SubscriptionService {

  private final Logger logger;
  private static int accessToken = 0;

  public TestStubSubscriptionService() {
    super();
    subscriptionMap = new HashMap<String,SubscriptionPair>();
    // populate with the three users known by all test cases
    subscriptionMap.put(TestConstants.MIKKEL_AARSKORT,
        new SubscriptionPair(TestConstants.MIKKEL_PASSWORD,
        new SubscriptionRecord("user-001","Mikkel", "grp01", Region.AARHUS)));
    subscriptionMap.put(TestConstants.MAGNUS_AARSKORT,
        new SubscriptionPair(TestConstants.MAGNUS_PASSWORD,
        new SubscriptionRecord("user-002","Magnus", "grp01", Region.COPENHAGEN)));
    subscriptionMap.put(TestConstants.MATHILDE_AARSKORT,
        new SubscriptionPair(TestConstants.MATHILDE_PASSWORD,
        new SubscriptionRecord("user-003","Mathilde", "grp02", Region.AALBORG)));
    // and populate with a single 'reserved' user which is used by the
    // course's automatic testing system. Leave this reserved login
    // in the test stub because otherwise our grading system will not
    // pass its tests and then you will not get the proper points for
    // your score. The reserved user is not used by any of the test
    // cases.
    subscriptionMap.put("reserved_aarskort", 
        new SubscriptionPair("cloudarch",
        new SubscriptionRecord("user-reserved","ReservedCrunchUser", "zzz0", Region.AARHUS)));
    logger = LoggerFactory.getLogger(TestStubSubscriptionService.class);
  }

  private class SubscriptionPair {
    public SubscriptionPair(String password, SubscriptionRecord record) {
      String salt = BCrypt.gensalt(4); // Preferring faster over security
      String hash = BCrypt.hashpw(password, salt);
      
      this.bCryptHash = hash;
      this.subscriptionRecord = record;
    }
    public String bCryptHash;
    public SubscriptionRecord subscriptionRecord;
  }
  
  /** A database 'table' that has loginName as primary key (key)
   * and the subscription record as value.
   */
  private Map<String, SubscriptionPair> subscriptionMap;
  private ServerConfiguration configuration;

  @Override
  public SubscriptionRecord authorize(String loginName, String password) {
    // The OAuth protocol this mimics is the combined GET /authorize and POST /token
    // which returns a access token. However, we in addition return a record
    // detailing the subscription: player name, group name, etc.
    SubscriptionPair pair = subscriptionMap.get(loginName);

    // Verify that loginName+pwd match a valid subscription
    if (pair == null || 
        ! BCrypt.checkpw(password, pair.bCryptHash)) { 
      return new SubscriptionRecord(HttpServletResponse.SC_UNAUTHORIZED);
    }

    String newToken = "token#"+accessToken++;
    pair.subscriptionRecord.setAccessToken(newToken);
    
    return pair.subscriptionRecord;
  }
  
  public String toString() {
    return "TestStubSubscriptionService (Only knows three fixed testing users)";
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void initialize(ObjectManager objMgr, ServerConfiguration config) {
    this.configuration = config;
  }

  @Override
  public void disconnect() {
    // No op
  }
}