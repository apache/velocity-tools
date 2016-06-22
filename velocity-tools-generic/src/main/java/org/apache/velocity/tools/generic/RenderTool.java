package org.apache.velocity.tools.generic;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.StringWriter;

import org.slf4j.Logger;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * This tool exposes methods to evaluate the given
 * strings as VTL (Velocity Template Language)
 * using either a pre-configured context or one you
 * provide directly.
 * <pre>
 * Example of eval():
 *      Input
 *      -----
 *      #set( $list = [1,2,3] )
 *      #set( $object = '$list' )
 *      #set( $method = 'size()' )
 *      $render.eval("${object}.$method")
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
 *      $render.recurse($bar)
 *
 *      Output
 *      ------
 *      hello world!
 *
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.RenderTool"&gt;
 *       &lt;property name="parseDepth" type="number" value="10"/&gt;
 *     &lt;/tool&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>Ok, so these examples are really lame.  But, it seems like
 * someone out there is always asking how to do stuff like this
 * and we always tell them to write a tool.  Now we can just tell
 * them to use this tool.</p>
 *
 * <p>This tool may be used in any scope, however, the context provided
 * for the {@link #eval(String)} and {@link #recurse(String)} methods
 * will only be current if the tool is request scoped.  If application or
 * session scoped, then the context will be the same one set at the time
 * of the tool's first use. In such a case, each call to eval(String) or
 * recurse(String) will by default create a new Context that wraps the
 * configured one to prevent modifications to the configured Context
 * (concurrent or otherwise).  If you wish to risk it and accrete changes
 * then you can relax the thread-safety by setting the 'forceThreadSafe'
 * property to 'false'. </p>
 *
 * <p>Of course none of the previous paragraph likely applies if you are
 * not using the core tool management facilities or if you stick to the
 * {@link #eval(Context,String)} and {@link #recurse(Context,String)}
 * methods. :)</p>
 *
 * <p>This tool by default will catch
 * and log any exceptions thrown during rendering and
 * instead return null in such cases. It also limits recursion, by default,
 * to 20 cycles, to prevent infinite loops. Both settings may be configured
 * to behave otherwise.</p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 */
@DefaultKey("render")
public class RenderTool extends SafeConfig
{
    /**
     * The maximum number of loops allowed when recursing.
     * @since VelocityTools 1.2
     */
    public static final int DEFAULT_PARSE_DEPTH = 20;
    @Deprecated
    public static final String KEY_PARSE_DEPTH = "parse.depth";
    @Deprecated
    public static final String KEY_CATCH_EXCEPTIONS = "catch.exceptions";

    public static final String KEY_FORCE_THREAD_SAFE = "forceThreadSafe";

    protected Logger LOG = null;
    private VelocityEngine engine = null;
    private Context context;
    private int parseDepth = DEFAULT_PARSE_DEPTH;
    private boolean catchExceptions = true;
    private boolean forceThreadSafe = true;

    /**
     * Looks for deprecated parse depth and catch.exceptions properties,
     * as well as any 'forceThreadSafe' setting.
     */
    protected void configure(ValueParser parser)
    {
        // look for deprecated parse.depth key
        Integer depth = parser.getInteger(KEY_PARSE_DEPTH);
        if (depth != null)
        {
            setParseDepth(depth);
        }

        // look for deprecated catch.exceptions key
        Boolean catchEm = parser.getBoolean(KEY_CATCH_EXCEPTIONS);
        if (catchEm != null)
        {
            setCatchExceptions(catchEm);
        }

        // check if they want thread-safety manually turned off
        this.forceThreadSafe =
            parser.getBoolean(KEY_FORCE_THREAD_SAFE, forceThreadSafe);
        // if we're request-scoped, then there's no point in forcing the issue
        if (Scope.REQUEST.equals(parser.getString("scope")))
        {
            this.forceThreadSafe = false;
        }
        
        this.LOG = (Logger)parser.getValue(ToolContext.LOG_KEY);
    }

    /**
     * Allow user to specify a VelocityEngine to be used
     * in place of the Velocity singleton.
     */
    public void setVelocityEngine(VelocityEngine ve)
    {
        this.engine = ve;
        this.LOG = ve.getLog();
    }

    /**
     * Set the maximum number of loops allowed when recursing.
     *
     * @since VelocityTools 1.2
     */
    public void setParseDepth(int depth)
    {
        if (!isConfigLocked())
        {
            this.parseDepth = depth;
        }
        else if (this.parseDepth != depth)
        {
            if (LOG != null)
            {
                LOG.debug("RenderTool: Attempt was made to alter parse depth while config was locked.");
            }
        }
    }

    /**
     * Sets the {@link Context} to be used by the {@link #eval(String)}
     * and {@link #recurse(String)} methods.
     */
    public void setVelocityContext(Context context)
    {
        if (!isConfigLocked())
        {
            if (context == null)
            {
                throw new NullPointerException("context must not be null");
            }
            this.context = context;
        }
        else if (this.context != context)
        {
            if (LOG != null)
            {
                LOG.debug("RenderTool: Attempt was made to set a new context while config was locked.");
            }
        }
    }

    /**
     * Get the maximum number of loops allowed when recursing.
     *
     * @since VelocityTools 1.2
     */
    public int getParseDepth()
    {
        return this.parseDepth;
    }

    /**
     * Sets whether or not the render() and eval() methods should catch
     * exceptions during their execution or not.
     * @since VelocityTools 1.3
     */
    public void setCatchExceptions(boolean catchExceptions)
    {
        if (!isConfigLocked())
        {
            this.catchExceptions = catchExceptions;
        }
        else if (this.catchExceptions != catchExceptions)
        {
            if (LOG != null)
            {
                LOG.debug("RenderTool: Attempt was made to alter catchE while config was locked.");
            }
        }
    }

    /**
     * Returns <code>true</code> if this render() and eval() methods will
     * catch exceptions thrown during rendering.
     * @since VelocityTools 1.3
     */
    public boolean getCatchExceptions()
    {
        return this.catchExceptions;
    }

    /**
     * <p>Evaluates a String containing VTL using the context passed
     * to the {@link #setVelocityContext} method. If this tool is request
     * scoped, then this will be the current context and open to modification
     * by the rendered VTL.  If application or session scoped, the context
     * will be a new wrapper around the configured context to protect it
     * from modification.
     * The results of the rendering are returned as a String.  By default,
     * <code>null</code> will be returned when this throws an exception.
     * This evaluation is not recursive.</p>
     *
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String eval(String vtl) throws Exception
    {
        Context ctx = forceThreadSafe ? new VelocityContext(context) : context;
        return eval(ctx, vtl);
    }


    /**
     * <p>Recursively evaluates a String containing VTL using the
     * current context, and returns the result as a String. It
     * will continue to re-evaluate the output of the last
     * evaluation until an evaluation returns the same code
     * that was fed into it.</p>
     *
     * @see #eval(String)
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String recurse(String vtl) throws Exception
    {
        Context ctx = forceThreadSafe ? new VelocityContext(context) : context;
        return recurse(ctx, vtl);
    }

    /**
     * <p>Evaluates a String containing VTL using the current context,
     * and returns the result as a String.  By default if this fails, then
     * <code>null</code> will be returned, though this tool can be configured
     * to let Exceptions pass through. This evaluation is not recursive.</p>
     *
     * @param ctx the current Context
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String eval(Context ctx, String vtl) throws Exception
    {
        if (this.catchExceptions)
        {
            try
            {
                return internalEval(ctx, vtl);
            }
            catch (Exception e)
            {
                if (LOG != null)
                {
                    LOG.debug("RenderTool.eval(): failed due to ", e);
                }
                return null;
            }
        }
        else
        {
            return internalEval(ctx, vtl);
        }
    }


    /* Internal implementation of the eval() method function. */
    protected String internalEval(Context ctx, String vtl) throws Exception
    {
        if (vtl == null)
        {
            return null;
        }
        StringWriter sw = new StringWriter();
        boolean success;
        if (engine == null)
        {
            success = Velocity.evaluate(ctx, sw, "RenderTool.eval()", vtl);
        }
        else
        {
            success = engine.evaluate(ctx, sw, "RenderTool.eval()", vtl);
        }
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
     * that was fed into it or the number of recursive loops
     * exceeds the set parse depth.</p>
     *
     * @param ctx the current Context
     * @param vtl the code to be evaluated
     * @return the evaluated code as a String
     */
    public String recurse(Context ctx, String vtl) throws Exception
    {
        return internalRecurse(ctx, vtl, 0);
    }

    protected String internalRecurse(Context ctx, String vtl, int count) throws Exception
    {
        String result = eval(ctx, vtl);
        if (result == null || result.equals(vtl))
        {
            return result;
        }
        else
        {
            // if we haven't reached our parse depth...
            if (count < parseDepth)
            {
                // continue recursing
                return internalRecurse(ctx, result, count + 1);
            }
            else
            {
                // abort, log and return what we have so far
                if (LOG != null)
                {
                    LOG.debug("RenderTool.recurse() exceeded the maximum parse depth" +
                              " of {} on the following template: {}",
                              parseDepth, vtl);
                }
                return result;
            }
        }
    }
}
