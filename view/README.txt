R E A D M E
===========

This package contains a standalone servlet that can be used for
template rendering in Web applications. It offers automatic population
of the Velocity context and automatic, configurable management of
view tools, and I18N support. Other efforts within this package are
the development of a toolbox manager and the definition of a set of
interfaces for view tools, thereby enabling the efficient handling
and reuse of view tools.


Build and JAR
-------------
An ant script is provided to build and jar the package.

> ant compile
> ant jar


Documentation
-------------
To generate the documentation for this package, follow these steps:

> ant docs
> ant javadocs
> ant javadocs-velservlet

Then look for the generated documentation in the 'doc' subdirectory.
