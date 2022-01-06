How to install a jar file as a local Maven artifact, see

- https://www.eviltester.com/2017/10/maven-local-dependencies.html#install-jar-locally-to-your-m2-repository

- https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project


To create the Maven artifact for the OBJ importer, run:

	mvn install:install-file -Dfile=jars/jimObjModelImporterJFX.jar -DpomFile=pom.xml

or using the supplied batch file:

	install.bat
