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

import java.io.*;

import cloud.cave.client.CaveProxy;
import cloud.cave.userinterface.*;
import cloud.cave.common.Config;
import cloud.cave.common.Marshalling;
import cloud.cave.config.CaveClientFactory;
import cloud.cave.config.StandardClientFactory;
import cloud.cave.domain.*;

import com.baerbak.cpf.ChainedPropertyResourceFileReaderStrategy;
import com.baerbak.cpf.PropertyReaderStrategy;
import frds.broker.ClientRequestHandler;
import frds.broker.Requestor;
import frds.broker.Versioning;
import frds.broker.marshall.json.StandardJSONRequestor;

/**
 * Main method for a command line client.
 *
 * It is configured using the base name of the CPF file,
 * which should be located in the proper
 * subfolder named 'cpf/' (Config.CPF_RESOURCE_FOLDER) in resources folder.
 * That is, if started with argument 'http.cpf' then the CPF file
 * in '(project)/src/main/resources/cpf/http.cpf' will be read for
 * configuration properties. You may also provide an absolute
 * path CPF file as argument in which case it will be taken
 * verbatim.
 *
 * 2nd and 3rd parameters are loginName and password
 *
 * 4th parameter is optional, and not really for use in the
 * ordinary use case of Cmd. However, if provided, the
 * argument must be the name of a file whose contents are
 * commands to be executed by Cmd. REMEMBER that the
 * last line MUST be 'q' to terminate the Cmd application.
 * To be used by the automated testing system Crunch.
 *
 * Note: The filesystem is read using File, so this
 * does NOT work if Cmd is bundled into a Jar file.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 */
public class CaveCmd {

  public static final String NO_CMD_FILE_PROVIDED = "none";

  public static void main(String[] args) throws IOException {
    CaveClientFactory factory;
    PropertyReaderStrategy propertyReader;
    
    String cpfFileName = Config.prependDefaultFolderForNonPathFilenames(args[0]);

    String loginName = args[1];
    String pwd = args[2];
    String cmdListFileName = NO_CMD_FILE_PROVIDED;
    if (args.length >= 4) {
      cmdListFileName = args[3];
    }

    propertyReader = new ChainedPropertyResourceFileReaderStrategy(cpfFileName);
    factory = new StandardClientFactory(propertyReader);
    
    ClientRequestHandler requestHandler = factory.createClientRequestHandler();
    Requestor requestor = new StandardJSONRequestor(requestHandler);
    Cave cave = new CaveProxy(requestor);

    // Set the marshalling format version.
    Versioning.SetMarshallingFormatVersion(Marshalling.MARSHALING_VERSION);

    System.out.println("Starting cmd with Cpf File = " + cpfFileName);

    // Determine the input stream
    InputStream systemIn = System.in;
    if (! cmdListFileName.equals(NO_CMD_FILE_PROVIDED)) {
      File cmdListFile = new File(cmdListFileName);
      if (cmdListFile.exists()) {
        systemIn = new FileInputStream(new File(cmdListFileName));
        System.out.println("  Reading commands from file: " + cmdListFileName);
      }
    }

    new CmdInterpreter(cave, loginName, pwd,
        System.out, systemIn)
            .readEvalLoop();
  }
}

