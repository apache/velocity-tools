<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<HTML>
<HEAD>
    <TITLE><tiles:getAsString name="title"/></TITLE>
</HEAD>
<BODY>

<TABLE cellSpacing=0 cellPadding=0 width=70% align=center border=0>
  <TR>
	<TD valign="top" width="75" bgColor=#d9f1fc>

	<!-- MENU -->
	<tiles:insert attribute="menu"/>

    </TD>
    <TD>

	<!-- BODY -->
	<tiles:insert attribute="body"/>

    </TD>
  </TR>
  <tr>
    <td colspan="2">

	<!-- FOOTER -->
	<tiles:insert attribute="footer"/>

    </td>
  </tr>
</table>
</BODY>
</HTML>
