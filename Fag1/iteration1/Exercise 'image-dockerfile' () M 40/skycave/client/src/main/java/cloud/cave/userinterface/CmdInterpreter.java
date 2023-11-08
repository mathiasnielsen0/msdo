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

package cloud.cave.userinterface;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.domain.*;

/**
 * The client interpreter, implementing a classic shell based read-eval-loop
 * command line tool to log a player into the cave and allow him/her to explore
 * it.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class CmdInterpreter {

  private Cave cave;
  private Player player;
  
  private PrintStream systemOut;
  private InputStream systemIn;
  private List<WallMessageDataTransferObject> localCacheOfReadMessages
          = new ArrayList<>();

  /**
   * Construct the interpreter.
   * 
   * @param cave
   *          the cave to log into
   * @param loginName
   *          the loginName of the player
   * @param pwd
   *          the password of the player
   * @param systemOut
   *          the print stream that acts as shell output
   * @param systemIn
   *          the input stream that act as keyboard input
   */
  public CmdInterpreter(Cave cave, String loginName, String pwd,
                        PrintStream systemOut, InputStream systemIn) {
    this.cave = cave;

    this.systemOut = systemOut;
    this.systemIn = systemIn;
    
    systemOut.println("Trying to log in player with loginName: " + loginName);

    player = cave.login(loginName, pwd);
    LoginResult authenticationStatus = player.getAuthenticationStatus();

    boolean success = LoginResult.isValidLogin(authenticationStatus);

    if (!success) {
      systemOut.println("*** SORRY! The login failed. Reason: "
          + authenticationStatus);
      System.exit(-1);
    }

    if (authenticationStatus == LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN) {
      systemOut.println("*** WARNING! User '"
          + player.getName() + "' is ALREADY logged in! ***");
      systemOut
          .println("*** The previous session will be disconnected. ***");
    }
  }

  /**
   * The classic 'read command, evaluate command, loop' of a shell. Issue your
   * command, and see the result in the shell.
   */
  public void readEvalLoop() {
    systemOut.println("\n== Welcome to SkyCave, player " + player.getName()
        + " ==");

    BufferedReader bf = new BufferedReader(new InputStreamReader(systemIn));

    systemOut
        .println("Entering command loop, type \"q\" to quit, \"h\" for help.");

    // and enter the command processing loop
    String line;
    try {
      do {
        systemOut.print("> ");
        line = bf.readLine();
        if (line.length() > 0) {
          // split into into tokens on whitespace
          String[] tokens = line.split("\\s");

          try {
            // First handle the 'short hand' notation for movement
            if (tokens[0].length() == 1) {
              char primaryCommand = line.charAt(0);
              handleSingleCharCommand(primaryCommand);
            } else {
              handleMultipleCharCommand(tokens[0], tokens);
            }
          } catch (IllegalDirectionCharacter exc) {
            systemOut.println("You entered an illegal direction character, must be one of (n,e,s,w,u,d).");
          }
          systemOut.println();
        }
      } while (! line.equals("q"));
    } catch (PlayerSessionExpiredException exc) {
      systemOut
              .println("**** Sorry! Another session has started with the same loginID. ***");
      systemOut
              .println("**** You have been logged out.                                 ***");
      System.exit(0);
    } catch (IOException e) {
      systemOut.println("Exception caught: " + e);
    }
    systemOut.println("Leaving SkyCave - Goodbye.");
  }

  /**
   * Interpret and execute a command.
   * 
   * @param command potential command to execute
   * @param tokens arguments split into token array
   */
  private void handleMultipleCharCommand(String command, String[] tokens) throws IllegalDirectionCharacter {
    if (command.equals("dig") && tokens.length > 2) {
      Direction direction = getDirectionFromChar(tokens[1].charAt(0));
      // Compile the room description by putting the tokens back into a single string again
      String roomDescription = "";
      roomDescription = mergeTokens(tokens, 2);

      UpdateResult isValid = player.digRoom(direction, roomDescription);
      if (isValid == UpdateResult.UPDATE_OK) {
        systemOut.println("You dug a new room in direction " + direction);
      } else {
        systemOut
                .println("You cannot dig there as there is already a room in direction "
                        + direction);
      }

    } else if (command.equals("change") && tokens.length > 1) {
      String roomDescription = "";
      roomDescription = mergeTokens(tokens, 1);
      UpdateResult status = player.updateRoom(roomDescription);
      if (status == UpdateResult.UPDATE_OK) {
        systemOut.println("You changed the room's description.");
      } else {
        systemOut.println("You cannot change the room. It was not created by you." );
      }

    } else if (command.equals("who")) {
      systemOut.println("You are: " + player.getName()+ "/"+ player.getID()+ " in Region "+player.getRegion());
      systemOut.println("   in local session: " + player.getAccessToken());

    } else if (command.equals("quote") && tokens.length > 1) {
      String index = tokens[1];
      int quoteIndex = 1;
      try {
        quoteIndex = Integer.parseInt(index);
        String quote = player.getQuote(quoteIndex);
        systemOut.println(quote);
      } catch(NumberFormatException exc) {
        systemOut.println("You have to provide a numeric parameter to quote. 0 for random quote.");
      }

    } else if (command.equals("post") && tokens.length > 1) {
      String message = mergeTokens(tokens, 1);
      player.addMessage(message);
      systemOut.println("You posted a message.");

    } else if (command.equals("read")) {
      int pageNumber = 0;
      if (tokens.length > 1) {
        try {
          pageNumber = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException exc) {
          systemOut.println("You have to provide a numeric parameter for page getNumber.");
          pageNumber = -1; // use page as flag to avoid output
        }
      }
      if (pageNumber >= 0) {
        // Cache last read messages
        localCacheOfReadMessages = player.getMessageList(pageNumber);
        // As we need the index, an oldfashioned loop is the way forward
        for (int index = 0; index < localCacheOfReadMessages.size(); index++) {
          String numeredOutput = String.format("%2d: %s", index, localCacheOfReadMessages.get(index).getMessage());
          systemOut.println(numeredOutput);
        }
      }

    } else if (command.equals("upd") && tokens.length > 2) {
      int msgIndex = 0;
      try {
        msgIndex = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException exc) {
        systemOut.println("You have to provide a numeric index to tell which message to update.");
        msgIndex = -1; // use index as flag to avoid output
      }
      // Test out of bounds
      if (msgIndex >= localCacheOfReadMessages.size()) {
        systemOut.println("The message no is invalid. There are only "
                + localCacheOfReadMessages.size()
                + " messages on the last read page.");
        msgIndex = -1;
      }
      if (msgIndex >= 0) {
        String newMessage = "";
        newMessage = mergeTokens(tokens, 2);
        String messageId = localCacheOfReadMessages.get(msgIndex).getId();
        UpdateResult updateResult = player.updateMessage(messageId, newMessage);
        if (updateResult == UpdateResult.UPDATE_OK) {
          systemOut.println("You changed the message.");
        } else if (updateResult == UpdateResult.FAIL_AS_NOT_CREATOR) {
          systemOut.println("You are not the author of that message.");
        }
      }

    } else if (command.equals("sys")) {
      systemOut.println("System information:");
      systemOut.println(cave.describeConfiguration());
      systemOut.println(player.toString());
    
    } else if (command.equals("exec")) {
      if (tokens.length > 2) {
        List<String> response = null;
        // Create the parameter array
        String[] parameters = new String[tokens.length - 2];
        for (int i = 2; i < tokens.length; i++) {
          parameters[i - 2] = tokens[i];
        }

        response = player.execute(tokens[1], parameters);
        
        prettyPrintResponse(systemOut, response);
        
      } else {
        systemOut
            .println("Exec commands require at least one parameter. Set it to null if irrelevant");
      }
    } else {
      systemOut.println("I do not understand that long command. (Type 'h' for help)");
    }
  }

  private void prettyPrintResponse(PrintStream systemOut2, List<String> reply) {
    reply.stream().forEach(str -> systemOut2.println(str));
  }

  /**
   * Merge the tokens in array 'tokens' from
   * index 'from' into a space separated string.
   * @param tokens the array of tokens
   * @param from the starting index 
   * @return a merged string with all tokens
   * separated by space
   */
  private String mergeTokens(String[] tokens, int from) {
    String mergedString="";
    for (int i=from; i < tokens.length; i++) { mergedString += " "+tokens[i]; }
    // fix bug in E15 version, remove first space
    return mergedString.substring(1);
  }

  private void handleSingleCharCommand(char primaryCommand) throws IllegalDirectionCharacter {
    switch (primaryCommand) {
    // look
    case 'l': {
      player.getLongRoomDescription().stream().forEach(systemOut::println);
      break;
    }
    // The movement commands
    case 'n': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 's': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'e': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'w': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'u': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'd': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }

    // position
    case 'p': {
      systemOut.println("Your position in the cave is: "
          + player.getPosition());
      break;
    }
    // Help
    case 'h': {
      showHelp();
      break;
    }
    // Quit
    case 'q': {
      LogoutResult logoutResult = cave.logout(player.getID());
      systemOut.println("Logged player out, result = " + logoutResult);
      break;
    }
    default: {
      systemOut.println("I do not understand that command. (Type 'h' for help)");
    }
    }
  }

  private Direction getDirectionFromChar(char commandChar) throws IllegalDirectionCharacter {
    switch (commandChar) {
    case 'n':
      return Direction.NORTH;
    case 's':
      return Direction.SOUTH;
    case 'e':
      return Direction.EAST;
    case 'w':
      return Direction.WEST;
    case 'u':
      return Direction.UP;
    case 'd':
      return Direction.DOWN;
    default:
      throw new IllegalDirectionCharacter("getDirectionFromChar got wrong parameter: "
          + commandChar);
    }
  }

  private void tryToMove(Direction direction) {
    if (player.move(direction) == UpdateResult.UPDATE_OK) {
      systemOut.println("You moved "+direction);
      systemOut.println(player.getShortRoomDescription());
    } else {
      systemOut.println("There is no exit going " + direction);
    }
  }

  private void showHelp() {
    systemOut.println("=== Help on the SkyCave commands. ===");
    systemOut.println("  Many commonly used commands are single-character");
    systemOut.println("Commands:");
    systemOut.println(" n,s,e,w,d,u    :  MOVE north, south, etc;");
    systemOut.println(" q              :  QUIT sky cave;");
    systemOut.println(" h              :  HELP, print this help instructions;");
    systemOut.println(" l              :  LOOK, print long description of present room;");
    systemOut.println(" p              :  POSITION, print your (x,y,z) position");
    systemOut.println("Longer Commands:");
    systemOut.println(" who            :  WHO, print info on your avatar;");
    systemOut.println(" dig [d] [desc] :  DIG room in direction [d] with description [desc];");
    systemOut.println(" change [desc]  :  CHANGE current room with new description [desc];");
    systemOut.println(" post [msg]     :  POST [msg] on this room's wall;");
    systemOut.println(" read [p]       :  READ messages on page 'p' of this room's wall (default is p=0);");
    systemOut.println(" upd [no] [msg] :  UPDATE message 'no' of last read wall messages to new [msg];");
    systemOut.println(" quote [i]      :  QUOTE get famous quote (i=0 for random quote);");
    systemOut.println(" sys            :  SYStem and configuration information;");

    systemOut.println(" exec [cmd] [param]* :  EXEC [cmd] with 1 or more [param]s;");
  }

}