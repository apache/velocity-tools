R E A D M E
===========

This package contains a standalone servlet that can be used for
template rendering in Web applications. It offers automatic population
of the Velocity context and automatic, configurable management of
view tools, and I18N support. Other efforts within this package are
the development of a toolbox manager and the definition of a set of
interfaces for view tools, thereby enabling the efficient handling
and reuse of view tools.


Build Package
-------------
An ant script is provided to build and jar the package. Prerequisites are:

o Ant 1.4.1 or higher 
o JDK 1.3.1 or higher

Simply execute ant with the default target:

> ant 

This generates a jar file velocity-tools-view-*.jar in the local directory,
writes a copy of velocity-tools-view-*.jar to the project's jar library 
(../lib/), and generates the documentation under the docs directory.



Application Example
-------------------

A simple application example has been included to demonstrate the use of the
VelocityViewServlet with automatically loaded view tools.

To run the example you need:

o Ant 1.4.1 or higher
o JDK 1.3.1 or higher
o Tomcat 3.X, Tomcat 4.X or a comparable servlet runner to run the
  examples.

To build a deployable version of the example follow these steps:

1) Build the View package as outlined in the first section
2) Change to directory examples/simple
3) Use ant to build a deployable version: 
   
   > ant
  
This generates a file simple.war file in directory 'examples'.
Deploy this .war file to the webapps directory of your servlet
runner and restart. Now point a web browser at:

http://<server>:<port>/simple/index.vm

  
Please send your feedback to velocity-user@jakarta.apache.org. 