## Exploratory Data analysis ENvironment (EDEN)

<img src="http://cda.ornl.gov/projects/eden/img/eden_logo.png" width="170" align="left" hspace="10" vspace="6">

**EDEN** is an interactive visual analytics tool for exploring quantitative multivariate data.  **EDEN** is written in Java and runs on Mac OS X, Windows, and Linux operating systems. **EDEN** is developed and maintained by the [Oak Ridge National Laboratory](http://www.ornl.gov) [Computational Data Analytics Group](http://cda.ornl.gov).  The lead developer is [Chad A. Steed](http://csteed.com/). 

If you are using **EDEN** for your work, we would greatly appreciate you citing the following paper:

Chad A. Steed, Daniel M. Ricciuto, Galen Shipman, Brian Smith, Peter E. Thornton, Dali Wang, and Dean N. Williams. Big Data Visual Analytics for Earth System Simulation Analysis. Computers & Geosciences, 61:71–82, 2013. doi:10.1016/j.cageo.2013.07.025  http://dx.doi.org/10.1016/j.cageo.2013.07.025

### Compiling the EDEN Source Code

Compiling **EDEN** is straightforward.  The first step is to clone the repository.  We supply a [Maven](http://maven.apache.org/) POM file to deal with the dependencies.  In the Eclipse development environment, import the code as a Maven project and Eclipse will build the class files.  

**EDEN** requires the [DataTable](https://github.com/csteed/datatable) library.  The DataTable library must first be compiled and packaged into a jar file.  Instructions for compiling DataTable are available on the DataTable repository.  After compiling DataTable into a jar file, the jar file must be installed in Maven.  Using the following command will install the DataTable file in the local Maven repository:

```
$ mvn install:install-file -Dfile=datatable-1.0.0.jar -DgroupId=gov.ornl.datatable -DartifactId=datatable -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
```

Optionally, the DataTable jar file can be specified with a file path in the pom.xml file instead of installing it in the local Maven repository.  

To compile **EDEN** on the command line, issue the following commands:

```
$ mvn compile
$ mvn package
```

### Running EDEN

These commands will generate 2 jar files in the target directory.  Copy the jar file with dependencies into the scripts directory and run either the eden.bat script (Windows) or the eden.sh script (Mac or Linux).  The **EDEN** GUI should appear after issuing this command.  Example data files are provided in the data directory for trying **EDEN** out.  

### Precompiled EDEN jar Files

Earlier precompiled releases of **EDEN** are available from this site under the [releases 
section](https://github.com/csteed/eden/releases).