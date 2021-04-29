:: Usage: pacman.bat or pacman.bat -mspacman
@ECHO OFF
@SETLOCAL

SET JAVA_HOME=C:\Program Files\Java\jdk-15.0.2
SET JFX_LIB=C:\Program Files\Java\javafx-sdk-15.0.1\lib
SET GIT=C:\Users\armin\git
SET MAVEN_REPOSITORY=C:\Users\armin\.m2\repository

SET "PACMAN_LIB=..\..\pacman-basic\pacman-core\target\pacman-core-1.0.jar"
SET "PACMAN_UI_FX=.\target\pacman-ui-fx-1.0.jar"

SET CLASSPATH="%PACMAN_LIB%;%PACMAN_UI_FX%;%JFX_LIB%\javafx.base.jar;%JFX_LIB%\javafx.controls.jar;%JFX_LIB%\javafx.media.jar;..\interactivemesh\jars\jimObjModelImporterJFX.jar"
::"%JAVA_HOME%"\bin\java.exe --module-path "%JFX_LIB%" --add-modules javafx.controls -cp %CLASSPATH% de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*


"%JAVA_HOME%\bin\javaw.exe" -Dfile.encoding=UTF-8 -p "%GIT%\pacman-javafx\pacman-ui-fx\target\classes;%GIT%\pacman-javafx\interactivemesh\jars\jimObjModelImporterJFX.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1-win.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1-win.jar;%GIT%\pacman-basic\pacman-core\target\classes" -classpath "%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1.jar;%MAVEN_REPOSITORY%\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1.jar" -m de.amr.games.pacman.ui.fx/de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*

@ENDLOCAL