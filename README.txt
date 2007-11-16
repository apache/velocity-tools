R E A D M E
===========

Welcome to the VelocityTools projects. This is a subproject of the 
Apache Velocity project hosted at http://velocity.apache.org/
The VelocityTools project contains three subprojects:


VelocityStruts

    Includes tools specific to integrating Velocity and Struts. This
    package sits on top of (requires) the VelocityView package.

VelocityView

    Package containing the VelocityViewServlet for rendering Velocity
    templates. There is no controller functionality - it's akin to the 
    JspServlet. It includes toolbox support. (Also contains a
    VelocityLayoutServlet to support more advanced template rendering
    as an alternative to Tiles.)

Generic tools

    A collection of general purpose tools that may be used independently of
    the VelocityView or VelocityStruts package (but is included with both).



Build Instructions
------------------

Building the project requires JDK 1.5.1 or higher and ant 1.7.0 
or higher. There is an ant script included that builds the entire 
project, including all three subprojects, documentation, application 
examples, etc. To build the project, start ant in the root directory 
of the project:

> ant
    
To return the project to the original virgin state, execute:

> ant clean

By default, the project will build the VelocityStruts jar, javadoc and
project documentation.

To build only the VelocityStruts jar (which includes both VelocityView classes
and the generic tools), execute:

> ant jar.struts

To build only the VelocityView jar (which includes the generic tools), execute:

> ant jar.view

To build only the generic tools jar, execute:

> ant jar.generic

To build only the simple example for VelocityView, execute:

> ant simple

To build only the VelocityStruts example, execute:

> ant struts

To build only the Showcase example, execute:

> ant showcase


Please note:

- Due to new compile-time dependencies, VelocityTools can only be compiled on JDK 1.5 
  or higher

The build process has been tested with JDK 1.5.0. The 
included example applications have been tested with Tomcat 5.0.22,
but should work with any servlet engine.

If you observe any problems with the build process, please report this
to the Velocity users mailing list, user@velocity.apache.org, and
put [veltools] in the subject line.


       
Documentation
-------------

The project includes brief overview documentation and more detailed
documentation for each subproject. Follow the 'Build Instructions' to
build then project, then, point your browser at docs/index.html



Feedback
--------

We welcome your feedback to user@velocity.apache.org.


$Revision$ $Date$
