Title: Apache Velocity Tools

Note : the trunk branch is obsolete. Please use the master branch.

# Apache Velocity Tools

Welcome to the VelocityTools projects. This is a subproject of the 
Apache Velocity project hosted at http://velocity.apache.org/

The VelocityTools project contains several subprojects:

    velocity-tools-generic/             A collection of general purpose tools
    velocity-tools-view/                Tools servlets and filters for use in a webapp
    velocity-tools-jsp/                 Tools for a J2EE JSP context
    velocity-tools-examples/
      velocity-tools-examples-simple/   Simple examples
      velocity-tools-examples-showcase/ A full webapp demonstrating tools

## REQUIREMENTS

Apache Velocity Tools will run with any Java runtime engine v1.7 or greater.

Building from source requires Java development kit v1.7 or greater and Maven 3 (3.0.5+).

Maven should fetch all needed dependencies for compile ant run time, which are:

* velocity-engine-core v2.0 and its dependencies
* commons-beanutils v1.9.3
* commons-digester3 v3.2
* JSON.simple v1.1.1 (only at compile time)

Plus the following ones, needed for the integrated tests:

* slf4j-simple v1.7.25
* junit v4.12
* easymock v3.6

## UPGRADING FROM EARLIER RELEASES

Release with the same major number are intended to be drop-in
replacements. However, in most cases the versions of dependency jars
must be adjusted because newer versions of Velocity might require
updates.

### Upgrading from Velocity Tools 2.0 to Velocity Tools 3.0

#### Dependency changes

* Velocity Tools now relies on the use of Velocity Engine 2.0, and also switched
to the slf4j logging system.
* commons-beanutils:commons-beanutils has been updated to 1.9.3
* org.apache.commons:commons-digester3 has been updated to 3.2
* com.googlecode.json-simple 1.1.1 is now needed (only at compile time)
* for tests, junit:junit has been updated to 4.12 and org.easymock:easymock to 3.6

#### Behavor / API changes

* tools autoloading turned off by default
    With velocity-tools-view in a webapp context, if you want the default
    tools loaded without explicitely loading them yourslef, you will have to enable autoloading in your `web.xml` file:
    
        <context-param>
          <param-name>org.apache.velocity.tools.loadDefaults</param-name>
          <param-value>true</param-value>
        </context-param>
    
    (or the same with `<init-param>` for a single servlet).
* the WebappResourceLoader, as other Engine loaders, now returns a Reader rather than an InputStream
* the unmaintained Struts tools have been dropped
* there are several new tools: LogTool, JsonTool, CollectionTool
* several tools became deprecated: AlternateTool, SortTool, ConversionTool (conversion methods are now located in DateTool and NumberTool)

You can consult [the full list of changes](http://velocity.apache.org/tools/3.0/changes.html)

## Upgrading from earlier versions

Please refer to [Tools 2.0 upgrading instructions](http://velocity.apache.org/tools/2.0/upgrading.html).

## Building Apache Velocity Tools

In order to use the latest version of Apache Velocity Tools, you may want to build it.

Building is easy.  All components necessary to build are included or get
downloaded from the internet during the build, except for the Java SDK and the Maven build tool.

**IMPORTANT** As the Apache Velocity Tools build process wants to download a number of jars
from the internet, you must be online when you are building for the first time.

To build the Velocity Tools' jars, just run maven using the command:

    mvn

This will create a `target/` directory containing the Velocity tools `.jar`
file in each sub-module directory.

## Feedback

We welcome your feedback to user@velocity.apache.org.

- The Apache Velocity Team
