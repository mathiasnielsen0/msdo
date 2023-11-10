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

import com.baerbak.cpf.PropertyReaderStrategy;

import java.lang.reflect.InvocationTargetException;

/**
 * Config encapsulates the names of CPF file property keys that must be set in
 * order for the factories to create proper delegate configurations of the
 * server and client side of the cave system.
 *
 * A common scheme is applied for ExternalServices, that is, services
 * that run on external nodes in the network, like MongoDB or microservices.
 * These requires A) a DNS address(es) of the server, like 'db:27017' or
 * 'localhost:9999,localhost:9998' and B) a connector implementation in java (akin a
 * driver) for skycave to communicate with it. The latter MUST
 * implement the ExternalService interface. The scheme dictate
 * how CPF files name the two properties. The former must
 * have a suffix of "_SERVER_ADDRESS" and the latter must
 * have a suffix of "_CONNECTOR_IMPLEMENTATION".
 *
 * Example: The CaveStorage implementation must be defined by
 * SKYCAVE_CAVESTORAGE_SERVER_ADDRESS and SKYCAVE_CAVESTORAGE_CONNECTOR_IMPLEMENTATION
 * respectively. See 'socket.cpf' for an example in the resource
 * folder.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class Config {

  /** The connector implementation suffix */
  public static final String CONNECTOR_SUFFIX = "_CONNECTOR_IMPLEMENTATION";
  /** The server address suffix */
  public static final String SERVER_ADDRESS_SUFFIX = "_SERVER_ADDRESS";

  /**
   * Property that must be set to 'name:port' of the endpoint for
   * the application server. In case of a cluster, separate each endpoint with
   * ','.
   */
  public static final String SKYCAVE_APPSERVER = "SKYCAVE_APPSERVER";

  /**
   * Prefix for the CaveStorage service properties
   */
  public static final String SKYCAVE_CAVESTORAGE = "SKYCAVE_CAVESTORAGE";

  /**
   * Prefix for the SubscriptionService properties
   */
  public static final String SKYCAVE_SUBSCRIPTIONSERVICE = "SKYCAVE_SUBSCRIPTIONSERVICE";

  /**
   * Prefix for the QuoteServer service properties
   */
  public static final String SKYCAVE_QUOTESERVICE = "SKYCAVE_QUOTESERVICE";

  /**
   * Prefix for the PlayerNameService properties
   */
  public static final String SKYCAVE_PLAYERNAMESERVICE = "SKYCAVE_PLAYERNAMESERVICE";

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the server request handler (Broker). This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION = "SKYCAVE_SERVERREQUESTHANDLER_IMPLEMENTATION"; 

  /**
   * Property that must be set to the fully qualified class name of
   * the class implementing the client request handler interface (Broker). This class must be in
   * the classpath and will be loaded at runtime by the ClientFactory.
   */
  public static final String SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION = "SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION";

  /**
   * Read a property using the given reader strategy. Fail immediately in case
   * the property is not set.
   * 
   * @param propertyReader
   *          the property reader strategy to be used to read properties
   * @param key
   *          the key for the property to be read
   * @return the value of the property with the given key
   * @throws CaveConfigurationNotSetException
   *           in case the property is not set
   */
  public static String failFastRead(PropertyReaderStrategy propertyReader, String key) {
    String value = propertyReader.getValue(key);
    if (value == null || value.equals("")) {
      throw new CaveConfigurationNotSetException("ConfigurationError: The configuration is not defined because"
          + " the configuration property with key '" + key + "' is not set.");
    }
    return value;
  }

  /**
   * Generic method to load and instantiate object of type T which is on the
   * path given by a property.
   * 
   * @param <T>
   *          type parameter defining the class type of the object to
   *          instantiate
   * @param propertyReader
   *          the strategy for reading the property
   * @param keyOfProperty
   *          the key of the property that holds the full path to the class to
   *          load
   * @param theObject
   *          actually a dummy but its type tells the method the generic type
   * @return object of type T loaded from the fully qualified type name given by
   *         the property
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadAndInstantiate(PropertyReaderStrategy propertyReader,
      String keyOfProperty, T theObject) {
    // read full path of class to load
    String qualifiedNameOfType;
    qualifiedNameOfType = 
        Config.failFastRead(propertyReader, keyOfProperty);

    // Use java reflection to read in the class
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedNameOfType);
    } catch (ClassNotFoundException e) {
      throw new CaveClassNotFoundException("Factory error: Class '"
          +qualifiedNameOfType+"' is not found."+
          "Property key : "+keyOfProperty);
    }
    
    // Next, instantiate object from the class 
    try {
      theObject = (T) theClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new CaveClassInstantiationException("Factory error: Class '"
          +qualifiedNameOfType+"' could not be instantiated!", e);
    }
    
    return theObject;
  }


  public static final String CPF_RESOURCE_FOLDER = "cpf";

  /** Given a provided filename of a CPF file, determine 'where it
   * should be' in a skycave context. If it is just a simple
   * filename (no path) then prepend the 'cpf/' path as this
   * is the default location of skycave cpf files. Otherwise
   * use it verbatim, as it allows setting any cpf file.
   * @param baseFileName
   * @return
   */
  public static String prependDefaultFolderForNonPathFilenames(String baseFileName) {
    // If a path is provided as part of the name, just return the filename itself
    if (baseFileName.contains("/")) return baseFileName;
    // otherwise, prepend the base folder for skycave
    return CPF_RESOURCE_FOLDER + "/" + baseFileName;
  }
}
