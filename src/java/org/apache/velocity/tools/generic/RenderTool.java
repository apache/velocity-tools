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


import java.io.StringWriter;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;


/**
 * This tool exposes methods to evaluate the given
 * strings as VTL (Velocity Template Language)
 * using the given context.
 * <p>
 *   NOTE: These examples assume you have placed an
 *   instance of the current context within itself
 *   as 'ctx'. And, of course, the RenderTool is
 *   assumed to be available as 'render'.
 * </p>
 * <pre>
 * Example of eval():
 *      Input
 *      -----
 *      #set( $list = [1,2,3] )
 *      #set( $object = '$list' )
 *      #set( $method = 'size()' )
 *      $render.eval($ctx, "${object}.$method")
 *
 *      Output
 *      ------
 *      3
 * 
 * Example of recurse():
 *      Input
 *      -----
 *      #macro( say_hi )hello world!#end
 *      #set( $foo = '#say_hi()' )
 *      #set( $bar = '$foo' )
 *      $render.recurse($ctx, $bar)
 *
 *      Output
 *      ------
 *      hello world!
 *
 * </pre>
 *
 * <p>Ok, so these examples are really lame.  But, it seems like
 * someone out there is always asking how to do stuff like this
 * and we always tell them to write a tool.  Now we can just tell
 * them to use this tool.</p>
 *
 * <p>This tool is safe (and optimized) for use in the application
 * scope of a servlet environment.</p>
 * 
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.8 $ $Date: 2004/11/10 20:17:17 $
 */

public class RenderTool
{

    /**
     * Constructs a new instance
     */
    public RenderTool()
    {}

    private static final String LOG_TAG = "RenderTool.eval()";

    /**
     * <p>Evaluates a String containing VTL using the current context,
     * and returns the result as a String.  If this fails, then 
     * <code>null</code> will be returned.  This evaluation is not
     * recursive.</p>
     * 
     * @param ctx the current Context
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String eval(Context ctx, String vtl) throws Exception
    {
        /* don't waste time with null or empty strings */
        if (vtl == null || vtl.length() == 0)
        {
            return null;
        }
        StringWriter sw = new StringWriter();
        boolean success = Velocity.evaluate(ctx, sw, LOG_TAG, vtl);
        if (success)
        {
            return sw.toString();
        }
        /* or would it be preferable to return the original? */
        return null;
    }

    /**
     * <p>Recursively evaluates a String containing VTL using the
     * current context, and returns the result as a String. It
     * will continue to re-evaluate the output of the last
     * evaluation until an evaluation returns the same code
     * that was fed into it.</p>
     *
     * FIXME? add a parse-depth to prevent infinite recursion?
     * 
     * @param ctx the current Context
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String recurse(Context ctx, String vtl) throws Exception
    {
        String result = eval(ctx, vtl);
        if (result == null || result.equals(vtl))
        {
            return result;
        }
        else
        {
            return recurse(ctx, result);
        }
    }

}
