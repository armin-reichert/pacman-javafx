@echo off
pushd pacman-core
call mvn clean install
popd
pushd pacman-ui-fx-2d
call mvn clean install -Pbuild-for-windows
popd
pushd pacman-ui-fx-3d
call mvn clean install -Pbuild-for-windows
popd
