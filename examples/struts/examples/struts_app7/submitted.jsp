<%--
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: submitted.jsp,v 1.2 2004/02/20 13:31:36 marino Exp $
--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/sslext.tld" prefix="sslext"%>
<html>
<head>
</head>

<body>
<font size="+4"><center><%=request.getRequestURI()%></center></font>
<br>
We are on the submitted page.  These are the values that were posted:
<br>
<bean:write name="testForm" property="propA"/>
<br>
<bean:write name="testForm" property="propB"/>
<br>
<br>
Return to <sslext:link page="/formAction_jsp.do" >form</sslext:link> test page.
<br>
<br>
Go to <sslext:link page="/true_jsp.do" >true</sslext:link> page.
<br>
Go to <sslext:link page="/false_jsp.do" >false</sslext:link> page.
<br>
Go to <sslext:link page="/any_jsp.do" >any</sslext:link> page.
</body>
</html>
