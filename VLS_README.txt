The quick and dirty guide to VLS (VelocityLayoutServlet)
--------------------------------------------------------


VLS - Objectives:

1. Provide easy layout control for velocity-tools based projects
2. Provide customizable error screens for said projects


VLS - Setup:

This is an extension of the VelocityViewServlet, so to use
it instead of the VVS, just change the servlet-class value of
your web.xml entry for the velocity servlet to: 

<servlet-class>org.apache.velocity.tools.view.VelocityLayoutServlet</servlet-class>

That means the full entry will be something like:

  <!-- Define Velocity template compiler -->
  <servlet>
    <servlet-name>velocity</servlet-name>
    <servlet-class>org.apache.velocity.tools.view.VelocityLayoutServlet</servlet-class>
    <load-on-startup>10</load-on-startup>
  </servlet>


If you enjoy configuring filenames and paths and would rather
not conform to the defaults, then you'll
want to know about these:

tools.view.servlet.error.template
tools.view.servlet.layout.directory
tools.view.servlet.layout.default.template

These are the velocity.properties keys
for configuring the behaviour of this dservlet.
the first specifies the filepath of the error template
relative to your webapp's root directory.
the second specifies the directory in which you will be placing
your layout templates.
the third specifies the filepath of the default layout template
relative to the layout directory, NOT relative to the
root directory of your webapp!

If you do not add any of these keys/values to your velocity.properties,
that will result in the equivalent of putting these default values in:

tools.view.servlet.error.template = Error.vm
tools.view.servlet.layout.directory = layout/
tools.view.servlet.layout.default.template = Default.vm


VLS - Layouts:

In your layout templates, the only thing you really need is the
screen content reference.  So an acceptable layout template could be
just

$screen_content

...but you'll probably want to do something along these lines:

<html>
<head>
  <title>$!page_title</title>
</head>
<body>

$screen_content

</body>
</html>

This saves you the trouble of doing the basic <html>,<head>, and <body>
tags in every single screen.  That's the point of layouts: to save effort
and eliminate redundancy.  Note that this still lets the inner screen
control the title of the page.  This works because the layout template
is blessed by the VLS with access to the same context as the screen *after*
the screen is done with it. Just do a #set( $page_title = "Hello" ) in the
screen.


                    Alternative Layouts

VLS provides two ways to specify an alternate template for a requested page:

1. Specify the layout in the request parameters

    Just add the query string "layout=MyOtherLayout.vm" to any request params
    and the VLS will find it and render your screen within that layout instead
    of the default layout.  It don't matter how you get the layout param into
    the query data, only that it's there.  If you're using the LinkTool, the 
    most common will likely be  
        <a href="$link.relative('MyScreen.vm').param('layout','MyOtherLayout.vm')">
        
    but form post data will work just as well.


2. Specify the layout in the requested screen.

    In the requested screen, put a line like this:
    #set( $layout = "MyOtherLayout.vm" )
    
    This will direct the VLS to use "MyOtherLayout.vm" instead of
    "Default.vm".  *Setting the layout in this fashion will
    override any layout set by the request parameters.*


                    Navigations, Tiles, and How

Those who are (or were) Turbine or Struts users will probably want to 
do more than just set the layout and screen content, and include
arbitrary "tiles" or "navigations" in the layout.  Thanks to Velocity's built-in
#parse directive, this is easy.

First, create your "tile" as a separate template file like:

<div id="footer">It's a footer!</div>

Now, assuming that this code is in a file named "Footer.vm"
located in the root of the webapp like any other non-layout templates.
You can include the footer like this:

<html>
<head>
  <title>$!page_title</title>
</head>
<body>

$screen_content

#parse('Footer.vm')

</body>
</html>

If you have a lot of different "footer" files and you want your screen
to decide which one will be used, do something like this:

<html>
<head>
  <title>$!page_title</title>
</head>
<body>

$screen_content

#parse( $screen_footer )

</body>
</html>

and in your screen, just do #set( $screen_header = 'FooFooter.vm' ).

Remember, your #parsed footer template will have access to the same 
velocity context as your layout, which gets the screen's context 
once the screen is done with it.  This lets you set variables for 
the layout and footer to use from your screens.


VLS - Error screen:

If an uncaught exception or error is thrown
at some point during the processing of your screen and layout, the error() method
of the VLS is called.  This overrides the default error() method of the VelocityServlet
to render a template instead of hardcoded html.

This error screen will be rendered within a layout under the same rules as any other
screen, and will have the following values placed in its context to help you debug
the error:

$error_cause

and 

$stack_trace


Their values are pretty much what you'd expect.  "$error_cause" is the java.lang.Throwable
that was thrown.   "$stack_trace" is the captured output of $cause.printStackTrace().

In the event that a MethodInvocationException is behind the calling of error(),
the root cause is extracted from it and dealt with as described above.  But, since
template reference behavior is partly at fault here, the VLS will also add the
MethodInvocationException itself to the context as $invocation_exception.  This
allows you to discover the reference and method call that triggered the root cause.
To get those, do something like this in your error template:

#if( $invocation_exception )
    oh joy! it's a MethodInvocationException!

    Message: $invocation_exception.message
    Reference name: $invocation_exception.referenceName
    Method name: $invocation_exception.methodName
#end
