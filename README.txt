R E A D M E
===========

Welcome to the VelocityTools projects. This is a subproject of the 
Apache Velocity project hosted at http://jakarta.apache.org/velocity/
The VelocityTools project contains three subprojects:


VelocityServlet

    A general-purpose servlet for rendering Velocity templates.
    There is no controller functionality - it's akin to the 
    JspServlet. It includes toolbox support. See view/README.txt
    for more information.

VelocityLibrary

    A collection of general purpose view tools. See 
    library/REAMDE.txt for more information.

VelocityStruts

    Tools specific to integrating Velocity and Struts. See
    struts/README.txt for more information. There are several
    nice application examples included.



Build Instructions
------------------

Building the project requires JDK 1.3.1 or JDK 1.4.0 and ant 1.4.1 
or higher. There is an ant script included that builds the entire 
project, including all three subprojects, documentation, application 
examples, etc. To build the project, start ant in the root directory 
of the project:

> ant
    
To return the project to the original virgin state, execute:

> ant clean

Please note:

- During the build process DVSL emits several of the following error
  messages. They can be ingnored. A bug report has be filed.

  [dvsl] [error] ResourceManager: unable to find resource 'VM_global_library.vm' in any resource loader.

- When compiling with JDK 1.4.0 there are two deprecation warnings
  involving java.net.URLEncoder. They can't be fixed at the moment, 
  because then the software wouldn't compile with JDK 1.3.1 anymore.

The build process has been tested with JDK 1.3.1 and JDK 1.4.0. The 
included example applications have been tested with Tomcat 4.0.4 and
Resin 2.1.0.

If you observe any problems with the build process, please report this
to the Velocity users mailing list, velocity-user@jakarta.apache.org.      


       
Documentation
-------------

The project includes brief overview documentation and more detailed
documentation for each subproject. Follow the 'Build Instructions' to
build then project, then, point your browser at docs/index.html



Feedback
--------

We welcome your feedback to velocity-user@jakarta.apache.org.


$Revision: 1.7 $ $Date: 2002/06/23 09:45:16 $
