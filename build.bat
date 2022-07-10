@echo off
pushd ..\pacman-basic\pacman-core
call mvn clean install
popd
pushd interactivemesh
call mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml
popd
pushd pacman_ui_fx
call mvn clean install
popd
pushd tentackle
call mvn clean install
popd
