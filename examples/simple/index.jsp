<%@taglib prefix="velocity" uri="http://velocity.apache.org/velocity-view" %>
<html>
<body>

I'm a JSP file that uses the VelocityViewTag.

<velocity:view>
#if( $XHTML )
  #set( $br = "<br />" )
#else
  #set( $br = "<br>" )
#end

$br
$br

Here we use a custom tool: $toytool.message

$br
$br

Lets count : #foreach($i in [1..5])$i #end

$br
$br

Let's play with a hashmap:$br
first add foo: $map.put("foo",$foo)$br
then add bar: $map.put("bar",$bar)$br
$br
and that gives us $map

$br
$br

Here we get the date from the DateTool:  $date.medium

$br
$br

#if( $isSimple )
This is simple#if( $XHTML ) xhtml#end app version ${version}.
#end

$br
$br

Click <a href="index.vm">here</a> to see this VTL markup as a normal template.

</velocity:view>
</body>
</html>
