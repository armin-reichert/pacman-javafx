@echo off
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd pacman-ui-fx-2d
call mvn clean install
popd
pushd pacman-ui-fx-2d-jlink
call mvn clean install
popd