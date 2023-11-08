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

package cloud.cave.common;

/** Utility functions and constants for marshalling.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class Marshalling {
  
  /** Version of the current marshaling */
  public static final int MARSHALING_VERSION = 5;

  public static final String MANGLING_SEPARATOR = "##";

  /**
   * Use name mangling to produce an objectId
   * that contains two pieces of information in one
   * string. If you change the format, be sure to
   * fix the demangling method as well in the invoker
   * code!
   *
   * @param playerId  player id
   * @param accessToken access token of player
   * @return a mangled string, containing both information
   */
  public static String manglePlayerIDAndAccessToken(String playerId, String accessToken) {
    return playerId + MANGLING_SEPARATOR + accessToken;
  }

  public static String[] demanglePlayerIDAndAccessToken(String objectId) {
    String[] parts = new String[2];
    int indexOfSeparator = objectId.indexOf(MANGLING_SEPARATOR);
    parts[0] = objectId.substring(0, indexOfSeparator);
    parts[1] = objectId.substring(indexOfSeparator+2);
    return parts;
  }
}
