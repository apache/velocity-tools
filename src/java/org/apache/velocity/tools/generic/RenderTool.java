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
 * @version $Revision: 1.4 $ $Date: 2003/11/20 05:43:18 $
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
    public static String eval(Context ctx, String vtl) throws Exception
    {
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
    public static String recurse(Context ctx, String vtl) throws Exception
    {
        String result = eval(ctx, vtl);
        if (result.equals(vtl))
        {
            return result;
        }
        else
        {
            return recurse(ctx, result);
        }
    }


}
