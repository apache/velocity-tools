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

Building the project requires JDK 1.3.1 or higher and ant 1.4.1 
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

To build only the VelocityLayoutServlet example, execute:

> ant layout


Please note:

- Due to new compile-time dependencies, VelocityTools can only be compiled on JDK 1.4 
  or higher.  But can still be run on JDK 1.3, provided you are willing to do without
  some newer features, like the BrowserSnifferTool.

- During the documentation build process DVSL emits several of the following error
  messages. They can be ingnored. A bug report has already been filed.

  [dvsl] [error] ResourceManager: unable to find resource 'VM_global_library.vm' in any resource loader.

- When compiling with JDK 1.4 there is a deprecation warning in LinkTool
  involving java.net.URLEncoder.  This is to maintain compatibility with JDK 1.3.
  However, when running the code on 1.4, the new, non-deprecated method is used.

The build process has been tested with JDK 1.4.2 and JDK 1.5.0. The 
included example applications have been tested with Tomcat 5.0.30 
and Tomcat 5.5.9, but should work with any servlet engine.

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
