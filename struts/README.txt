R E A D M E
===========

This is the root directory of the VelocityStruts package. This 
package aims to make Velocity available as an alterative view
technology for the Apache Struts Web application framework.
Struts support in Velocity is provided through several view tools. 

This is an alpha version. The API may still change. We will produce 
a beta version when we believe that the API is reasonably stable. 



Build Instructions
------------------

See the README.txt file in the directory above the directory of 
this file.

The build process generates a velocity-tools-struts-*.jar file in
the directory where this README is located. This jar file contains
the classes needed for Velocity support in Struts.



Documentation
-------------

After the project has been built, the directory 'docs' contains 
reference documentation for the included view tools and an incomplete 
draft of a user guide. More documentation will be added shortly. For 
now the best way to get a feel for the Struts Velocity integration is 
to look at the included application examples.



Application Examples
--------------------

Several Struts application examples are included to demonstrate the 
use of Velocity templates with Struts. 

To run the examples you need Tomcat 4.X or a compatible servlet runner.

The build process automatically generates a ready-to-deploy war archive
file of the included application examples. The war file is located in 
the examples subdirectory. Deploy (copy) this .war file to the webapps 
directory of your servlet runner and restart. Now point a web browser 
at:

http://<server>:<port>/velstruts/

to access the examples.
 
 
  
Feedback
--------

We welcome your feedback to velocity-user@jakarta.apache.org.


$Revision: 1.6 $ $Date: 2002/06/23 09:45:16 $
