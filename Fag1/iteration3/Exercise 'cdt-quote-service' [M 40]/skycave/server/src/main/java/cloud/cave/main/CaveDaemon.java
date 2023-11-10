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

package cloud.cave.main;

import cloud.cave.common.Config;
import cloud.cave.domain.Cave;
import cloud.cave.common.Marshalling;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.config.StandardServerFactory;

import com.baerbak.cpf.ChainedPropertyResourceFileReaderStrategy;
import com.baerbak.cpf.PropertyReaderStrategy;

import frds.broker.ServerRequestHandler;
import frds.broker.Versioning;

import org.slf4j.*;

/**
 * The 'main' daemon to run on the server side. It is configured using the
 * base name of the CPF file, which should be located in the proper
 * subfolder named 'cpf/' (@See Config.CPF_RESOURCE_FOLDER) in resources folder.
 * That is, if started with argument 'http.cpf' then the CPF file
 * in '(project)/src/main/resources/cpf/http.cpf' will be read for
 * configuration properties. You may also provide an absolute
 * path CPF file as argument in which case it will be taken
 * verbatim.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class CaveDaemon {

  public static void main(String[] args) throws InterruptedException {
    
    // Create the logging
    Logger logger = LoggerFactory.getLogger(CaveDaemon.class);

    String cpfFileName = Config.prependDefaultFolderForNonPathFilenames(args[0]);

    // Create the abstract factory to create delegates using dependency injection.
    // The daemon always uses CPF files for defining delegates.
    CaveServerFactory factory;
    PropertyReaderStrategy propertyReader;
    propertyReader = new ChainedPropertyResourceFileReaderStrategy(cpfFileName);
    factory = new StandardServerFactory(propertyReader);
    
    // Create the object manager that creates and holds all delegate references
    // for global access - a sort of lookup service/DNS/yellow pages for delegates
    ObjectManager objManager = new StandardObjectManager(factory);

    Cave cave = objManager.getCave();

    // Welcome
    logger.info("SkyCave Daemon Starting. Use Ctrl-c to terminate!");
    logger.info("cpf=" + cpfFileName);

    // Set the marshalling format version.
    Versioning.SetMarshallingFormatVersion(Marshalling.MARSHALING_VERSION);

    // and start the daemon...
    ServerRequestHandler daemon = objManager.getServerRequestHandler();
    daemon.start(); 
  }


}
