<%@page import="test.ssl.*" %>
<%@ taglib uri="/WEB-INF/sslext.tld" prefix="sslext"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<html>
<head>
</head>

<body>
<font size="+4"><center><%=request.getRequestURI()%></center></font>
<br>
We are on the form page.  View the page source to see the difference in the action attribute values between the two forms.
<br>
<br>
<sslext:form action="/secureSubmit_jsp" >
This posts to a secure action.
<br>
   <html:text property="propA" value="" size="8" maxlength="8" />
<br>
   <html:text property="propB" value="" size="8" maxlength="8" />
<br>
<html:submit/>
</sslext:form>
<sslext:form action="/nonsecureSubmit_jsp" >
This posts to a non-secure action.
<br>
   <html:text property="propA" value="" size="8" maxlength="8" />
<br>
   <html:text property="propB" value="" size="8" maxlength="8" />
<br>
<html:submit/>
</sslext:form>
</body>
</html>
