<%@page import="test.ssl.*" %>
<%@ taglib uri="/WEB-INF/sslext.tld" prefix="sslext"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<html>
<head>
</head>

<body>
<font size="+4"><center><%=request.getRequestURI()%></center></font>
<br>
<jsp:include page="top.html"/>
<br>
We are on the true page. The secure (SSL) action "true" forwards to this page.
<br>
<br>

Try the <sslext:link page="/false_jsp.do" >false</sslext:link> page.
<br>
Try the <sslext:link page="/any_jsp.do" >any</sslext:link> page.
<br>
<br>
Go to <sslext:link page="/formAction_jsp.do">form</sslext:link> test page.
</body>
</html>
