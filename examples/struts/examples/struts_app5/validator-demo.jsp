<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<html>
    <head>
	    <title>Struts App5: Struts ValidatorTool Demo (JSP Version)</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    </head>

    <body>
        <h2>Struts App5: Struts ValidatorTool Demo (JSP Version)</h2>
        <p>A demonstration of the Velocity ValidatorTool provided for Struts support.</p>

	<html:errors/>

        <html:form type="emailForm" action="do_submit_jsp" method="post" onsubmit="return validateEmailForm(this)">
            <html:text property="email"/>&nbsp;
            <html:submit/>
	</html:form>

    </body>
    <!-- if this line is commented out, the form will be validated server-side ... go ahead, try it :) -->
    <html:javascript formName="emailForm"/>
</html>
