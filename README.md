# A JavaFX UI (2D + 3D) for Pac-Man and Ms. Pac-Man

## About this project

A JavaFX user interface for my UI-agnostic [Pac-Man / Ms. Pac-Man game](https://github.com/armin-reichert/pacman-basic) implementations. 

Both games can be played in 2D and 3D, you can switch between 2D and 3D by pressing key combination <kbd>Alt+3</kbd>.

## How to run the released version

### With Java runtime 18+ installed on your computer:
- Download the executable jar file `pacman-ui-fx-1.0-shaded.jar` from the [release folder](https://github.com/armin-reichert/pacman-javafx/releases). Start the application by double-clicking this file in the file explorer. 

### Without locally installed Java runtime:
- Download the zip file `pacman-javafx-tentackle-1.0-jlink.zip` from the release folder. Extract it somewhere and execute file `run.cmd` in folder `bin`.  

## How to build locally

Clone also repository [pacman-basic](https://github.com/armin-reichert/pacman-basic). Then

```
cd whatever\path\to\git\pacman-javafx 
build.bat
```

This script 
- runs a Maven build of the [pacman-core](https://github.com/armin-reichert/pacman-basic/tree/main/pacman-core) project (game logic and model),
- creates a Maven artifact wrapping the jar file of the OBJ file [importer](http://www.interactivemesh.org/models/jfx3dimporter.html), 
- runs a Maven build of the [pacman-ui-fx](pacman-ui-fx) project (user interface),
- creates the executable jar file `pacman-ui-fx-1.0-shaded.jar` in folder `pacman-javafx\pacman-ui-fx\target`,
- creates the zip file `pacman-javafx-tentackle-1.0-jlink.zip in folder `pacman-javafx\tentackle\target`.

## How to run the locally built version

If Java runtime 18+ is installed, you can run the executable jar by double-click. Alternatively, extract the zip file and execute the `run.cmd`file as described above. Or just execute the `run.bat`file in folder `pacman-javafx`.

## How to use

General shortcuts:

- <kbd>F11</kbd> Enter fullscreen mode
- <kbd>Esc</kbd> Exit fullscreen mode
- <kbd>F1</kbd> Toggle dashboard
- <kbd>F2</kbd> Toggle picture-in-picture view
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

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/3D-info-panel.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://youtu.be/-ANLq4mMn3Q)
