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

/**
 * The central configuration roles and implementations - factories, Chained
 * Property File readers, and object manager. Notably abstract factories are
 * defined for both the client side and the server side. Both relies on reading
 * properties defined in CPF files which must be read as the first step by the
 * application server (daemon) or the client (cmd). These are explained in the
 * Config constants. The default CPF reader is provided by http://bitbucket.com/henrikbaerbak/cpf.
 * <p>
 * The delegates created by the factories are stored in an instance of the
 * ObjectManager which becomes an application wide 'yellow pages' / DNS of all
 * delegates.
 * <p>
 * Thus, the initialisation sequence in SkyCave is always to create a Factory
 * instance with a property reader, and pass it on to the object manager.
 * 
 * @see cloud.cave.config.ObjectManager
 * 
 */
package cloud.cave.config;

