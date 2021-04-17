:: Usage: pacman.bat or pacman.bat -mspacman
::@ECHO OFF
@SETLOCAL
SET "JAVA_HOME=C:\Program Files\Java\jdk-15.0.2"
SET "JFX_LIB=C:\Program Files\Java\javafx-sdk-15.0.1\lib"
SET "PACMAN_LIB=..\..\pacman-basic\pacman-core\target\pacman-core-1.0.jar"
SET "PACMAN_UI_FX=.\target\pacman-ui-fx-1.0.jar"
SET CLASSPATH="%PACMAN_LIB%;%PACMAN_UI_FX%;%JFX_LIB%\javafx.base.jar;%JFX_LIB%\javafx.controls.jar;%JFX_LIB%\javafx.graphics.jar;..\interactivemesh\jars\jimObjModelImporterJFX.jar"
"%JAVA_HOME%"\bin\javaw.exe --module-path "%JFX_LIB%" --add-modules javafx.controls -cp %CLASSPATH% de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*
@ENDLOCAL