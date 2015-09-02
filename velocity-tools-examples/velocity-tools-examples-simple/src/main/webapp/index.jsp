<%@taglib prefix="velocity" uri="http://velocity.apache.org/velocity-view" %>
<%--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.    
--%>
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
