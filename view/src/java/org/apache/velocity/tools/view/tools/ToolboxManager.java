/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.velocity.tools.view.tools;

import java.io.InputStream;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ViewContext;


/**
 *  Class to load and manage a toolbox, for now, slapped together
 *  from gabriel's latest.  Need to convert to XML.
 *
 *  Assumes a format like
 *
 *  <toolbox>
 *    <properties>
 *      <contextkey>toolbox</contextkey>
 *    </properties>
 *    <tools>
 *      <object>
 *         <contextkey>foo</contextkey>
 *         <class></class>
 *      </object>
 *      <integer>
 *          <value>10</value>
 *      </integer>
 *      <string>
 *          <value>Hello There</value>
 *      </string>
 *    </tools>
 *  </toolbox>
 */
public class ToolboxManager
{
    private SAXReader saxReader = null;
    private String contextKey = "";

    private ArrayList toolList = new ArrayList();

    public ToolboxManager()
    {
        saxReader = new SAXReader();
    }

    public void load( InputStream toolinput)
        throws Exception
    {
        Document document = saxReader.read( toolinput );

        setup( document );
     }


    protected void setup( Document doc )
        throws Exception
    {
        System.out.println(" TOOLBOX SETUP! ");

        List tools = doc.selectNodes( "//toolbox/tools/*" );

        Iterator it = tools.iterator();

        String classname = "";


        while( it.hasNext() )
        {
            try
            {
                Element e = (Element) it.next();

                String name = e.getName();

                System.out.println("Node : " + name );

                Map info = new HashMap();

                info.put("name", name );

                Node n = e.selectSingleNode("contextkey");

                System.out.println("Contextkey = " + n.getText() );

                info.put("contextkey", n.getText());

                n = e.selectSingleNode( "class" );

                classname = n.getText();

                System.out.println("Class = " + classname );

                info.put("class", classname );

                info.put("instance", Class.forName(classname).newInstance() );

                toolList.add( info );
            }
            catch( NoClassDefFoundError ncdfe )
            {
               System.out.println("Error creating classname : " + ncdfe );
            }
        }

        System.out.println("TOOLBOX LOAD COMPLETE");
    }

    public ToolboxContext getToolboxContext( ViewContext ctx)
    {
        Map toolbox = new HashMap();

        for (int i = 0; i < toolList.size(); i++)
        {
            Map info = (Map) toolList.get( i );

            Object o = info.get("instance");

            if ( o instanceof ContextTool )
            {
                o = ( (ContextTool) o).init( ctx );
            }

            toolbox.put( info.get("contextkey"), o );

            System.out.println("Loaded bean " + info.get("contextkey")
                    + " into context.");
        }

        return new ToolboxContext( toolbox );
    }

    public String getContextKey()
    {
        return contextKey;
    }
}
