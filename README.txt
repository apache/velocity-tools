R E A D M E
===========

Contents
--------

view : A general-purpose servlet for rendering Velocity templates.
       There is no controller functionality - it's akin to the 
       JspServlet. It includes toolbox support. See view/README.txt
       for more information.

struts : Tools specific to integrating Velocity and Struts. See
       struts/README.txt for more information. There are several
       nice application examples included.

tools : A collection of general purpose context tools. See 
       tools/REAMDE.txt for more information.
       
       
       
Documentation
-------------

There is overview documentation and documentation for the different
components. To build the documentation follow these steps:

> ant docs
> ant javadocs
> ant javadocs-vellibrary

> cd view
> ant docs
> ant javadocs-velservlet

> cd ../struts
> ant docs

> cd ../tools
> ant docs

then, point your browser at docs/index.html

