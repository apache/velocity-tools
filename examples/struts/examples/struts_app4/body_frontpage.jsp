<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<TABLE cellSpacing=0 cellPadding=0 width="100%" border=1>
    <TR>
      	<TD align=middle> </TD>
    </TR>
    <TR>
      	<TD align=middle>This tile features a TilesController that put a variable "foo" in request scope
                 before the tile was rendered.  the variable contains the value: <bean:write name="foo" scope="request"/> </TD>
    </TR>
</TABLE>
