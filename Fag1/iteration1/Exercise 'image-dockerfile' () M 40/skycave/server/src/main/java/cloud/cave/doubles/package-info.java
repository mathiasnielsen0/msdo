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
 * Implementation of central server side roles as
 * test doubles. Normally test doubles shall reside
 * in the test tree but as we in the course will deploy
 * servers in an agile fashion with 'fake-it' services
 * they must be in the src tree to ensure they are
 * part of production servers.
 * 
 * */
package cloud.cave.doubles;

