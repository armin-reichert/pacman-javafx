## How to create the deployable archive

To create a zip file containing the Java/JavaFX runtime modules and the application, run `mvn -f deploy.xml install`.

To run the application, unzip the archive, then execute file `run.cmd` in folder `bin`.

## Tentackle help

Tentackle JLink and JPackage Maven Plugin 17.11.0.0
  Maven Plugin to Create Self-Contained Applications

This plugin has 4 goals:

tentackle-jlink:help
  Display help information on tentackle-jlink-maven-plugin.
  Call mvn tentackle-jlink:help -Ddetail=true -Dgoal=<goal-name> to display
  parameter details.

tentackle-jlink:init
  Initializes the templates.
  Copies the default templates to the project's template directory.

tentackle-jlink:jlink
  Creates a self-contained java application with the jlink tool.
  This mojo works for modular, non-modular and even mixed applications. It's not
  just a wrapper for jlink, but analyzes the project's dependencies and finds
  the best strategy to invoke jlink to create a directory containing an
  application-specific jimage module. As a result, it requires only minimum
  configuration.
  
  Basically, applications fall into one of 3 categories:
  
  1.  Full-blown modular applications: all module-infos must require real
    modules only. Jlink creates an image from those modules. Optional artifacts
    and runtime modules can still be added.
  2.  Modular applications with non-modular dependencies: jlink is used to
    create an image from the minimum necessary java runtime modules only, which
    are determined by the plugin either from the module-infos or via the jdeps
    tool. The application's dependencies are placed on the modulepath via the
    generated run-script.
  3.  Non-modular traditional classpath applications: same as 2, but all
    dependencies are placed on the classpath.
  Since it is very likely, that even modern modular applications require some
  3rd-party dependencies not modularized yet, most of those applications will
  probably fall into the second category.
  Artifacts not processed by jlink are copied to separate folders and passed to
  the java runtime explicitly via the module- and/or classpath. A
  platform-specific launch script will be generated according to the
  runTemplate. For applications using Tentackle's auto update feature, an update
  script is generated via the updateTemplate as well. Finally, the created
  directory is packed into a deployable zip file.
  
  The minimum plugin configuration is very simple:
  
```
...
   <packaging>jlink</packaging>
   ...
   <plugin>
   <groupId>org.tentackle</groupId>
   <artifactId>tentackle-jlink-maven-plugin</artifactId>
   <version>${tentackle.version}</version>
   <extensions>true</extensions>
   <configuration>
   <mainModule>com.example</mainModule>
   <mainClass>com.example.MyApp</mainClass>
   </configuration>
   </plugin>
```
The freemarker templates are copied to the project's template folder, if
missing. They become part of the project and can be changed easily according
to project specific needs (for example by adding runtime arguments). To
install and edit the templates before running jlink (or jpackage, see
JPackageMojo), use InitMojo first.
The template model provides the following variables:

- mainModule: the name of the main module. Empty if classpath application.
- mainClass: the name of the main class.
- modulePath: the module path.
- classPath: the class path
- phase: the mojo lifecycle phase
- goal: the plugin goal (jlink or jpackage)
- id: the execution id
- all system properties (dots in property names translated to camelCase, e.g.
  'os.name' becomes 'osName'
- all maven properties (translated to camelCase as well)
- the plugin configuration variables
Modules not passed to jlink and automatic modules are copied to the mp folder
and added to the modulePath template variable. If no such modules are
detected, no folder is created.
Non-modular classpath artifacts are copied to the cp folder and added to the
classPath template variable. Again, the folder is only created if necessary.
Additional project resources, such as property files or logger configurations,
are copied to the conf directory and this directory is prepended to the
classpath.
The generation of the ZIP-file and attachment of the artifact for installation
and deployment can be customized by an application-specific implementation.
This allows further modification of the generated image or files in the jlink
target directory. It is also possible to add more than one artifact, for
example, each with a different configuration. To do so, provide a plugin
dependency that contains a class annotated with @Service(ArtifactCreator).

Notice that you can create an image for a different java version than the one
used for the maven build process by specifying an explicit jdkToolchain.

tentackle-jlink:jpackage
Creates a java application installer with the jpackage tool.
The mojo works in 4 phases:

1.  Invokes the jlink tool as described in JLinkMojo. This will generate a
  directory holding the runtime image. However, no run or update scripts and
  no zip file will be created.
2.  Invokes the jpackage tool to generate the application image from the
  previously created runtime image. Application- and platform specific options
  can be configured via the packageImageTemplate.
3.  If the runtime image contains extra classpath- or modulepath-elements, the
  generated config files will be patched. This is especially necessary to
  provide the correct classpath order according to the maven/module dependency
  tree, which usually differs from the one determined by jpackage, because
  jpackage has no idea about the maven project structure and does its own
  guess according to the packages referenced from within the jars. This may
  become an issue if the classpath order is critical, such as configurations
  overridden in META-INF.
4.  Finally, the installer will be generated from the application image. The
  packageInstallerTemplate is used to provide additional options to the
  jpackage tool.
The minimum plugin configuration is very simple:
 ...
 <packaging>jpackage</packaging>
 ...
 <plugin>
 <groupId>org.tentackle</groupId>
 <artifactId>tentackle-jlink-maven-plugin</artifactId>
 <version>${tentackle.version}</version>
 <extensions>true</extensions>
 <configuration>
 <mainModule>com.example</mainModule>
 <mainClass>com.example.MyApp</mainClass>
 </configuration>
 </plugin>
The freemarker templates are copied to the project's template folder, if
missing. They become part of the project and can be changed easily according
to project specific needs (for example by adding runtime arguments). To
install and edit the templates before running jpackage (or jlink, see
JLinkMojo), use InitMojo first. In addition to the template variables defined
by the JLinkMojo, the variable runtimeDir is provided pointing to the runtime
image directory (which is platform specific).
If the application is built with Tentackle's update feature, please keep in
mind that applications deployed by an installer are maintained by a platform
specific package manager. If the installation is system-wide, the installation
files cannot be changed by a regular user. Some platforms, however, also
provide per-user installations that can be updated. For Windows, the jpackage
tool provides the option --win-per-user-install. MacOS allows the user to
decide whether to install system-wide or for the current user only. See
withUpdater for more details.

If both jlink zip-files and jpackage installers are required, change the
packaging type to jar and add executions, like this:
```
 <executions>
 <execution>
 <id>both</id>
 <goals>
 <goal>jlink</goal>
 <goal>jpackage</goal>
 </goals>
 </execution>
 </executions>
```
The jpackage goal will then re-use the previously created jlink image.
The contents of the application image and attachment of the artifacts for
installation and deployment can be customized by an application-specific
implementation. To do so, provide a plugin dependency that contains a class
annotated with @Service(ArtifactCreator).

Important: the jpackage tool is available since Java 14.

Notice that you can create an image for a different java version than the one
used by the maven build process via jdkToolchain. Furthermore, you can select
the jpackage tool explicitly from another JDK via jpackageToolchain or
jpackageTool.

