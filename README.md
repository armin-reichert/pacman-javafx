# A JavaFX UI (2D + 3D) for Pac-Man and Ms. Pac-Man

## ℹ️ Online version available

Both games (2D) can now also be played online:
- [GitHub](https://armin-reichert.github.io/webfx-pacman/)
- [WebFX Demo Pac-Man](https://pacman.webfx.dev/)
- [WebFX Demo Ms. Pac-Man](https://mspacman.webfx.dev/)

This has been made possible thanks to the [WebFX](https://webfx.dev/) transpiler technology (:+1: to Bruno Salmon).

## Video

[![Pac-Man 3D Video](doc/pacman-maze.png)](https://magentacloud.de/s/qYDg6BKK7G6TxpB)

## About this project

JavaFX user interfaces for my UI-agnostic/faceless [Pac-Man and Ms. Pac-Man games](https://github.com/armin-reichert/pacman-basic). There is a 2D-only user interface and
a full version where the play scene can be switched between 2D and 3D, even during the gameplay (key combination <kbd>Alt+3</kbd>).

## How to run

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find executable jar files and
installers for Windows and Linux (not tested by me). If you have a Java runtime installed you can also start the game(s) by double-clicking the "fat" jar file.
Otherwise run the installer and double-click the new desktop icon(s).

## How to build locally

You need to have a [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) installed and ensure that this version is used by the following build steps.

### Clone repositories (only first time)
- `cd <repository-root>`
- `git clone https://github.com/armin-reichert/pacman-basic.git`
- `git clone https://github.com/armin-reichert/pacman-javafx.git`

### Build everything in one step
- `cd <repository-root>\pacman-javafx`
- build.bat  

or build separately

### Build core game (model and logic)
- `cd <repository-root>\pacman-basic\pacman-core`
- `mvn clean install` (builds game model and logic and runs unit tests)

### Build JavaFX user interface variants
- `cd <repository-root>\pacman-javafx\pacman-ui-fx-2d`
- `mvn install -Djavafx.platform=win` or `mvn install -P build-for-windows` (builds Windows executables of 2D game) 
- `mvn install -Djavafx.platform=linux`  or `mvn install -P build-for-linux` (builds Linux executables of 2D game)
- `cd <repository-root>\pacman-javafx\pacman-ui-fx-3d`
- `mvn install -Djavafx.platform=win` or `mvn install -P build-for-windows` (builds Windows executables of 2D+3D game) 
- `mvn install -Djavafx.platform=linux`  or `mvn install -P build-for-linux` (builds Linux executables of 2D+3D game)

To be able to create these executables you need to first install the following tools

- [Inno Setup](https://jrsoftware.org/isinfo.php)
- [WiX toolset](https://wixtoolset.org/)

as described in the [JavaPackager guide](https://github.com/fvarrui/JavaPackager/blob/master/docs/windows-tools-guide.md) (:+1: to [Francisco Vargas Ruiz](https://github.com/fvarrui)).

I also had to add the paths "C:\Program Files (x86)\WiX Toolset v3.11\bin" and "C:\Program Files (x86)\Inno Setup 6" to my PATH variable.

To build the Linux executables, you need to have a Linux JDK on your computer. Edit the variable `linux.jdk.path` in the `pom.xml`files to point to your local path.

After having build the executable it can be started via the command line by calling `mvn javafx:run`.

## How to use

Starting the game and switching game variant:
- <kbd>V</kbd> Switch between Pac-Man and Ms. Pac-Man (only possible on intro screen)
- <kbd>5</kbd> Add credit ("insert coin")
- <kbd>1</kbd> Start game
- <kbd>H</kbd>Show/hide context-sensitive help

Pac-Man steering:
- Pac-Man is steered using the cursor keys. When the dashboard is open, these keys are taken away by the JavaFX widgets. 
In that case, you can steer Pac-Man using key combination <kbd>CTRL</kbd>+cursor key.

General shortcuts:
- <kbd>F11</kbd> Enter fullscreen mode
- <kbd>Esc</kbd> Exit fullscreen mode
- <kbd>F1</kbd> or <kbd>Alt+B</kbd> Toggle dashboard
- <kbd>F2</kbd> Toggle picture-in-picture view
- <kbd>Alt+C</kbd> Play all intermission/cut scenes
- <kbd>Alt+3</kbd> Toggle using 2D/3D play scene

Play screen shortcuts:
- <kbd>Alt+LEFT</kbd> Select previous camera perspective
- <kbd>Alt+RIGHT</kbd> Select next camera perspective
- <kbd>Q</kbd>Quit play scene and show intro screen

Cheats:
  - <kbd>Alt+A</kbd> Toggle autopilot mode
  - <kbd>Alt+E</kbd> Eat all pills except the energizers
  - <kbd>Alt+I</kbd> Toggle immunity of player against ghost attacks
  - <kbd>Alt+L</kbd> Add 3 player lives
  - <kbd>Alt+N</kbd> Enter next game level
  - <kbd>Alt+X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

### 3D Play Scene

![Play Scene](doc/pacman-maze.png)

### Dashboard

![Dashboard](doc/dashboard-general.png)

![Dashboard](doc/dashboard-shortcuts.png)

![Dashboard](doc/dashboard-appearance.png)

![Dashboard](doc/dashboard-3d-settings.png)

![Dashboard](doc/dashboard-game-control.png)

![Dashboard](doc/dashboard-game-info.png)

![Dashboard](doc/dashboard-ghost-info.png)

![Dashboard](doc/dashboard-about.png)


