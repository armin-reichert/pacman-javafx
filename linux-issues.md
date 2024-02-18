

## Linux issues

Ubuntu in VMWare player under Windows 10.

```
armin@armin-virtual-machine:~$ lsb_release -a
No LSB modules are available.
Distributor ID:	Ubuntu
Description:	Ubuntu 22.04.3 LTS
Release:	22.04
Codename:	jammy

```

Starting from within IntelliJ after creating the jar using Gradle, the following exception occurs:
```
Exception in thread "Thread-4" com.sun.media.jfxmedia.MediaException: Could not create player!
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaManager.getPlayer(NativeMediaManager.java:299)
	at javafx.media@21.0.2/com.sun.media.jfxmedia.MediaManager.getPlayer(MediaManager.java:118)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer.play(NativeMediaAudioClipPlayer.java:319)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer.clipScheduler(NativeMediaAudioClipPlayer.java:112)
	at javafx.media@21.0.2/com.sun.media.jfxmediaimpl.NativeMediaAudioClipPlayer$Enthreaderator.lambda$static$0(NativeMediaAudioClipPlayer.java:85)
	at java.base/java.lang.Thread.run(Thread.java:840)
```

See https://stackoverflow.com/questions/62619607/javafx-media-issue-on-ubuntu-could-not-create-player

Install FFMPEG (takes very long to install):

```
sudo apt-install ffmpeg
```

Now, 2D version works!

But 3D features are not available! (Is this caused by running inside VMWare?):
```
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.paint.Material <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:51 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
Feb 17, 2024 1:16:52 PM javafx.scene.shape.Mesh <init>
WARNING: System can't support ConditionalFeature.SCENE3D
```

Found this: https://stackoverflow.com/questions/30288837/warning-system-cant-support-conditionalfeature-scene3d-vmware-ubuntu

Tried first: -Dprism.verbose=true

(Don't get how to pass system properties to gradlew call. Tried command-line, gradle.properties file, nothing worked.)

Ok, so let's try Maven. Call `mvn clean install -Pbuild-for-linux` first. Downloads half the internet, then gives
error message
```
Failed to execute goal io.github.fvarrui:javapackager:1.7.2:package (default) on project pacman-ui-fx-2d: JDK path doesn't exist: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/C:/dev/linux/jdk-17.0.7 -> [Help 1]
```

Fixed that (forgot to set linux.jdk.path in root "pom.xml" to the path used by Ubuntu/VMWare).

Then run `mvn clean install -Pbuild-for-linux`: Gives error

```
INFO] --- javapackager:1.7.2:package (default) @ pacman-ui-fx-2d ---
[WARNING] The POM for io.github.fvarrui:launch4j:jar:2.5.2 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details
[INFO] Using packager io.github.fvarrui.javapackager.packagers.LinuxPackager
[INFO] Creating app ...
[INFO]     Initializing packager ...
[INFO]         PackagerSettings [outputDirectory=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target, licenseFile=null, iconFile=null, generateInstaller=true, forceInstaller=false, mainClass=de.amr.games.pacman.ui.fx.Main, name=pacman-ui-fx-2d, displayName=pacman-ui-fx-2d, version=1.0, description=pacman-ui-fx-2d, url=null, administratorRequired=false, organizationName=Armin Reichert, organizationUrl=, organizationEmail=null, bundleJre=true, customizedJre=true, jrePath=null, jdkPath=/home/armin/.jdks/openjdk-21.0.2, additionalResources=[], modules=[], additionalModules=[], platform=linux, envPath=null, vmArgs=[], runnableJar=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar, copyDependencies=false, jreDirectoryName=jre, winConfig=null, linuxConfig=LinuxConfig [categories=[Utility], generateDeb=true, generateRpm=true, generateAppImage=false, pngFile=null, wrapJar=true], macConfig=null, createTarball=true, createZipball=false, extra=null, useResourcesAsWorkingDir=true, assetsDir=/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/assets, classpath=null, jreMinVersion=null, manifest=null, additionalModulePaths=[], fileAssociations=[], packagingJdk=/home/armin/.jdks/openjdk-21.0.2, scripts=Scripts [bootstrap=null, preInstall=null, postInstall=null], arch=x64]
[INFO]     Packager initialized!
[INFO]     
[INFO]     Creating app structure ...
[INFO]         App folder created: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d
[INFO]         Assets folder created: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets
[INFO]     App structure created!
[INFO]     
[INFO]     Resolving resources ...
[INFO]         Trying to resolve license from POM ...
[INFO]         License not resolved!
[INFO]         
[WARNING]         No license file specified
[INFO]         Copying resource [/linux/default-icon.png] to file [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png]
[INFO]         Icon file resolved: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png
[INFO]         Effective additional resources [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png]
[INFO]     Resources resolved!
[INFO]     
[INFO]     Copying additional resources
[INFO]         Copying file [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png] to folder [/home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d]
[INFO]         Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && 'cp' /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/assets/pacman-ui-fx-2d.png /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/pacman-ui-fx-2d.png
[INFO]     All additional resources copied!
[INFO]     
[INFO]     Copying all dependencies ...
[INFO]     Dependencies copied to null!
[INFO]     
[INFO]     Using runnable JAR: /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar
[INFO]     Bundling JRE ... with /home/armin/.jdks/openjdk-21.0.2
[INFO]         Creating customized JRE ...
[INFO]         Getting required modules ... 
[WARNING]             No dependencies found!
[INFO]             Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && '/home/armin/.jdks/openjdk-21.0.2/bin/jdeps' -q --multi-release 21 --ignore-missing-deps --print-module-deps /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d-1.0-shaded.jar
[INFO]             java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported
[INFO]         Required modules found: [java.base, java.desktop, java.management, java.naming, java.sql, jdk.jfr, jdk.unsupported]
[INFO]         
[INFO]         Creating JRE with next modules included: java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported
[INFO]         Using /home/armin/.jdks/openjdk-21.0.2/jmods modules directory
[INFO]         Executing command: /bin/sh -c cd '/home/armin/IdeaProjects/pacman-javafx/.' && '/home/armin/.jdks/openjdk-21.0.2/bin/jlink' --module-path /home/armin/.jdks/openjdk-21.0.2/jmods --add-modules java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported --output /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/jre --no-header-files --no-man-pages --strip-debug --compress=2
[ERROR]         Warning: The 2 argument for --compress is deprecated and may be removed in a future release
[INFO]         Error: java.io.IOException: Cannot run program "objcopy": error=2, No such file or directory
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for pacman-all 1.0:
[INFO] 
[INFO] pacman-all ......................................... SUCCESS [  1.011 s]
[INFO] pacman-core ........................................ SUCCESS [ 14.501 s]
[INFO] pacman-ui-fx-2d .................................... FAILURE [ 19.852 s]
[INFO] pacman-ui-fx-3d .................................... SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  35.730 s
[INFO] Finished at: 2024-02-17T16:23:25+01:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal io.github.fvarrui:javapackager:1.7.2:package (default) on project pacman-ui-fx-2d: Command execution failed: /home/armin/.jdks/openjdk-21.0.2/bin/jlink --module-path /home/armin/.jdks/openjdk-21.0.2/jmods [Ljava.lang.String;@230de89b --add-modules java.base,java.desktop,java.management,java.naming,java.sql,jdk.jfr,jdk.unsupported --output /home/armin/IdeaProjects/pacman-javafx/pacman-ui-fx-2d/target/pacman-ui-fx-2d/jre --no-header-files --no-man-pages --strip-debug --compress=2 -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <args> -rf :pacman-ui-fx-2d
```

Ok, then just `mvn clean install`. Runs without error. Now

```
cd pacman-ui-fx-3d
../mvnw javafx:run -Dprism.verbose="true"
```

Does not work either (no PRISM output. WTF!). Maybe that wrapper is the reason`? So install Maven:

See https://linuxgenie.net/how-to-install-maven-on-ubuntu-22-04/

```
sudo apt install maven -y
```

(Downloads half of the internet...)

Now try with Maven client: `mvn javafx:run -Dprism.verbose="true"`

Same issue. No message from PRISM.

Ok, so try this: 

In IntelliJ, right-click over the "Main" class of the 3d subproject and select "More Run/Debug -> Modify Run Configuration..."

In the run configuration, add the VM parameters input field and enter `-Dprism.verbose=true -Dprism.forceGPU=true`.
Then run the application. And, you won't believe what you see in the console:

```
Prism pipeline init order: es2 sw 
Using Double Precision Marlin Rasterizer
Using dirty region optimizations
Not using texture mask for primitives
Not forcing power of 2 sizes for textures
Using hardware CLAMP_TO_ZERO mode
Opting in for HiDPI pixel scaling
Prism pipeline name = com.sun.prism.es2.ES2Pipeline
Loading ES2 native library ... prism_es2
	succeeded.
GLFactory using com.sun.prism.es2.X11GLFactory
(X) Got class = class com.sun.prism.es2.ES2Pipeline
Initialized prism pipeline: com.sun.prism.es2.ES2Pipeline
Maximum supported texture size: 16384
Maximum texture size clamped to 4096
Non power of two texture support = true
Maximum number of vertex attributes = 16
Maximum number of uniform vertex components = 16384
Maximum number of uniform fragment components = 16384
Maximum number of varying components = 124
Maximum number of texture units usable in a vertex shader = 16
Maximum number of texture units usable in a fragment shader = 16
Graphics Vendor: VMware, Inc.
       Renderer: SVGA3D; build: RELEASE;  LLVM;
        Version: 4.1 (Compatibility Profile) Mesa 23.2.1-1ubuntu3.1~22.04.2
ES2ResourceFactory: Prism - createStockShader: FillPgram_Color.frag
ES2ResourceFactory: Prism - createStockShader: Solid_TextureRGB.frag
ES2ResourceFactory: Prism - createStockShader: FillRoundRect_Color.frag
ES2ResourceFactory: Prism - createStockShader: Texture_Color.frag
PPSRenderer: scenario.effect - createShader: LinearConvolveShadow_20
        
```

and below (whatever that means):
```
vsync: true vpipe: true
```

But what matters: The 3D view is working in Ubuntu!
