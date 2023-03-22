@echo off
rem Builds the Pac-Man Java FX implementation and deploys into a zip file
rem Expects that the Git repositories "pacman-basic" and "pacman-javafx" are locally available
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
rem pushd interactivemesh
rem call mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml
rem popd
pushd objimport
call mvn clean install
popd
pushd pacman-ui-fx
call mvn clean install
popd
pushd tentackle
call mvn clean install
popd
