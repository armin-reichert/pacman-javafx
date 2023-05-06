@echo off
rem Builds the Pac-Man Java FX executables (jar and zip files)
rem Git repositories "pacman-basic" and "pacman-javafx" must be locally available
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd pacman-ui-fx-3d
call mvn clean install
popd
pushd pacman-ui-fx-3d-jlink
call mvn clean install
popd