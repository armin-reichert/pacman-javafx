@echo off
pushd pacman-core
call mvn clean install
popd
pushd pacman-ui-fx-2d
call mvn install -Pbuild-for-linux
popd
pushd pacman-ui-fx-3d
call mvn install -Pbuild-for-linux
popd
