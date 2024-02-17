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

In the [release folder](https://github.com/armin-reichert/pacman-javafx/releases) you find executables/installers (for Windows). On Windows, the application must be 
uninstalled first (if already installed), otherwise the installer crashes (no idea why).

If you want to have installers for other platforms e.g. Linux or Mac-OS, ask someone who is able to do that. I find it much too
difficult and especialy much too tedious to achieve that (I am really too old for that shit).

## How to build

You need to have a [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) or newer installed on your computer.

### Clone repository (only first time)
 
- `cd /whatever/path/to/git`
- `git clone https://github.com/armin-reichert/pacman-javafx.git`

## Build project with Gradle

- `cd /whatever/path/to/git/pacman-javafx`
- `./gradlew jpackage` (builds and creates installer in directory `build/jpackage`)

### Running the application(s) using Gradle

- 2D app: `./gradlew pacman-ui-fx-2d:run`
- 3D app: `./gradlew pacman-ui-fx-3d:run`

## Build project with Maven

### Build Windows installers for both variants (2D and 3D)

- `cd /whatever/path/to/git/pacman-javafx`
- `mvnw clean install -Pbuild-for-windows`

### Build user interface variants separately

- `cd /whatever/path/to/git/pacman-javafx/pacman-ui-fx-2d`
- `mvnw install -Djavafx.platform=win`   or `mvnw install -P build-for-windows` (Windows executables of 2D game) 

- `cd /whatever/path/to/git/pacman-javafx/pacman-ui-fx-3d`
- `mvnw install -Djavafx.platform=win`   or `mvnw install -P build-for-windows` (Windows executables of 2D+3D game) 

To be able to create the Windows executables, you need to first install the following tools:

- [Inno Setup](https://jrsoftware.org/isinfo.php)
- [WiX toolset](https://wixtoolset.org/)

as described in the [JavaPackager guide](https://github.com/fvarrui/JavaPackager/blob/master/docs/windows-tools-guide.md) 
(:clap: to [Francisco Vargas Ruiz](https://github.com/fvarrui)).

I also had to add the paths "C:\Program Files (x86)\WiX Toolset v3.11\bin" and "C:\Program Files (x86)\Inno Setup 6" to my PATH variable.

### Running the application(s) using Maven

In the 2D or 3D subproject folder, call `..\mvnw javafx:run`.


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
- <kbd>F11</kbd> Enter full-screen mode
- <kbd>Esc</kbd> Exit full-screen mode
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


## Linux issues

Ubuntu in VMWare player under Windows 10.

```
armin@armin-virtual-machine:~$ lsb_release -a
No LSB modules are available.
Distributor ID:	Ubuntu
Description:	Ubuntu 22.04.3 LTS
Release:	22.04
Codename:	jammy

```

Starting from within IntelliJ after creating the jar using Gradle, the following exception occurs:
```
Exception in thread "Thread-4" com.sun.media.jfxmedia.MediaException: Could not create player!
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaManager.getPlayer(NativeMediaManager.java:299)
	at javafx.media@21.0.2/com.sun.media.jfxmedia.MediaManager.getPlayer(MediaManager.java:118)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer.play(NativeMediaAudioClipPlayer.java:319)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer.clipScheduler(NativeMediaAudioClipPlayer.java:112)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer$Enthreaderator.lambda$static$0(NativeMediaAudioClipPlayer.java:85)
	at java.base/java.lang.Thread.run(Thread.java:840)
```

See https://stackoverflow.com/questions/62619607/javafx-media-issue-on-ubuntu-could-not-create-player

Install FFMPEG (takes very long to install):

```
sudo apt-install ffmpeg
```

Now, 2D version works!

But 3D features are not available! (Is this caused by running inside VMWare?):
```
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:52 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
```

Found this: https://stackoverflow.com/questions/30288837/warning-system-cant-support-conditionalfeature-scene3d-vmware-ubuntu

Tried first: -Dprism.verbose=true

(Don't get how to pass system properties to gradlew call. Tried command-line, gradle.properties file, nothing worked.)

Ok, so let's try Maven. Call `mvn clean install -Pbuild-for-linux` first. Downloads half the internet, then gives
error message
```
Failed to execute goal io.github.fvarrui:javapackager:1.7.2:package (default) on project pacman-ui-fx-2d: JDK path doesn't exist: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/C:/dev/linux/jdk-17.0.7 -> [Help 1]
```

Fixed that (forgot to set linux.jdk.path in root "pom.xml" to the path used by Ubuntu/VMWare).

Then run `mvn clean install -Pbuild-for-linux`: Gives error

```
INFO] --- javapackager:1.7.2:package (default) @ pacman-ui-fx-2d ---
[WARNING] The POM for io.github.fvarrui:launch4j:jar:2.5.2 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details
[INFO] Using packager io.github.fvarrui.javapackager.packagers.LinuxPackager
[INFO] Creating app ...
[INFO]     Initializing packager ...
[INFO]         PackagerSettings [outputDirectory=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target, licenseFile=null, iconFile=null, generateInstaller=true, forceInstaller=false, mainClass=de.amr.games.pacman.ui.fx.Main, name=pacman-ui-fx-2d, displayName=pacman-ui-fx-2d, version=1.0, description=pacman-ui-fx-2d, url=null, administratorRequired=false, organizationName=Armin Reichert, organizationUrl=, organizationEmail=null, bundleJre=true, customizedJre=true, jrePath=null, jdkPath=/home/armin/.jdks/openjdk-21.0.2, additionalResources=[], modules=[], additionalModules=[], platform=linux, envPath=null, vmArgs=[], runnableJar=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar, copyDependencies=false, jreDirectoryName=jre, winConfig=null, linuxConfig=LinuxConfig [categories=[Utility], generateDeb=true, generateRpm=true, generateAppImage=false, pngFile=null, wrapJar=true], macConfig=null, createTarball=true, createZipball=false, extra=null, useResourcesAsWorkingDir=true, assetsDir=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/assets, classpath=null, jreMinVersion=null, manifest=null, additionalModulePaths=[], fileAssociations=[], packagingJdk=/home/armin/.jdks/openjdk-21.0.2, scripts=Scripts [bootstrap=null, preInstall=null, postInstall=null], arch=x64]
[INFO]     Packager initialized!
[INFO]     
[INFO]     Creating app structure ...
[INFO]         App folder created: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d
[INFO]         Assets folder created: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets
[INFO]     App structure created!
[INFO]     
[INFO]     Resolving resources ...
[INFO]         Trying to resolve license from POM ...
[INFO]         License not resolved!
[INFO]         
[WARNING]         No license file specified
[INFO]         Copying resource [/linux/default-icon.png] to file [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png]
[INFO]         Icon file resolved: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png
[INFO]         Effective additional resources [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png]
[INFO]     Resources resolved!
[INFO]     
[INFO]     Copying additional resources
[INFO]         Copying file [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png] to folder [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d]
[INFO]         Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && 'cp' /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/pacman-ui-fx-2d.png
[INFO]     All additional resources copied!
[INFO]     
[INFO]     Copying all dependencies ...
[INFO]     Dependencies copied to null!
[INFO]     
[INFO]     Using runnable JAR: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar
[INFO]     Bundling JRE ... with /home/armin/.jdks/openjdk-21.0.2
[INFO]         Creating customized JRE ...
[INFO]         Getting required modules ... 
[WARNING]             No dependencies found!
[INFO]             Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && '/home/armin/.jdks/openjdk-21.0.2/bin/jdeps' -q --multi-release 21 --ignore-missing-deps --print-module-deps /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar
[INFO]             java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported
[INFO]         Required modules found: [java.base, java.desktop, java.management, java.naming, java.sql, jdk.jfr, jdk.unsupported]
[INFO]         
[INFO]         Creating JRE with next modules included: java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported
[INFO]         Using /home/armin/.jdks/openjdk-21.0.2/jmods modules directory
[INFO]         Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && '/home/armin/.jdks/openjdk-21.0.2/bin/jlink' --module-path /home/armin/.jdks/openjdk-21.0.2/jmods --add-modules java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported --output /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/jre --no-header-files --no-man-pages --strip-debug --compress=2
[ERROR]         Warning: The 2 argument for --compress is deprecated and may be removed in a future release
[INFO]         Error: java.io.IOException: Cannot run program "objcopy": error=2, No such file or directory
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for pacman-all 1.0:
[INFO] 
[INFO] pacman-all ......................................... SUCCESS [  1.011 s]
[INFO] pacman-core ........................................ SUCCESS [ 14.501 s]
[INFO] pacman-ui-fx-2d .................................... FAILURE [ 19.852 s]
[INFO] pacman-ui-fx-3d .................................... SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  35.730 s
[INFO] Finished at: 2024-02-17T16:23:25+01:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal io.github.fvarrui:javapackager:1.7.2:package (default) on project pacman-ui-fx-2d: Command execution failed: /home/armin/.jdks/openjdk-21.0.2/bin/jlink --module-path /home/armin/.jdks/openjdk-21.0.2/jmods [Ljava.lang.String;@230de89b --add-modules java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported --output /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/jre --no-header-files --no-man-pages --strip-debug --compress=2 -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <args> -rf :pacman-ui-fx-2d
```

Ok, then just `mvn clean install`. Runs without error. Now

```
cd pacman-ui-fx-3d
../mvnw javafx:run -Dprism.verbose="true"
```

Does not work either (no PRISM output. WTF!). Maybe that wrapper is the reason`? So install Maven:

See https://linuxgenie.net/how-to-install-maven-on-ubuntu-22-04/

```
sudo apt install maven -y
```

(Downloads half of the internet...)

Now try with Maven client: `mvn javafx:run -Dprism.verbose="true"`

Same issue. No message from PRISM.








