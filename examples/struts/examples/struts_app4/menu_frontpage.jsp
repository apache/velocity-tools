<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<!-- import the menu items into page scope -->
<tiles:importAttribute name="items"/>

<TABLE width="100%" border=1>
<TBODY>
	<TR>
            <TD>
                <logic:iterate id="item" name="items">
                    <a href='<bean:write name="item" property="link"/>'><bean:write name="item" property="value"/></a><br>
                </logic:iterate>
            </TD>
	</TR>
</TBODY>
</TABLE>
