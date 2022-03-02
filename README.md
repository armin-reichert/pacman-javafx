# JavaFX Pac-Man and Ms. Pac-Man

A JavaFX user interface for my UI-agnostic [Pac-Man / Ms. Pac-Man game](https://github.com/armin-reichert/pacman-basic) implementations. Both games can be played in 2D and 3D.

Unfortunately, I have no animated 3D-model yet. The Pac-Man and ghost models used in this implementation have been generously provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart). Cudos to you, Gianmarco! 

## How to build

First, build the Maven project [pacman-basic](https://github.com/armin-reichert/pacman-basic) with

```
mvn clean install
```

Then
```
cd interactivemesh
mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml
cd ..\pacman-ui-fx
mvn clean install
cd ..\pacman-ui-fx-deploy
mvn clean install
```

or just run `build.bat`.

## How to run

The `pacman-ui-fx-deploy\target` directory now contains a zip file `pacman-javafx-1.0-jlink.zip`. 

Extract this file and start the application by executing the file `run.cmd` inside the `bin` folder.  

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find exactly this zip file.

## How to use

General shortcuts:
- <kbd>F11</kbd> Enter fullscreen mode
- <kbd>Esc</kbd> Exit fullscreen mode
- <kbd>A</kbd> Toggle autopilot mode
- <kbd>I</kbd> Toggle immunity of player against ghost attacks
- <kbd>CTRL+I</kbd> Toggle information display (HUD)
- <kbd>P</kbd> Toggle pause
- <kbd>SHIFT+P</kbd> When paused, execute single simulation step
- <kbd>CTRL+S</kbd> Increase speed (<kbd>SHIFT</kbd> = decrease)
- <kbd>CTRL+3</kbd> Toggle using 2D/3D play scene

Intro screen shortcuts:
- <kbd>V</kbd> Switch between the two game variants
- <kbd>Z</kbd> Play all intermission scenes

Play screen shortcuts:
- <kbd>CTRL+LEFT</kbd> Select next camera perspective
- <kbd>CTRL+RIGHT</kbd> Select previous camera perspective
- <kbd>CTRL+L</kbd> Toggle draw mode (line vs. shaded)
- <kbd>CTRL+R</kbd> Increase maze "resolution" (<kbd>SHIFT</kbd> = decrease)
- <kbd>CTRL+H</kbd> Increase maze wall height (<kbd>SHIFT</kbd> = decrease)
- <kbd>Q</kbd> Quit scene

Cheats:
  - <kbd>E</kbd> Eat all pills except the energizers
  - <kbd>L</kbd> Add 3 player lives
  - <kbd>N</kbd> Enter next game level
  - <kbd>X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

![Play Scene](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/pacman-maze.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://www.youtube.com/watch?v=-ANLq4mMn3Q)
