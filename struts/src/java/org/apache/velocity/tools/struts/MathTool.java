/*
 * ====================================================================
 * 
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

package org.apache.velocity.tools.struts;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ContextTool;

/**
 * A context tool for performing the more useful methods from 
 * <code>java.lang.Math</code>.
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>, based on
 *   code from <a href="mailto:ebr@tcdi.com">Eric B. Ridge</a>.
 *
 * @version $Revision: 1.4 $ $Date: 2002/01/12 07:28:30 $
 */

public class MathTool implements ContextTool 
{

    // -------------------------------------------- Properties ----------------
    
    // a singleton
    private static final MathTool _instance = new MathTool();

  
    
    // -------------------------------------------- Public Utility Methods ----

    /** 
     * The value of PI, as defined by <code>java.lang.Math.PI</code> 
     */
    public static final double PI = java.lang.Math.PI;

    /** 
     * @return The value of PI, as defined by <code>java.lang.Math.PI</code> 
     */
    public static final double pi() 
    { 
        return java.lang.Math.PI; 
    }
    
    /**
     * @return the smaller of the specified number
     */
    public static final int min(int a, int b)
    { 
        return (a<b)?a:b; 
    }
    
    /**
     * @return the smaller of the specified number
     */
    public static final long min(long a, long b)
    { 
        return (a<b)?a:b;
    }
    
    /**
     * @return the smaller of the specified number
     */
    public static final float min(float a, float b)
    { 
        return (a<b)?a:b; 
    }
        
    /**
     * @return the smaller of the specified number
     */
    public static final double min(double a, double b)
    { 
        return (a<b)?a:b; 
    }
    
    /**
     * @return the larger of the specified number
     */
    public static final int max(int a, int b)
    { 
        return (a>b)?a:b; 
    }
    
    /**
     * @return the larger of the specified number
     */
    public static final long max(long a, long b)
    { 
        return (a>b)?a:b; 
    }
    
    /**
     * @return the larger of the specified number
     */
    public static final float max(float a, float b)
    { 
        return (a>b)?a:b;
    }
    
    /**
     * @return the larger of the specified number
     */
    public static final double max(double a, double b)
    { 
        return (a>b)?a:b;
    }
    
    /**
     * Creates a pseudo-random Integer between <code>start</code>
     * and <code>end</code>, inclusive
     */
    public static final int random(int start, int end)
    {
        return start+((int) (1000000.0*java.lang.Math.random()) % end);
    }
    
    /**
     * @return <code>base</code> raised to the specified <code>power</code>
     */
    public static final int pow(int base, int power) 
    { 
        return (int)java.lang.Math.pow(base, power);
    }
    
    /**
     * @return <code>base</code> raised to the specified <code>power</code>
     */
    public static final long pow(long base, long power) 
    { 
        return (long)java.lang.Math.pow(base, power);
    }
    
    /**
     * @return <code>base</code> raised to the specified <code>power</code>
     */
    public static final float pow(float base, float power)
    { 
        return (float)java.lang.Math.pow(base, power); 
    }
    
    /**
     * @return <code>base</code> raised to the specified <code>power</code>
     */
    public static final double pow(double base, double power) 
    { 
        return java.lang.Math.pow(base, power); 
    }
    
    /**
     * @return the absolute value of the specified number
     */
    public static final int abs(int a)
    { 
        return java.lang.Math.abs(a); 
    }
    
    /**
     * @return the absolute value of the specified number
     */
    public static final long abs(long a)
    { 
        return java.lang.Math.abs(a); 
    }
    
    /**
     * @return the absolute value of the specified number
     */
    public static final float abs(float a)
    { 
        return java.lang.Math.abs(a); 
    }
    
    /**
     * @return the absolute value of the specified number
     */
    public static final double abs(double a)
    { 
        return java.lang.Math.abs(a); 
    }
        
    /**
     * @return <code>a</code> modulo <code>b</code>
     */
    public static final int mod(int a, int b) 
    { 
        return a%b; 
    }

    
    // -------------------------------------------- Constructors -------------
    
    /** 
     * Default contsructor. Does nothing.
     */
    public MathTool() 
    {
    }


    // -------------------------------------------- ContextTool Methods -------

    /**
     * Tool initialization method. The MathTool doesn't interact with the 
     * context, so the <code>context</code> parameter is ignored.
     */
    public Object init(ViewContext context)
    {
        return MathTool._instance;
    }

    /**
     * Perform necessary cleanup work
     */
    public void destroy(Object o) 
    {
    }

}
