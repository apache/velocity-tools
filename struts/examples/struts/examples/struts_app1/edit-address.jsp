<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<html>
    <head>
    	<title>App1</title>
    	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    </head>
    
    <body>
        
        <p><b><bean:message key="edit"/> (JSP <bean:message key="version"/>)</b></p>
        
        <html:form name="address" method="POST" scope="session" type="examples.app1.AddressBean" action="address1.do">
        <input type="hidden" name="action" value="list">
        
        <table width="300" border="1" cellspacing="" cellpadding="5">
        	<tr> 
        		<td><bean:message key="firstname"/>:</td>
        		<td><html:text name="address" property="firstname"/></td>
        	</tr>
        	<tr> 
        		<td><bean:message key="lastname"/></td>
        		<td><html:text name="address" property="lastname"/></td>
        	</tr>
        	<tr> 
        		<td><bean:message key="street"/></td>
        		<td><html:text name="address" property="street"/></td>
        	</tr>
        	<tr> 
        		<td><bean:message key="zip"/></td>
        		<td><html:text name="address" property="zip"/></td>
        	</tr>
        	<tr> 
        		<td><bean:message key="city"/></td>
        		<td><html:text name="address" property="city"/></td>
        	</tr>
        	<tr> 
        		<td><bean:message key="country"/></td>
        		<td><html:text name="address" property="country"/></td>
        	</tr>
        </table>
        <br>
        
        <input type="submit" name="Submit" value="  <bean:message key="save"/>  " onclick="address.action.value='save'; document.address.submit(); return false;">
        <input type="submit" name="Submit2" value="<bean:message key="cancel"/>"  onclick="address.action.value='list'; document.address.submit(); return false;">
        
        </html:form>

        <br>
        <a href="examples/struts_app1/edit-addressjsp.txt">Template</a>

    
    </body>
</html>
