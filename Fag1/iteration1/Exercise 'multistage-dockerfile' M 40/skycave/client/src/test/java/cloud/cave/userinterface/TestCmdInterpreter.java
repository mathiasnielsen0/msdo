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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import cloud.cave.common.CommonClientCaveTest;
import cloud.cave.common.WallMessageDataTransferObject;
import cloud.cave.domain.*;
import cloud.cave.doubles.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Testing the command line interpreter. We replace system in and out with
 * ByteArray input and output streams and manipulate these through Strings to
 * 'type stuff' into the interpreter and next evaluate the output. Basically
 * the input is a test stub and the output is a spy.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestCmdInterpreter {

  private ByteArrayOutputStream baos;
  private PrintStream ps;
  private Cave cave;

  @BeforeEach
  public void setup() {
     baos = new ByteArrayOutputStream();
     ps = new PrintStream(baos);  
     
     cave = CommonClientCaveTest.createCaveProxyForTesting();
  }
  
  @Test
  public void shouldSeeProperOutputForMostCommands()  {
    // The command sequence is
    // look, who, sys, exec, n, s, e, w, d, u, back, u, p, h, z, dig, u, dig,
    // post, read, exec, exit
    String cmdList = 
        "l\nwho\nsys\nn\ns\ne\nw\nd\nu\nu\np\nh\nz\ndig u Another upper room\n"+
            "u\ndig d NotPossible\n"+
            "exec HomeCommand null\nexec BimseCommand null\nexec HomeCommand\n"+
            "exit\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT,
            TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();
    
    String output = baos.toString();
    
    // System.out.println(output);

    // look
    assertThat(output, containsString("NORTH   EAST   WEST   UP"));
    assertThat(output, containsString("[0] Magnus"));
    
    // who
    assertThat(output, containsString("You are: Magnus/user-002 in Region COPENHAGEN"));

    // sys
    assertThat(output, containsString("CaveStorage: cloud.cave.doubles.FakeCaveStorage"));
    assertThat(output, containsString("SubscriptionService: cloud.cave.doubles.TestStubSubscriptionService"));

    // north
    assertThat(output, containsString("You moved NORTH"));
    assertThat(output, containsString("You are in open forest, with a deep valley to one side."));
    // south
    assertThat(output, containsString("You moved SOUTH"));
    // east
    assertThat(output, containsString("You moved EAST"));
    // west
    assertThat(output, containsString("You moved WEST"));
    // down
    assertThat(output, containsString("There is no exit going DOWN"));
    // up
    assertThat(output, containsString("You moved UP"));
    // p
    assertThat(output, containsString("Your position in the cave is: (0,0,1)"));
    // h
    assertThat(output, containsString("=== Help on the SkyCave commands. ==="));
    
    // z
    assertThat(output, containsString("I do not understand that command. (Type 'h' for help)"));
    
    // dig
    assertThat(output, containsString("You dug a new room in direction UP"));
    assertThat(output, containsString("Another upper room"));
    
    // invalid dig
    assertThat(output, containsString("You cannot dig there as there is already a room in direction DOWN"));
    assertThat(output, not(containsString("NotPossible")));
    

    // exec home command
    assertThat(output, containsString("You went home to position (0,0,0)"));
    // exec bimse command
    assertThat(output, containsString("Player.execute failed to load Command class: BimseCommand"));
    // exec 3
    assertThat(output, containsString("Exec commands require at least one parameter. Set it to null if irrelevant"));
    // exit
    assertThat(output, containsString("I do not understand that long command. (Type 'h' for help)"));
    
    // quit
    assertThat(output, containsString("Logged player out, result = SUCCESS"));
  }
  
  @Test
  public void shouldReportIfUserIsAlreadyLoggedIn() {
    String cmdList = "q\n";
    
    // Ensure dual login
    cave.login(TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD);
    
    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
        ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();
    
    String output = baos.toString();
    // System.out.println(output);
    
    assertThat(output, containsString("*** WARNING! User 'Magnus' is ALREADY logged in! ***"));
    assertThat(output, containsString("*** The previous session will be disconnected. ***"));
  }

  @Disabled // TODO: PendingRequires the System.exit(-1) to be refactored out of the interpreter to work.
  @Test
  public void shouldReportIfUserGivesWrongCredentials() {
    String cmdList = "q\n";
    
    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, "689",
        ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();
    
    String output = baos.toString();
    // System.out.println(output);
    
    assertThat(output, containsString("*** Sorry! The login failed. Reason:"));
  }
  
  /**
   * Fix bug in E15 interpreter which prefixed a space character into
   * the stored room description
   */
  @Test
  public void shouldNotPrefixSpaceForDigCommandIntoRoomDescription()  {
    // move north, dig room up, move up
    String cmdList = 
        "n\ndig u 012345678\nu\nq\n";
    
    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
        ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();
    
    String output = baos.toString();
    
    // Now validate that the output is
    // You moved UP
    // 012345678
    // WITHOUT any space in front of 012345678
    
    // System.out.println(output);

    assertThat(output, containsString("012345678"));
    assertThat("Error as a space is prefixed room description",
        output, not(containsString(" 012345678")));
  }

  @Test
  public void shouldAllowUpdateRoomDescription() {
    // Given a room dig DOWN
    String cmdList =
            "dig d 012345678\nd\nl\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    // When magnus changes the description
    cmdList =
            "change Better Description\nl\nq\n";

    cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();
    // Then it is allowed, and room's description is changed
    assertThat(output, containsString("You changed the room's description."));
    assertThat(output, containsString("Better Description"));
  }

  @Test
  public void shouldNotAllowChangeNonCreatedRooms() {
    // Given I am in an initial room, created by Will

    // When magnus tries to change the room description
    String cmdList =
            "change Better Description\nl\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();

    // it is denied
    assertThat(output, containsString("You cannot change the room. It was not created by you."));
    assertThat(output, containsString("You are standing at the end of a road"));
  }

  @Test
  public void shouldHandleIllegalDigCommand() {
    // Given I am in an initial room, created by Will

    // When magnus tries to 'dig fisk hest' which has no proper direction character
    String cmdList =
            "dig fisk hest\nl\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();
    // When he is told the proper direction characters to use
    assertThat(output, containsString("You entered an illegal direction character, must be one of (n,e,s,w,u,d)"));
  }

  // TODO - solve the 'quote-client' exercise
  @Test
  public void shouldSeeProperOutputForQuote() {
    String cmdList =
            "quote 7\nquote fisk\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();

    // TODO - Update and add tests for 'quote-client' exercise
    assertThat(output, containsString("The true sign of intelligence is not knowledge but imagination. - Albert Einstein"));
    assertThat(output, containsString("You have to provide a numeric parameter to quote. 0 for random quote."));
  }

  // TODO - solve the 'wall-client' exercise
  @Test
  public void shouldSeeProperOutputForWall() {
    String cmdList =
            "post A message\nread\nread 9\nread fisk\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();

    // post and read on the wall
    // TODO: Update this assertion when solving the 'wall-client' exercise
    assertThat(output, containsString("You posted a message."));
    assertThat(output, containsString(" 0: [Magnus, just now] A message"));
//    assertThat(output, containsString(" 0: WALL # 9 NOT IMPLEMENTED YET"));

    assertThat(output, containsString("You have to provide a numeric parameter for page getNumber."));
  }

  // The CmdInterpreter's 'upd' command is tricky to test without the actual
  // implementation in place in the PlayerProxy (and I will give you that :)
  // so we have to replace the Player with a configurable stub - but as
  // it is an cave instance we provide, we have to stub that as well!

  @Test
  public void shouldEditWallMessage() {
    // Create a test double that only responds on the updateMessage method
    // and validate expected parameters
    PlayerDouble playerDouble = new PlayerDouble();

    // and a cave that returns the player double
    Cave stubCave = new Cave() {
      @Override
      public Player login(String loginName, String password) {
        return playerDouble;
      }
      @Override
      public LogoutResult logout(String playerID) { return null; }
      @Override
      public String describeConfiguration() { return null; }
    };

    // Test proper message provided by Interpreter when updating before a wall page has been read
    String cmdList =
            "upd 0 The library is nice too...\nq\n";

    playerDouble.setExpectations(PlayerDouble.ID_FIXED_MSG_1, UpdateResult.UPDATE_OK);

    CmdInterpreter cmd = new CmdInterpreter(stubCave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();
    assertThat(output, containsString("The message no is invalid. There are only 0 messages on the last read page."));

    // Test that updating is possible in case a right index is given
    playerDouble.setExpectations(PlayerDouble.ID_FIXED_MSG_1, UpdateResult.UPDATE_OK);
    cmdList =
            "read\nupd 0 The library is nice too...\nq\n";

    cmd = new CmdInterpreter(stubCave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    output = baos.toString();
    assertThat(output, containsString(" 0: Fisk is good"));
    assertThat(output, containsString(" 1: So is Sild"));
    assertThat(output, containsString("You changed the message."));

    // Test proper output in case update succeed
    playerDouble.setExpectations(PlayerDouble.ID_FIXED_MSG_1, UpdateResult.UPDATE_OK);
    cmdList =
            "read\nupd 0 The library is nice too...\nq\n";

    cmd = new CmdInterpreter(stubCave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    output = baos.toString();
    assertThat(output, containsString(" 0: Fisk is good"));
    assertThat(output, containsString(" 1: So is Sild"));
    assertThat(output, containsString("You changed the message."));

    // Test proper output in case you are not the author and no numeric argument given
    playerDouble.setExpectations(PlayerDouble.ID_FIXED_MSG_2, UpdateResult.FAIL_AS_NOT_CREATOR);
    cmdList =
            "read\nupd 1 The library is nice too...\nupd fisk The library\nq\n";

    cmd = new CmdInterpreter(stubCave, TestConstants.MAGNUS_AARSKORT, TestConstants.MAGNUS_PASSWORD,
            ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    output = baos.toString();
    assertThat(output, containsString(" 0: Fisk is good"));
    assertThat(output, containsString(" 1: So is Sild"));
    assertThat(output, containsString("You are not the author of that message."));
    assertThat(output, containsString("You have to provide a numeric index to tell which message to update."));
  }

  private InputStream makeToInputStream(String cmdList) {
    InputStream is = new ByteArrayInputStream(cmdList.getBytes());
    return is;
  }
}

/** Test double acting as a combined stub and spy
 * for testing the updateMessage handling in the CmdInterpreter.
 */

class PlayerDouble implements Player {
  public static final String ID_FIXED_MSG_1 = "id17";
  public static final String ID_FIXED_MSG_2 = "id42";

  private UpdateResult statusCodeToReturn;
  private String expectId;

  @Override
  public LoginResult getAuthenticationStatus() {
    return LoginResult.LOGIN_SUCCESS;
  }

  @Override
  public UpdateResult updateMessage(String messageId, String newContents) {
    assertThat(messageId, is(expectId));
    return statusCodeToReturn;
  }

  // set what
  public void setExpectations(String messsageId, UpdateResult doReturnThisStatusCode) {
    expectId = messsageId;
    statusCodeToReturn = doReturnThisStatusCode;
  }

  @Override
  public List<WallMessageDataTransferObject> getMessageList(int pageNumber) {
    List<WallMessageDataTransferObject> fixedMessages =
            new ArrayList<>();
    fixedMessages.add(new WallMessageDataTransferObject(ID_FIXED_MSG_1, "Fisk is good"));
    fixedMessages.add(new WallMessageDataTransferObject(ID_FIXED_MSG_2, "So is Sild"));
    return fixedMessages;
  }

  // Rest are dummy impl

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getID() {
    return null;
  }

  @Override
  public String getPosition() {
    return null;
  }

  @Override
  public String getShortRoomDescription() {
    return null;
  }

  @Override
  public Region getRegion() {
    return null;
  }

  @Override
  public String getAccessToken() {
    return null;
  }

  @Override
  public List<String> getPlayersHere() {
    return null;
  }

  @Override
  public List<String> getLongRoomDescription() {
    return null;
  }

  @Override
  public List<Direction> getExitSet() {
    return null;
  }

  @Override
  public String getQuote(int quoteIndex) {
    return null;
  }

  @Override
  public UpdateResult move(Direction direction) {
    return UpdateResult.FAIL_AS_NOT_FOUND;
  }

  @Override
  public UpdateResult digRoom(Direction direction, String description) {
    return UpdateResult.FAIL_AS_NOT_FOUND;
  }

  @Override
  public UpdateResult updateRoom(String newDescription) {
    return UpdateResult.UPDATE_OK;
  }

  @Override
  public void addMessage(String message) {
  }

  @Override
  public List<String> execute(String commandName, String... parameters) {
    return null;
  }
};

