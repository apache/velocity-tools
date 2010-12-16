package org.apache.velocity.tools.plugin.taglib.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.annotations.DigesterLoader;
import org.apache.commons.digester.annotations.DigesterLoaderBuilder;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DigesterTest
{

    @Test
    public void testDigester() throws IOException, SAXException {
        DigesterLoader loader = new DigesterLoaderBuilder().useDefaultAnnotationRuleProviderFactory().useDefaultDigesterLoaderHandlerFactory();
        Digester digester = loader.createDigester(Taglib.class);
        digester.register("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN", getClass().getResource("/web-jsptaglibrary_1_2.dtd"));
        InputStream is = getClass().getResourceAsStream("/displaytag.tld");
        Taglib taglib = (Taglib) digester.parse(is);
        is.close();
        System.out.println(taglib);
    }

}
