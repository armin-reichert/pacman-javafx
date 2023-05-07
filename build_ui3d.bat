@echo off
@echo off
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd pacman-ui-fx
call mvn clean install
popd
pushd pacman-ui-fx-jlink
call mvn clean install
popd
pushd pacman-ui-fx-3d
call mvn clean install
popd
pushd pacman-ui-fx-3d-jlink
call mvn clean install
popd