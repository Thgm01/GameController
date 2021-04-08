# AutoReferee Communication Interface

Instead of using the visual interface, the GameController can also be
operated using an automatic referee system (AutoRef). 

If you face any problems with the implementation or API, please 
create an Issue on github to report it.

## Setup


### 1. Building from Source

First, you need to build the GameController from source by calling `ant`
in the main directory. For more details, see the official README.md.


### 2. Requirements

The GameController expects a `game.json` file to either be passed to the 
jar as a command line argument using `--config` or `-c` or to be be present in 
`resources/config/sim`. The config file needs to contain:

````json
{
  "type": "NORMAL" | "KNOCKOUT",
  "class": "ADULT" | "KID",
  "blue": {
    "id": <team_id>
  },
  "red":{
    "id": <team_id>
  }
}
````
The `team_id` is the unique identifier a team is given in the beginning
of the tournament. It needs to be an identifier that is set in the 
`teams.cfg` file in the respective `hl_sim_adult` or `hl_sim_kid` config
folder.



### 3. Executing the Jar

To run the GameController with an instances that can be accessed from the
AutoRef, you need to execute the `GameControllerSimulator.jar`:

```bash
cd build/jar
java -jar GameControllerSimulator.jar [--config path/to/config --port port]
```

### 4. Testing

To test the TCP/IP message interface, you can run the ``main.py`` script
in ``sim_client`` using Python 3.

## TCP/IP Connection

To connect to the GameController via the AutoReferee, you need to
open a TCP/IP connection on port 8750. The port can be configured in
the `AutoRefServer.java` class or by passing another port as a `-p` or
`--port` argument to the jar.

The GameController needs to be up and running (i.e., you need to see
the visual interface) before a connection can be established.

Only one AutoRef can be connected at any point in time.
However, the UI accepts manual input using the buttons despite an AutoRef
is being connected.
In case the AutoRef disconnects from the GameController, a new connection
can be established right away.

## API

All message need to be communicated via Strings in the following format:

`` <id>:<type>:<paramter>``

The ID is a unique numeric identifier of a message. 
It is used to communicate to the AutoRef whether the command that was 
received was legal in the current game state.
For every message received, the GameController will send a response message:

``<id>:OK`` In case the command was valid and the operation has been
performed

``<id>:ILLEGAL`` In case the command was valid but the operation 
was rejected by the GameController due to preconditions not being met
(e.g., the current game state does not allow the operation to be 
performed)

``<id>:INVALID`` In case an unknown command was sent to the GameController.

In the following, all valid message types with the set of parameters 
will be defined. An example for starting a game using the API is provided
in `sim_client/main.py`.

### Kick-Off

Format: ``<id>:KICKOFF:<team_id>``

The GameController automatically sets the team playing on the left
side to have kick-off. Before the game starts, the team having kick-off
can be explicitely set using this message.

The ``team_id`` is the unique identifier of a team that needs to match the ID indicated in the
``game.json`` config.

### Playing Sides

Format: ``<id>:SIDE_LEFT:<team_id>``

The GameController automatically sets the sides a team is playing on
based on the team colors. However, this can be manually changed using 
this message. The message sets explicitly which team plays on the 
left side of the field. The right side is adjusted accordingly.

The ``team_id`` is the unique identifier of a team that needs to match the ID indicated in the
``game.json`` config.


### Clock

Format: ``<id>:CLOCK:<time_in_ms>``

The GameControllerSimulator requires the time to be kept and communicated
via `CLOCK` messages. It is not set up to work with the system's
internal clock. As parameter, it expects the current time in milliseconds.

The GameController can run in realtime as well as faster or slower than
realtime. 

### State

Format: ``<id>:STATE:<gamestate>``

Accepted States: ``READY|SET|PLAY|FINISH|SECOND-HALF``

The GameState is used to explicitly set the current state of the game
in the beginning of a half time, after a game was scored and when the
game time has exceeded the length of a half time.

### Penalty

Format: ``<id>:PENALTY:<team_id>:<robot_id>:<Offense>``

Accepted Offenses: ``BALL_MANIPULATION|PICKUP|INCAPABLE|PHYSICAL_CONTACT``

The Penalties are used to communicate that a robot is removed from the field
suffering a 30 sec removal penalty. The ``team_id`` is the unique
identifier of a team that needs to match the ID indicated in the 
``game.json`` config. The ``robot_id`` is the identifier of the robot
(between 1 and 4 in KidSize and 1 and 2 in AdultSize).

Important: This is not used for direct or indirect free kicks! They
require a different format!

### Score

Format: ``<id>:SCORE:<team_id>``

This message indicates that a goal was scored by a given team.
The ``team_id`` is the unique identifier of a team that needs to match the ID indicated in the
``game.json`` config.

## Upcoming Changes

### As soon as possible
- [ ] Proper configuration of the simulated games:
    - [ ] Removing substitute players
    - [ ] Removing half time period
    
### Update planned for April 12th
- [ ] Penalty shoot-out mode to resolve draws in Round Robin
- [ ] Defining and implementing API for advanced foul behavior
    - [ ] Direct Free Kick
    - [ ] Indirect Free Kick
    - [ ] Corner Kick
    - [ ] Goal Kick
    - [ ] Penalty Kick
    - [ ] Throw-in
- [ ] Defining an implementing API for yellow and red cards
  
### Update planned for May
- [ ] Allow the GameController to be started headless for running
in the cloud



