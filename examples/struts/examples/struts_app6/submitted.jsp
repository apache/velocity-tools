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
