@echo off
pushd pacman-ui-fx
start "Pac-Man Game" /min cmd /c target\jlink\bin\run.cmd
popd