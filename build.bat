@echo off
rem Builds the Pac-Man Java FX implementation and deploys into a zip file
rem Expects that the Git repositories "pacman-basic" and "pacman-javafx" are locally available
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd interactivemesh
call mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml
popd
pushd pacman-ui-fx
call mvn clean install
popd
pushd tentackle
call mvn clean install
popd
