R E A D M E
===========

This package contains a standalone servlet VelocityViewServlet that 
can be used for template rendering in Web applications. It is akin 
to the JSPServlet. It offers automatic population of the Velocity 
context and automatic, configurable management of view tools. 



Build Instructions
------------------

An ant script is provided to build and jar the package. Prerequisites are:

o Ant 1.4.1 or higher 
o JDK 1.3.1 or higher

Simply execute ant with the default target:

> ant 

This generates a jar file velocity-tools-view-*.jar in the local directory,
writes a copy of velocity-tools-view-*.jar to the project's jar library 
(../lib/), and generates the documentation under the docs directory.



Documentation
-------------
After the project has been built, the directory 'docs' contains some
minimal documentation. For now the best way to get a feel for the 
VelocityViewServlet and its capabilities is to look at the included 
application examples and the Javadoc.



Application Examples
--------------------

A simple application example has been included to demonstrate the use 
of the VelocityViewServlet with automatically loaded view tools.

To run the examples you need Tomcat 4.X or a compatible servlet runner.

The build process automatically generates a ready-to-deploy simple.war 
archive file of the included application example. The war file is 
located in the examples subdirectory. Deploy (copy) this simple.war file 
to the webapps directory of your servlet runner and restart. Now point 
a web browser at:

http://<server>:<port>/simple/

to access the examples.
 
 
  
Feedback
--------

We welcome your feedback to velocity-user@jakarta.apache.org.


$Revision: 1.6 $ $Date: 2002/06/23 09:45:16 $
