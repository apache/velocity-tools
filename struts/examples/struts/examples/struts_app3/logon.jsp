<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html>
    <head>
        <title>Sign in, Please!</title>
        <html:base/>
    </head>

    <body>
    
        <html:errors/>

        <h3>Sign in, Please! (JSP Version)</h3>
        
        <html:form action="/logonSubmit_jsp" focus="username">

        <table border="0">

            <tr>
                <th align="right">
                    Username:
                </th>
                <td align="left">
                    <html:text property="username"/>
                </td>
            </tr>
        
            <tr>
                <th align="right">
                    Password:
                </th>
                <td align="left">
                    <html:password property="password"/>
                </td>
            </tr>
        
            <tr>
                <td align="right">
                    <html:submit property="submit" value="Submit"/>
                </td>
                <td align="left">
                    <html:reset/>
                </td>
            </tr>
        
        </table>
        
        </html:form>

        <a href="logon_jsp.txt">View Template</a><br>

    
    </body>
</html>

