## Pac-Man and Ms. Pac-Man (game model+logic, sound events, animation control, no UI!)

Pac-Man and Ms. Pac-Man game implementation written in Java with levels, timing, ghost "AI" etc. following the details given in the (highly appreciated) [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman. The Ms. Pac-Man levels probably are not 100% accurate because I could not find a similarly detailed description as the Pac-Man dossier. Any hints? 

The code in this project is more "to the point" than the one in my other [state-machine focussed implementation](https://github.com/armin-reichert/pacman).

The implementation follows the Model-View-Controller design pattern:
- The game controller is a [finite-state machine](pacman-core/src/main/java/de/amr/games/pacman/lib/Fsm.java) with states
  -  INTRO, CREDIT, READY, HUNTING, LEVEL_STARTING, LEVEL_COMPLETE, PACMAN_DYING, GHOST_DYING, GAME_OVER and INTERMISSION. (See [GameState](pacman-core/src/main/java/de/amr/games/pacman/controller/GameState.java))
- User interfaces can be attached to the model/controller layer using a [game event listener](pacman-core/src/main/java/de/amr/games/pacman/event/GameEventListener.java) interface.

<img src="doc/pacman-core-classes.png">

The MVC design allows to implement different user interfaces for Pac-Man and Ms. Pac-Man without having to recreate the details of the game logic. 

As proof of concept I implemented the following two UI variants: 
- A Swing UI implementation, see repository [pacman-ui-swing](https://github.com/armin-reichert/pacman-ui-swing) (OUTDATED! Must be adapted to core changes).
- A JavaFX UI implementation with play scenes in 2D and 3D, see repository [pacman-javafx](https://github.com/armin-reichert/pacman-javafx).

YouTube:

[![YouTube video](doc/thumbnail.jpg)](https://www.youtube.com/watch?v=t529vDUtCT0&t=125s)

### Build

`mvn clean install`

### Intro scene

<img src="doc/intro.png">

### Pac-Man play scene

<img src="doc/playing.png">

### Ms. Pac-Man play scene (2D vs. 3D)

<img src="doc/mspacman_playing.png">

<img src="doc/playscene3D.png">
