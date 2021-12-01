# pacman-javafx

A JavaFX user interface for my UI-agnostic [Pac-Man / Ms. Pac-Man game](https://github.com/armin-reichert/pacman-basic) implementations. Both games can be played in 2D and 3D.

Unfortunately, I have no animated 3D-model yet. The Pac-Man and ghost models used in this implementation have been generously provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart). Cudos to you, Gianmarco! 


## How to build
```
cd interactivemesh
.\install
cd ..\pacman-ui-fx
mvn clean install
cd ..\pacman-ui-fx-deploy
mvn clean install
```

## How to run

The `pacman-ui-fx-deploy\target` directory now contains a zip file `pacman-javafx-1.0-jlink.zip`. Extract this file to any directory run the application using the batch file `run.cmd` inside the `bin` folder.  

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find exactly this zip file.

### How to use

On the intro screen, you can switch between the two game variants by pressing <kbd>v</kbd>. You can switch between window and fullscreen mode using the standard keys <kbd>F11</kbd> and <kbd>Esc</kbd>.

On the play screen, the following functionality is available:
- <kbd>CTRL+3</kbd> Toggle using 2D/3D play scene
- <kbd>CTRL+C</kbd> Change 3D camera (3 cameras currently implemented)
- <kbd>CTRL+I</kbd> Toggle information about current scene (HUD)
- <kbd>CTRL+L</kbd> Toggle draw mode (line vs. shaded)
- <kbd>CTRL+P</kbd> Toggle pausing game play
- <kbd>CTRL+R</kbd> Increase maze "resolution" (<kbd>CTRL+SHIFT+R</kbd> = decrease)
- <kbd>CTRL+H</kbd> Increase maze wall height (<kbd>CTRL+SHIFT+H</kbd> = decrease)
- <kbd>CTRL+S</kbd> Increase speed (<kbd>CTRL+SHIFT+S</kbd> = decrease)
- <kbd>A</kbd> Toggle autopilot mode
- <kbd>I</kbd> Toggle immunity of player against ghost attacks
- <kbd>Q</kbd> Quit play scene
- Cheats:
  - <kbd>E</kbd> Eat all pills except the energizers
  - <kbd>L</kbd> Add one player life
  - <kbd>N</kbd> Enter next game level
  - <kbd>X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

![Play Scene](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/playscene3D.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://youtu.be/t529vDUtCT0)
