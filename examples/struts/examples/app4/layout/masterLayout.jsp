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
 * $Id$
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

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
	<tiles:importAttribute name="someAttribute"/>
    <bean:message name="someAttribute"/>
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
