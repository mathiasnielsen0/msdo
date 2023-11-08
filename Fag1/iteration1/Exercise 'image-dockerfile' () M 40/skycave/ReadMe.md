SkyCave - Starting point for the SkyCave project
====

*Henrik Bærbak Christensen, Computer Science, Aarhus University*

Minor but important contributions by thesis student *Daniel Damgaard*,
graduated Summer 2016.

What is it?
-----------

SkyCave is the exam project in the *MicroService and DevOps* fagpakke
at Computer Science, Aarhus University. It is loosely inspired by the
very first adventure game in computing, *Colossal Cave Adventure*, by
Will Crowther, 1972. However, game elements have been removed and
replaced by MMO (Massive Multiuser Online) features.

The code base was originally developed for the course *Cloud
Computing* 2015 - 2016, and later heavily rewritten
during 2019. Security aspects and updating to Gradle 6+ during 2021.

Project lifted to Gradle 8, JUnit 5, and FRDS.Broker 3.0.3 during
2023.

Please consult material on the course homepage for any further
information.

Requirements
------------

To execute and develop SkyCave, you need Java 11+, and Gradle 7+
installed. If you do not have Gradle 7+ installed, then be sure to
*use the gradle wrapper* by initially doing

    gradle wrapper
    
and then make a habbit of `./gradlew xxx` instead of `gradle xxx` in
the examples below.

How do I get started?
---------------------

For running the daemon (SkyCave application server) and the cmd
(SkyCave client/user interface), typical usage is

Starting a SkyCave application server (the *daemon*) using its
configuration file 'http.cpf':

    gradle daemon -Pcpf="http.cpf"

To start the daemon in a specific configuration as required by an
exercise, use a CPF named after the exercise, like e.g.:
  
    gradle daemon -Pcpf="quote-client.cpf"

To shut down the daemon again, do it the hard way: Hit Ctrl-c.

Starting a SkyCave client command-line (the *cmd*) configured to
talk to a daemon in a specific configuration, again, use 
-Pcpf=(exercise.cpf):

    gradle cmd -Pcpf="operations.cpf"

(Note: Most exercises do not need special configurations for the 
'cmd' as the default http.cpf will suffice for almost all cases,
except contacting a real cloud-deployed daemon.)

The *cmd* defaults to the initial (starter code) player with ID =
mikkel_aarskort and password '111'. To log in as a specific player, use

    gradle cmd -Pcpf="http.cpf" -Pid=magnus_aarskort -Ppwd=222
    
The 'TestStubSubscriptionService' only knows three users:

    mikkel_aarskort, 111
    magnus_aarskort, 222
    mathilde_aarskort, 333
    
Configuration of SkyCave
-------------

SkyCave is heavily reconfigurable to allow automated testing, as well
as supporting incremental development work and alternative
implementations of protocols, databases, service connectors, etc. The
configurability is controlled by reading **Chained Property Files
(CPFs)**, like `http.cpf` above, which define properties = (key,value)
pairs for all configuration options. All keys are prefixed by
SKYCAVE_. For instance, the following key-value pair defines which
host and port the *daemon* is operating on (from http.cpf):

    # === Configure for server to run on localhost
    SKYCAVE_APPSERVER = localhost:7777

Following the Gradle convention of having resource files in subfolders
of `src/main/resources`, you should locate your CPF files in the
`resources/cpf` folder. Note that the client (cmd) and server (daemon)
subprojects have their own CPF files; in most cases it is only the
server side that needs a lot of reconfiguration for a specific
exercise.

Review the provided `local.cpf`, `socket.cpf`, and `http.cpf` for
examples. The gradle build scripts define `http.cpf` as the default
for *daemon* and *cmd*.

Both subproject /server and /client (the 'daemon' and 'cmd'
respectively) have their own set of CPF files in the resource folder.

**Note:** A central aspect of CPF files is the *chaining feature* in
which one CPF may inherit and overwrite key-value pairs from an
ancestor CPF file, by the chain line. For instance, the `http.cpf`
inherits most of the properties from `socket.cpf` by using the chaining
line:

    # http.cpf is reusing that most is
    # already set correctly in socket.cpf

    < cpf/socket.cpf

The CPF library can be found
at [Chained Property Files (CPF)](https://bitbucket.org/henrikbaerbak/cpf).

How to I get started understanding the code?
--------------------------------------------

I advice starting by reviewing the testcases for the Player
abstraction.

First review the tests for the `server` project,
test/cloud/cave/server/TestPlayerServant, which gives you an
impression of the server side implementation.

Next, review the (same) tests in the `client` project
(TestPlayerProxy). Here the IPC/networking layer is 'short-circuited'
and all network calls simulated by simple in-JVM method calls.

The distribution aspect (which is central to the course) uses the
Broker pattern described in [Flexible, Reliable, Distributed
Software](leanpub.com/frds), and if you follow the call sequence from
the tests in the testcases in test/cloud/cave/client/TestPlayerProxy
in the `client` subproject, you will trace through many of the central
aspects (marshalling and IPC).

The code base reuses some default implementations in the [FRDS.Broker
library](bitbucket.com/henrikbaerbak/broker). However, importing the
SkyCave project in IntelliJ will also, through gradle, import all the
source code to browse.

And - review the design slides and other documentation from the course
web pages.

How do I pack the daemon into a deployment unit (jar)?
----------------------------------------------------------

The *daemon* can be packaged into a fat jar (that is, a
self-contained jar bundles with all dependencies included.)

The gradle target is *jar*. The jar file is called 'daemon.jar' and
will be located in the server/build/libs folder.

Example: Make the daemon fat jar and next run a daemon using the jar:

    gradle :server:jar
    java -jar server/build/libs/daemon.jar http.cpf

Note that now you have to remember the correct ordering of arguments,
see the main() method of the daemon.

What to do next?
----------------

Solve the exercises posted on the e-learning platform using the
techniques taught.

Happy coding. *- Henrik*
