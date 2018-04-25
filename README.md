## Falcon

<img src="images/edenfx-screenshot.png" width="170" align="right" hspace="10" vspace="6">

**EDEN.fx** is an interactive visual analytics tool for exploring multivariate data.

**EDEN.fx** is written in Java and runs on Mac OS X, Windows, and Linux operating systems.
 The application is developed and maintained by the [Oak Ridge National Laboratory](http://www.ornl.gov)
 Computational Data Analytics Group.  The lead developer is [Dr. Chad A. Steed](http://csteed.com/).

**EDEN.fx** has the following software dependencies:
* Java SDK or JRE version 1.8 or higher
* Simple Logging Facade for Java (SLF4j) - a logging abstraction framework for Java (http://www.slf4j.org/).
* Apache Commons Math - a Java library of mathematical functions (http://commons.apache.org/proper/commons-math/).
* ControlsFX - a collection of high quality UI controls and other tools that complement the core JavaFX distribution.

### Compiling the EDEN.fx Source Code

Compiling **EDEN.fx** is straightforward.  The first step is to clone the repository.  We supply a [Maven](http://maven.apache.org/)
POM file to deal with the dependencies.  In the Eclipse development environment, import the code as a Maven project and
Eclipse will build the class files.  IntelliJ IDEA can import the code as a Maven project through similar procedures.

To compile **EDEN.fx** on the command line, issue the following commands (these commands assume that the Java SDK and
Maven are properly installed on the development system):

```
$ mvn clean
$ mvn compile
$ mvn package
```

These commands should produce several build files in a new subfolder named 'target'.  A jar file will be built
(named 'edenfx-1.1.0-jar-with-dependencies.jar') with all dependencies bundled and the main class set.  In Windows or
Mac, this jar file can be double-clicked to execute the main Falcon GUI.  However, running Falcon this way will use the
default Java JVM memory allocations, which are usually too small for even moderate sized files.  The preferred option
would be to use the script files provided in the main project folder to run the application with a more suitable Java
JVM memory allocation.

### Running EDEN

These commands will generate 2 jar files in the target directory.  Copy the jar file with dependencies into the scripts
directory and run either the edenfx.bat script (Windows) or the edenfx.sh script (Mac or Linux).  The **EDEN.fx** window
should appear after issuing this command.  Example data files are provided in the data directory.

A installer is provided for the OSX operating system.  After running the Maven package command, the installer will be
saved in the 'target/bundles' folder.  This file will have a '.pkg' extension.
This installer will guide the user through an installation
process copying the native application to the system Applications folder.  If the user is running on a Mac system, this
is the best way to use the tool.
