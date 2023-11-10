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

package cloud.cave.domain;

import cloud.cave.common.CaveException;

/** The result of an update operation (of a room or a message or ...).
 * Generally, if an update succeeds then use value UPDATE_OK; otherwise
 * use one of the fail values. Can be extended to model 'graceful
 * degradation'.
 */
public enum UpdateResult {
  UPDATE_OK,                // Update operation succeeded
  FAIL_AS_NOT_FOUND,        // Updated failed as object to update was non existing
  FAIL_AS_NOT_CREATOR,      // Update failed as object was not created/owned by user
  FAIL_AS_ALREADY_EXISTING, // Update failed as object already exists
  ;

  /** Convert used HTTP status Codes to UpdateResults
   *
   * @param status the http status code
   * @return corresponding updateResult enum
   */
  public static UpdateResult translateFromHTTPStatusCode(int status) {
    if (status >= 200 && status < 300){
      return UPDATE_OK;
    }
    switch (status) {
      case 401: return FAIL_AS_NOT_CREATOR; // = UNAUTHORIZED
      case 404: return FAIL_AS_NOT_FOUND;   // = NOT FOUND
      case 403: return FAIL_AS_ALREADY_EXISTING; // FORBIDDEN
      default:
        throw new CaveException("UpdateResult asked to translate unsupported HTTP code: " + status);
    }
  }
}
