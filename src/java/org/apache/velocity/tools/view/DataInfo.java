/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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


package org.apache.velocity.tools.view;


/**
 * <p>ToolInfo implementation to handle "primitive" data types.
 * It currently supports String, Number, and Boolean data.</p>
 *
 * <p>An example of data elements specified in your toolbox.xml
 * might be:
 * <pre>
 *  &lt;data type="string"&gt;
 *    &lt;key&gt;app_name&lt;/key&gt;
 *    &lt;value&gt;FooWeb Deluxe&lt;/value&gt;
 *  &lt;/data&gt;
 *  &lt;data type="number"&gt;
 *    &lt;key&gt;app_version&lt;/key&gt;
 *    &lt;value&gt;4.2&lt;/value&gt;
 *  &lt;/data&gt;
 *  &lt;data type="boolean"&gt;
 *    &lt;key&gt;debug&lt;/key&gt;
 *    &lt;value&gt;true&lt;/value&gt;
 *  &lt;/data&gt;
 *  &lt;data type="number"&gt;
 *    &lt;key&gt;screen_width&lt;/key&gt;
 *    &lt;value&gt;400&lt;/value&gt;
 *  &lt;/data&gt;
 * </pre></p>
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: DataInfo.java,v 1.4 2003/07/22 18:29:50 nbubna Exp $
 */
public class DataInfo implements ToolInfo
{

    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_BOOLEAN = "boolean";

    private static final int TYPE_ID_STRING = 0;
    private static final int TYPE_ID_NUMBER = 1;
    private static final int TYPE_ID_BOOLEAN = 2;

    private String key;
    private int type_id;
    private Object data;


    public DataInfo() {}


    /***********************  Mutators *************************/

    public void setKey(String key)
    { 
        this.key = key;
    }


    public void setType(String type)
    { 
        if (TYPE_BOOLEAN.equalsIgnoreCase(type))
        {
            this.type_id = TYPE_ID_BOOLEAN;
        }
        else if (TYPE_NUMBER.equalsIgnoreCase(type))
        {
            this.type_id = TYPE_ID_NUMBER;
        }
        else /* if no type or type="string" */
        {
            this.type_id = TYPE_ID_STRING;
        }
    }


    public void setValue(String value)
    {
        if (type_id == TYPE_ID_BOOLEAN)
        {
            this.data = Boolean.valueOf(value);
        }
        else if (type_id == TYPE_ID_NUMBER)
        {
            if (value.indexOf('.') >= 0)
            {
                this.data = new Double(value);
            }
            else
            {
                this.data = new Integer(value);
            }
        }
        else /* type is "string" */
        {
            this.data = value;
        }
    }


    /***********************  Accessors *************************/

    public String getKey()
    {
        return key;
    }


    public String getClassname()
    {
        return data.getClass().getName();
    }


    /**
     * Returns the data. Always returns the same
     * object since the data is a constant. Initialization
     * data is ignored.
     */
    public Object getInstance(Object initData)
    {
        return data;
    }

}
