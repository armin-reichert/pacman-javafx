@echo off
rem --- Runs the 2D-only version of the game ---
pushd pacman-ui-fx-jlink
start "Pac-Man Game" /min cmd /c target\jlink\bin\run.cmd
popd