struts
======

This part of jakarta-velocity-tools is focused on Struts-Velocity
integration using the VelocityViewServlet and other tools found
in this project.

Struts support in Velocity is provided through several context
tools. 

To build the Struts tools :

 $ant jar

This generates a jar file in the same directory. 

More documentation will be added shortly. For now the best way
to get a feel for the Struts Velocity integration is to look at
the included examples.



Examples
--------

Several Struts application examples have been included to demonstrate
the use of Velocity templates with Struts. 


To run the examples you need:

o ant to build the web application (war file)
o Tomcat 3.X, Tomcat 4.X or a comparable servlet runner.


To build and deploy the example follow these steps:

o Go to subdirectory examples/struts
o Build the war file with:  
    $ant devwar 
  This generates a file velstruts.war in subdirectory examples. 
o Put the generated war file into the webapps directory of your Tomcat 
  installation (or the corresponding directory of other servlet runners). 
o Restart Tomcat to auto-deploy the application and point your browser at:
    http://your-server:your-port/velstruts/
  
  
  
Please send your feedback to velocity-user@jakarta.apache.org. 