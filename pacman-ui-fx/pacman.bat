::
:: Start script for the Pac-Man and Ms. Pac-Man JavaFX implementation
::
:: Pac-Man:     pacman.bat
:: Ms. Pac-Man: pacman.bat -mspacman
::

@echo off
@setlocal

:: adapt these
set JDK=C:\Program Files\Java\jdk-15.0.2
set JFX=C:\Program Files\Java\javafx-sdk-15.0.2
set GIT=C:\Users\armin\git
set MVN_REPO=C:\Users\armin\.m2\repository
set MVN_JFX_VERSION=15.0.1
set pacman-ui-fx-jar=%GIT%\pacman-javafx\pacman-ui-fx\target\pacman-ui-fx-1.0.jar

"%JDK%\bin\javaw.exe" -Dfile.encoding=UTF-8 -p "%pacman-ui-fx-jar%;%GIT%\pacman-javafx\interactivemesh\jars\jimObjModelImporterJFX.jar;%MVN_REPO%\org\openjfx\javafx-controls\%MVN_JFX_VERSION%\javafx-controls-%MVN_JFX_VERSION%-win.jar;%MVN_REPO%\org\openjfx\javafx-graphics\%MVN_JFX_VERSION%\javafx-graphics-%MVN_JFX_VERSION%-win.jar;%MVN_REPO%\org\openjfx\javafx-base\%MVN_JFX_VERSION%\javafx-base-%MVN_JFX_VERSION%-win.jar;%MVN_REPO%\org\openjfx\javafx-media\%MVN_JFX_VERSION%\javafx-media-%MVN_JFX_VERSION%-win.jar;%GIT%\pacman-basic\pacman-core\target\classes" -classpath "%MVN_REPO%\org\openjfx\javafx-controls\%MVN_JFX_VERSION%\javafx-controls-%MVN_JFX_VERSION%.jar;%MVN_REPO%\org\openjfx\javafx-graphics\%MVN_JFX_VERSION%\javafx-graphics-%MVN_JFX_VERSION%.jar;%MVN_REPO%\org\openjfx\javafx-base\%MVN_JFX_VERSION%\javafx-base-%MVN_JFX_VERSION%.jar;%MVN_REPO%\org\openjfx\javafx-media\%MVN_JFX_VERSION%\javafx-media-%MVN_JFX_VERSION%.jar" -m de.amr.games.pacman.ui.fx/de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*

@endlocal