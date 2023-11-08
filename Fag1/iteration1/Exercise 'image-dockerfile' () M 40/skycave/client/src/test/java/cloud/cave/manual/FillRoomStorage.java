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

package cloud.cave.manual;

import cloud.cave.client.CaveProxy;
import cloud.cave.common.Config;
import cloud.cave.common.Marshalling;
import cloud.cave.config.*;
import cloud.cave.domain.*;

import com.baerbak.cpf.ChainedPropertyResourceFileReaderStrategy;
import com.baerbak.cpf.PropertyReaderStrategy;
import frds.broker.ClientRequestHandler;
import frds.broker.Requestor;
import frds.broker.Versioning;
import frds.broker.marshall.json.StandardJSONRequestor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility to generate a NxNxN sized cube of rooms.
 * Expects an 'empty' cave, and will dig N cellar
 * levels of NxN  rooms going east and north.
 *
 * Expects an empty cave.
 * 
 * <p>
 * Do NOT use this on the production server :-)
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class FillRoomStorage {
  
  private static final int CUBE_SIZE = 10;

  public static void main(String[] args) {
    CaveClientFactory factory;
    PropertyReaderStrategy propertyReader;

    String cpfFileName = Config.prependDefaultFolderForNonPathFilenames(args[0]);
    String loginName = args[1];
    String pwd = args[2];

    propertyReader = new ChainedPropertyResourceFileReaderStrategy(cpfFileName);
    factory = new StandardClientFactory(propertyReader);

    ClientRequestHandler requestHandler = factory.createClientRequestHandler();
    Requestor requestor = new StandardJSONRequestor(requestHandler);
    Cave cave = new CaveProxy(requestor);

    // Set the marshalling format version.
    Versioning.SetMarshallingFormatVersion(Marshalling.MARSHALING_VERSION);

    System.out.println("*** Cave Room Generator:  ***");
    System.out.println("  Cpf = "+cpfFileName);
    
    // and login Mikkel
    Player player = cave.login(loginName, pwd);
    System.out.println("--> login result: "+player.getAuthenticationStatus());
    
    if (! LoginResult.isValidLogin(player.getAuthenticationStatus())) {
      System.out.println("Not a valid user, stopping...");
      System.exit(-1);
    }
    
    // Now we know the player is valid, let us go to work...
    System.out.println("--> player logged into cave");
    System.out.println("*** Initialized, will start digging DOWN. Do the stepDown while writing! ***");
    
    // Fill the Cave, starting from position (0,0,0)
    final int max = CUBE_SIZE;

    jumpToRoomAtPosition(player, "(0,0,0)");
    digFullCubeDownFromHere(player, CUBE_SIZE);

    cave.logout(player.getID());

    System.out.println();
    System.out.println("*** Done. Remember to erase DB manually before attempting a new run. ***");
  }

  /** Dig a room DOWN from current position, and then make a full NxNxN
   * sized cube of rooms, all going down, east, north.
   * @param player the player to dig
   * @param N The size N of the cube
   */
  private static void digFullCubeDownFromHere(Player player, int N) {
    String originalPosition = player.getPosition();
    digOneCellAndMoveThere(player, Direction.DOWN);

    for (int cellarLevels = 0; cellarLevels < N; cellarLevels++) {
      digFullSquareAtCurrentLevel(player, N);
      // prep for next cellar level, if not the last has been dug
      if (cellarLevels < N - 1) {
        digOneCellAndMoveThere(player, Direction.DOWN);
      }
    }
    jumpToRoomAtPosition(player, originalPosition);
  }

  /** Dig a NxN sized 'square' of rooms at current level */
  private static void digFullSquareAtCurrentLevel(Player player, int N) {
    String originalPosition = player.getPosition();
    for (int y = 0; y < N; y++) {
      digCorridor(player, Direction.EAST, CUBE_SIZE);
      // prep for next corridor, if not the last has been dug
      if (y < N - 1) {
        digOneCellAndMoveThere(player, Direction.NORTH);
      }
    }
    jumpToRoomAtPosition(player, originalPosition);
  }

  /** Dig a corridor at current level going in given direction for N rooms */
  private static void digCorridor(Player player, Direction direction, int N) {
    String originalPosition = player.getPosition();
    // We are already in the first room of the corridor, thus dig count minus one rooms
    for (int c = 0; c < N-1; c++) {
      digOneCellAndMoveThere(player, direction);
    }
    jumpToRoomAtPosition(player, originalPosition);
  }

  private static void digOneCellAndMoveThere(Player player, Direction direction) {
    UpdateResult wentOk;
    wentOk = player.digRoom(direction, "The room dug from player in position "+player.getPosition());
    if (wentOk != UpdateResult.UPDATE_OK) {
      System.out.println("ERROR: The cave is not empty, failed on digging room at position: "+player.getPosition());
      System.exit(-1); // Fail fast...
    }
    player.move(direction);
    System.out.println(" --> AT: " + player.getPosition());
  }

  private static void jumpToRoomAtPosition(Player player, String newPosition) {
    String positionAsString = newPosition;
    List<String> output = player.execute("JumpCommand", positionAsString);
    if ( ! output.contains("You jumped to position: " + positionAsString) ) {
      System.out.println("ERROR: Failed to jump to " + positionAsString + ". Output of command "
              + output.stream().collect(Collectors.joining("/")));
      System.exit(-1); // Fail fast...
    }
  }
}
