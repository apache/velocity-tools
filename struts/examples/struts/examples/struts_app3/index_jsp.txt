<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%--

If user is logged in, display "Welcome ${username}!"
Else display "Welcome World!"
Display link to log in page; maintain session id if needed.
If user is logged in, display a link to the sign-out page.

Note: Only the minimum required html or Sruts custom tags 
are used in this example.

--%>

<html>
    <head>
        <title>Welcome World!!</title>
        <html:base/>
    </head>

    <body>
        <logic:present name="user">
        <h3>Welcome <bean:write name="user" property="username"/>! (JSP Version)</h3>
        </logic:present>
        
        <logic:notPresent scope="session" name="user">
        <h3>Welcome World! (JSP Version)</h3>
        </logic:notPresent>
        
        <html:errors/>
        
        <ul>
            <li><html:link forward="logon_jsp">Sign in</html:link></li>
            <logic:present name="user">
            <li><html:link forward="logoff_jsp">Sign out</html:link></li>
            </logic:present>
        </ul>

        <html:link forward="welcome_vm">Switch to Velocity</html:link><br>
        <a href="index_jsp.txt">View Template</a><br>
        
    </body>

</html>

