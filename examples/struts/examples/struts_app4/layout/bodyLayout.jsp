<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<TABLE cellSpacing=0 cellPadding=0 width="100%" border=0>
	<TBODY>
	<TR>
	  <TD>

		<!-- HEADER -->
		<tiles:insert attribute="header"/>

	  </TD>
	</TR>
	<TR>
	  <TD>

	  	<!-- BODY -->
		<tiles:insert attribute="body"/>

	  </TD>
	</TR>
	</TBODY>
</TABLE>
