<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html>
<body bgcolor="white">

<h2>List HTTP Header Fields (JSP Version)</h2>

Display the values of the HTTP headers included in this request.<br><br>

<table border="1">
  <tr>
    <th>Header Name</th>
    <th>Header Value</th>
  </tr>

<logic:iterate id="name" collection="<%= request.getHeaderNames() %>" type="java.lang.String">

    <bean:header id="head" name="<%= name %>"/>
    <tr>
      <td><%= name %></td>
      <td><%= head %></td>
    </tr>

</logic:iterate> 

</table>

</body>
</html>
