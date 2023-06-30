@echo off
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd pacman-ui-fx-2d
call mvn clean install
popd
rem pushd pacman-ui-fx-2d-jlink
rem call mvn clean install
rem popd