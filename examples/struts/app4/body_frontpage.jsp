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
