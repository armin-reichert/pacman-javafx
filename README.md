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
cd ..\pacman_ui_fx
mvn clean install
cd ..\tentackle
mvn clean install
```

or just run `build.bat`.

## How to run

The `tentackle\target` directory now contains a zip file `pacman-javafx-tentackle-1.0-jlink.zip`. 

Extract this file and start the application by executing the file `run.cmd` inside the `bin` folder.  

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find exactly this zip file.

## How to use

General shortcuts:

- <kbd>F11</kbd> Enter fullscreen mode
- <kbd>Esc</kbd> Exit fullscreen mode
- <kbd>CTRL+I</kbd> Toggle information/settings panels
- <kbd>Alt+A</kbd> Toggle autopilot mode
- <kbd>Alt+I</kbd> Toggle immunity of player against ghost attacks
- <kbd>V</kbd> Switch between the game variants (Pac-Man, Ms. Pac-Man)
- <kbd>Alt+Z</kbd> Play all intermission scenes
- <kbd>Alt+3</kbd> Toggle using 2D/3D play scene

Play screen shortcuts:
- <kbd>Alt+LEFT</kbd> Select next camera perspective
- <kbd>Alt+RIGHT</kbd> Select previous camera perspective
- <kbd>Alt+L</kbd> Toggle draw mode (line vs. shaded)
- <kbd>Q</kbd> Quit scene

Cheats:
  - <kbd>Alt+E</kbd> Eat all pills except the energizers
  - <kbd>Alt+L</kbd> Add 3 player lives
  - <kbd>Alt+N</kbd> Enter next game level
  - <kbd>Alt+X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

![Play Scene](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/pacman-maze.png)

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/left-info-panel.png)

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/right-info-panel.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://youtu.be/-ANLq4mMn3Q)
