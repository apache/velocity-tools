<%--
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: bodyLayout.jsp,v 1.2 2004/02/20 13:31:43 marino Exp $
--%>
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
