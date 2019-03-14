package org.apache.velocity.tools.test.blackbox;

import org.apache.velocity.tools.view.BreadcrumbTool;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Breadcrumb tool tool tests
 *
 * @version $$
 * @author Claude Brisson
 * @since 3.1
 */

public class BreadcrumbToolTests extends BaseToolTests
{
    private BreadcrumbTool newBreadcrumbTool(String uri) throws Exception
    {
        return newBreadcrumbTool(new RequestAdaptor("", uri, null), new ResponseAdaptor());
    }

    private BreadcrumbTool newBreadcrumbTool(InvocationHandler requestHandler, InvocationHandler responseHandler) throws Exception
    {
        return newTool(BreadcrumbTool.class, requestHandler, responseHandler);
    }

    @Test
    public void testBreadcrumb() throws Exception
    {
        Map<String, Object> params = new HashMap<>();
        BreadcrumbTool bc = newBreadcrumbTool("/colors/red/nuances.vhtml");
        bc.configure(params);
        assertEquals("<a href=\"/index.vhtml\">home</a>&nbsp;&gt;&nbsp;<a href=\"/colors/index.vhtml\">colors</a>&nbsp;&gt;&nbsp;<a href=\"/colors/red/index.vhtml\">red</a>&nbsp;&gt;&nbsp;nuances", bc.toString());

        params.put("home.name", "index");
        params.put("home.url", "/welcome.vhtml");
        params.put("nuances.name", "shades");
        bc = newBreadcrumbTool("/colors/red/nuances.vhtml");
        bc.configure(params);
        assertEquals("<a href=\"/welcome.vhtml\">index</a>&nbsp;&gt;&nbsp;<a href=\"/colors/index.vhtml\">colors</a>&nbsp;&gt;&nbsp;<a href=\"/colors/red/index.vhtml\">red</a>&nbsp;&gt;&nbsp;shades", bc.toString());
    }
}
