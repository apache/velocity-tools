/*
 * Copyright 2003 The Apache Software Foundation.
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
 */

package org.apache.velocity.tools.generic;

/**
 * <p>A view tool that allows template designers to load
 * an arbitrary object into the context. Any object
 * with a public constructor without parameters can be used
 * as a view tool.</p>
 *
 * <p>THIS CLASS IS HERE AS A PROOF OF CONCEPT ONLY. IT IS NOT
 * INTENDED FOR USE IN PRODUCTION ENVIRONMENTS. USE AT YOUR OWN RISK.</p>
 * 
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ToolLoader.java,v 1.4 2004/02/18 20:11:07 nbubna Exp $
 * @deprecated This class will be removed after VelocityTools 1.1
 */

public class ToolLoader
{

    public ToolLoader()
    {
    }

    /**
     * Creates and returns an object of the specified classname.
     * The object must have a valid default constructor.
     *
     * @param clazz the fully qualified class name of the object
     * @return an instance of the specified class or null if the class
     *         could not be instantiated.
     */
    public Object load(String clazz)
    {
        try
        {
            return Class.forName(clazz).newInstance();
        }
        catch (Exception e)
        {
            return null; 
        }
    }

}
