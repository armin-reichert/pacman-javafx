@echo off
pushd pacman-ui-fx-3d
call mvn clean install
popd
pushd pacman-ui-fx-3d-jlink
call mvn clean install
popd