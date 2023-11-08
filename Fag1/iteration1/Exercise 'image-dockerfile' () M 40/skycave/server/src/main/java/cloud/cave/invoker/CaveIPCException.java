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

import cloud.cave.common.CaveException;

/**
 * All IPC (inter process communication) exceptions derive from this root
 * exception.
 * <p>
 * As recommended by 'uncle bob' it is an unchecked exception. Robert C. Martin:
 * "Clean Code", p. 106.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class CaveIPCException extends CaveException {

  private static final long serialVersionUID = -6391176877460888747L;
  
  public CaveIPCException(String message, Exception originalException) {
    super(message, originalException);
  }

}
