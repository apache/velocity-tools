R E A D M E
===========

This part of jakarta-velocity-tools is focused on Struts-Velocity
integration using the VelocityViewServlet and other tools found
in this project. Struts support in Velocity is provided through 
several context tools. 

This is an alpha version. We do not recommend that you build production
systems based on this software at this stage. The API may still
change. We will produce a beta version when we believe that the API
is reasonably stable. 

Prerequisites are:

o Ant 1.4.1 or higher 
o JDK 1.3.1 or higher

To build the package:

> ant

This compiles the package, generates a jar file in the same directory 
and sets up the examples.

The directory 'docs' contains reference documentation for the included
context tools. More documentation will be added shortly. For now the 
best way to get a feel for the Struts Velocity integration is to look 
at the included examples.



Examples
--------

Several Struts application examples have been included to demonstrate
the use of Velocity templates with Struts. 

To run the examples you need:

o ant 1.4.1 or higher to build a deployable version of the example 
  applications (war file)
o JDK 1.3.1 or higher
o Tomcat 3.X, Tomcat 4.X or a comparable servlet runner to run the
  examples.

To build a deployable version of the examples follow these steps:

o Build the Struts package as outlined in the first section.
o Change to directory examples/struts
o Use ant to build a deployable version: > ant war
  
This will generate a velstruts.war file in directory 'examples'.
Deploy this .war file to the webapps directory of your servlet
runner and restart. Now point a web browser at:

http://<server>:<port>/velstruts/

to access the examples.
 
 
  
Please send your feedback to velocity-user@jakarta.apache.org. 