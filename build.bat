@echo off
pushd interactivemesh
call mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml
popd
pushd pacman-ui-fx
call mvn clean install
popd
pushd pacman-ui-fx-deploy
call mvn clean install
popd