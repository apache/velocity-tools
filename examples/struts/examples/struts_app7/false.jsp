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
We are on the false page. The non-secure (non-SSL) action "false" forwards to this page.
<br>
<br>
Try the <sslext:link page="/true_jsp.do" >true</sslext:link> page.
<br>
Try the <sslext:link page="/any_jsp.do" >any</sslext:link> page.
<br>
<br>
Go to <sslext:link page="/formAction_jsp.do">form</sslext:link> test page.
</body>
</html>
