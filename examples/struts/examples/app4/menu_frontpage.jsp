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
