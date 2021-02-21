:: Usage: pacman.bat or pacman.bat -mspacman
@SETLOCAL

SET "JAVA_HOME=C:\Program Files\Java\jdk-15.0.2"
SET "JFX_LIB=C:\Program Files\Java\javafx-sdk-15.0.1\lib"
SET "JFX_PLATFORM=C:\Users\armin\.m2\repository"
SET APP=de.amr.games.pacman.ui.fx.app.PacManGameAppFX

SET "CLASSPATH=..\..\pacman-basic\pacman\target\pacman-basic-1.0.jar;.\target\pacman-ui-fx-1.0.jar;^
 "%JFXLIB%\javafx.base.jar";^
 "%JFXLIB%\javafx.controls.jar";^
 "%JFXLIB%\javafx.graphics.jar";
 "%JFX_PLATFORM%\org\openjfx\javafx-controls\15\javafx-controls-15.jar";^
 "%JFX_PLATFORM%\org\openjfx\javafx-controls\15\javafx-controls-15-win.jar";^
 "%JFX_PLATFORM%\org\openjfx\javafx-graphics\15\javafx-graphics-15.jar";^
 "%JFX_PLATFORM%\org\openjfx\javafx-graphics\15\javafx-graphics-15-win.jar";^
 "%JFX_PLATFORM%\org\openjfx\javafx-base\15\javafx-base-15.jar";^
 "%JFX_PLATFORM%\org\openjfx\javafx-base\15\javafx-base-15-win.jar""
 
"%JAVA_HOME%"\bin\javaw.exe --module-path "%JFX_LIB%" --add-modules javafx.controls -cp %CLASSPATH% %APP% %*

@ENDLOCAL