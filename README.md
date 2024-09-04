# A JavaFX UI (2D + 3D) for Pac-Man and Ms. Pac-Man

## ℹ️ Online version available

An older release of Pac-Man and Ms. Pac-Man (2D) can be played online:

- [GitHub](https://armin-reichert.github.io/webfx-pacman/)
- [WebFX Demo Pac-Man](https://pacman.webfx.dev/)
- [WebFX Demo Ms. Pac-Man](https://mspacman.webfx.dev/)

This has been made possible thanks to the [WebFX](https://webfx.dev/) transpiler technology (👏 to Bruno Salmon).

## About this project

This is an implementation of the classic Arcade games Pac-Man and Ms. Pac-Man in a JavaFX user interface. The game implementation is completely decoupled from the user interface such that also different user interfaces (e.g. a Swing UI) can be implemented without any change to the game code. (When developing the game, I originally maintained a Swing and a JavaFX UI in parallel to validate the UI abstraction, however the Swing UI got outdated and has been abandonded.)

There is a 2D-only user interface version and an extended version where the play scene can be switched between 2D and 3D, even during the gameplay (key combination <kbd>
Alt+3</kbd>). The game implementation tries to mimic the original Arcade version as good as possible, however there are some differences, e.g. in the "attract mode" behaviour, or the bonus behaviour in the Ms. Pac-Man game.

The game also contains a 3rd game variant (working title "Pac-Man XXL") with 8 maps (shemlessly stolen from the one and only Sean Williams, https://github.com/masonicGIT/pacman). To integrate these maps, I implemented a map editor that can either be used as a standalone application or called with the XXL version of the game). The nice thing is that alle these maps are playable in 2D and in 3D! You can also create maps with different size than the original Pac-Man maps, however this is still work in progress. Also the game can hang if the maps have dead-ends. So you have to be aware of that. The map editor has been used to create all the 8 new maps and you don't have to specify any graphics assets when adding a new map. Colors can be specified using map properties inside the editor.

## How to run

In each [release](https://github.com/armin-reichert/pacman-javafx/releases) you find attached 3 installers. 

- On Windows, a MSI installer is created. 
- The Linux (.deb) installer has only been tested on Ubuntu inside a VM, it worked after installing the FFMPeg library (see [linux-issues.md](doc/linux-issues.md)).
- The Mac-OS (.dmg) version has not been tested at all, any help is appreciated.

## How to build

Prerequisites: You need to have [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) and [Git](https://github.com/git-guides/install-git) installed on your computer.

- `git clone https://github.com/armin-reichert/pacman-javafx.git`
- `cd pacman-javafx`
- `./gradlew jpackage` or just `make` (Windows) or `./make.sh` (Linux)

This will create
- an installer (.msi/.deb/.dmg depending on your platform) for the 2D-only game (subdirectory `pacman-ui-2d/build/jpackage`)
- an installer for the full game (subdirectory `pacman-ui-3d/build/jpackage`)
- an installer for the map editor (subdirectory `pacman-mapeditor/build/jpackage`)

### Running the application(s) using Gradle

- Pac-Man 2D: `./gradlew pacman-ui-2d:run`
- Pac-Man 3D: `./gradlew pacman-ui-3d:run`

To be able to create the Windows executables, you need to have the following tools installed:

- [Inno Setup](https://jrsoftware.org/isinfo.php)
- [WiX toolset](https://wixtoolset.org/)

as described in the [JavaPackager guide](https://github.com/fvarrui/JavaPackager/blob/master/docs/windows-tools-guide.md)
(👏 to [Francisco Vargas Ruiz](https://github.com/fvarrui)).

I also had to add the paths "C:\Program Files (x86)\WiX Toolset v3.11\bin" and "C:\Program Files (x86)\Inno Setup 6" to my PATH variable.

## How to use the application

Start screen:
- <kbd>V</kbd>, <kbd>LEFT</kbd> Select next game variant
- <kbd>RIGHT</kbd> Select previous game variant
- <kbd>ENTER</kbd> or <kbd>SPACE</kbd> Start game 

Intro screen:
- <kbd>5</kbd> Add credit ("insert coin")
- <kbd>1</kbd> Start the game
- <kbd>H</kbd> Show/hide context-sensitive help

The keys <kbd>5</kbd> and <kbd>1</kbd> have been chosen because the [MAME](https://www.mamedev.org/) emulator uses them too.

Pac-Man steering:

- Pac-Man is steered using the *cursor keys*. When the dashboard is open, the cursor keys might also change slider values etc. To avoid this, you can also steer using <kbd>CTRL</kbd>+cursor key.

General shortcuts:

- <kbd>F11</kbd> Enter full-screen mode
- <kbd>Esc</kbd> Exit full-screen mode
- <kbd>Alt+3</kbd> Toggle 2D and 3D display of play scene
- <kbd>Q</kbd>Quit current scene and show start screen
- <kbd>Alt+M</kbd> Mute/unmute

Intro screen shortcuts:

- <kbd>Alt+C</kbd> Play the cut scenes

Play screen shortcuts:

- <kbd>F1</kbd> or <kbd>Alt+B</kbd> Toggle the dashboard display
- <kbd>F2</kbd> Toggle the picture-in-picture view
- <kbd>Alt+Shift+E</kbd> Open the Map Editor (available only for Pac-Man XXL game variant)
- <kbd>Alt+LEFT</kbd> Select previous camera perspective
- <kbd>Alt+RIGHT</kbd> Select next camera perspective

Cheats:

- <kbd>Alt+A</kbd> Toggle manual/autopilot steering mode
- <kbd>Alt+E</kbd> Eat all pellets (except the energizers)
- <kbd>Alt+I</kbd> Toggle immunity of player against ghost attacks
- <kbd>Alt+L</kbd> Add 3 player lives
- <kbd>Alt+N</kbd> Enter next game level
- <kbd>Alt+X</kbd> Kill all ghosts outside of the ghost house

## How it looks (Click to play video)
<div float="left">
    <a href="https://magentacloud.de/s/wiBT4sHy52dApYG">
        <img src="doc/pacman-maze.png" style="width:500px">
    </a>
</div>

### 2D Play Scenes

![Pac-Man Play Scene](doc/pacman-playscene-2d.png)

![Pac-Man Play Scene New](doc/pacman-newmaze-sample.png)

![Ms. Pac-Man Play Scene](doc/mspacman-playscene-2d.png)

### 3D Play Scenes

![Pac-Man Play Scene](doc/pacman-playscene.png)

![Pac-Man Play Scene New](doc/pacman-newmaze-sample-3d.png)

![Ms. Pac-Man Play Scene](doc/mspacman-maze.png)

### Tile map editor

![Map Editor](doc/map-editor.png)
