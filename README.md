# A JavaFX UI (2D + 3D) for Pac-Man and Ms. Pac-Man

## ℹ️ Online version available

Both games (2D) can now also be played online:
- [GitHub](https://armin-reichert.github.io/webfx-pacman/)
- [WebFX Demo Pac-Man](https://pacman.webfx.dev/)
- [WebFX Demo Ms. Pac-Man](https://mspacman.webfx.dev/)

This has been made possible thanks to the [WebFX](https://webfx.dev/) transpiler technology (👏 to Bruno Salmon).

## Video

[![Pac-Man 3D Video](doc/pacman-maze.png)](https://magentacloud.de/s/qYDg6BKK7G6TxpB)

## About this project

JavaFX user interfaces for my UI-agnostic/faceless [Pac-Man and Ms. Pac-Man games](https://github.com/armin-reichert/pacman-basic). There is a 2D-only user interface and
a full version where the play scene can be switched between 2D and 3D, even during the gameplay (key combination <kbd>Alt+3</kbd>).

## How to run

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find executables/installers (for Windows, Linux and Mac-OS). On Windows, the application must be uninstalled first (if already installed), otherwise the installer crashes (no idea why). The Linux and Mac-OS installers/executables are completey untested.

## Build with Gradle
- `cd whateverpath/to/git/pacman-javafx`
- `gradlew jpackage` (builds and creates Windows installers, see directory `build/jpackage`)

### Running the application using Gradle
In the 2D or 3D subproject folder, call `gradlew run`. To create an executabe/installer for your platform, run `gradlew jpackage`. (Linux and Mac-OS versions are completely untested, any help appreciated!)

## Build with Maven

You need to have a [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) or newer and a local Maven client installed.

### Clone repository (only first time)
- `cd <repository-root>`
- `git clone https://github.com/armin-reichert/pacman-javafx.git`

### Build Windows executables
- `cd <repository-root>/pacman-javafx`
- `mvn_build.bat`  

or build separately:

### Build JavaFX user interface variants
- `cd <repository-root>/pacman-javafx\pacman-ui-fx-2d`
- `mvn install -Djavafx.platform=win` or 
- `mvn install -P build-for-windows` (Windows executables of 2D game) 
- `mvn install -Djavafx.platform=linux` or 
- `mvn install -P build-for-linux` (Linux executables of 2D game, untested)
- `cd <repository-root>/pacman-javafx\pacman-ui-fx-3d`
- `mvn install -Djavafx.platform=win` or 
- `mvn install -P build-for-windows` (Windows executables of 2D+3D game) 
- `mvn install -Djavafx.platform=linux` or 
- `mvn install -P build-for-linux` (Linux executables of 2D+3D game, untested)

To be able to create the Windows executables, you need to first install the following tools:

- [Inno Setup](https://jrsoftware.org/isinfo.php)
- [WiX toolset](https://wixtoolset.org/)

as described in the [JavaPackager guide](https://github.com/fvarrui/JavaPackager/blob/master/docs/windows-tools-guide.md) (:clap: to [Francisco Vargas Ruiz](https://github.com/fvarrui)).

I also had to add the paths "C:\Program Files (x86)\WiX Toolset v3.11\bin" and "C:\Program Files (x86)\Inno Setup 6" to my PATH variable.

### Linux executables (Untested, you're welcome to get this working)
To build the Linux executables, you need to have a Linux JDK on your computer. Edit the variable `linux.jdk.path` 
in the `pom.xml`files to point to your local path. Then run the `mvn_build_linux` batch file.

### Running the application using Maven
In the 2D or 3D subproject folder, call `mvn javafx:run`.


## How to use the application 

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


