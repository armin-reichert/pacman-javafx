::
:: Start script for the Pac-Man and Ms. Pac-Man JavaFX implementation
::
:: Pac-Man:     pacman.bat
:: Ms. Pac-Man: pacman.bat -mspacman
::

@echo off
@setlocal

set JDK=C:\Program Files\Java\jdk-15.0.2
set JFX=C:\Program Files\Java\javafx-sdk-15.0.1
set GIT=C:\Users\armin\git
set MAVEN_REPOSITORY=C:\Users\armin\.m2\repository

set pacman-core-jar=%GIT%\pacman-basic\pacman-core\target\pacman-core-1.0.jar
set pacman-ui-fx-jar=%GIT%\pacman-javafx\pacman-ui-fx\target\pacman-ui-fx-1.0.jar

set CLASSPATH="%pacman-core-jar%;^
%pacman-ui-fx-jar%;^
%JFX%\lib\javafx.base.jar;^
%JFX%\lib\javafx.controls.jar;^
%JFX%\lib\javafx.media.jar;^
..\interactivemesh\jars\jimObjModelImporterJFX.jar"

"%JDK%\bin\javaw.exe" -Dfile.encoding=UTF-8 -p "%GIT%\pacman-javafx\pacman-ui-fx\target\classes;%GIT%\pacman-javafx\interactivemesh\jars\jimObjModelImporterJFX.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1-win.jar;%GIT%\pacman-basic\pacman-core\target\classes" -classpath "%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1.jar" -m de.amr.games.pacman.ui.fx/de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*

@endlocal