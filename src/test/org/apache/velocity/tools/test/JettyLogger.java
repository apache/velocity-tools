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

package org.apache.velocity.tools.test;

import java.io.PrintWriter;
import java.io.IOException;

import org.mortbay.log.Logger;
import org.mortbay.util.DateCache;

/** Basic Jetty logger for our showcase webapp
 *
 *  @author <a href=mailto:cbrisson@apache.org>Claude Brisson</a>
 */

public class JettyLogger implements Logger {

    private boolean debug = false;
    private String name = null;
    private DateCache _dateCache=new DateCache("yyyy-MM-dd HH:mm:ss.SSS");

    private static PrintWriter out = null;

    static {
        try {
            String logfile = System.getProperty("jetty.log.file","/tmp/error.log");
            out = new PrintWriter(logfile);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public JettyLogger() {
        this(null);
    }

    public JettyLogger(String name) {
        this.name = name==null? "" : name;
    }

    /*
     * org.mortbay.log.Logger interface
     */

    public boolean isDebugEnabled() {
        return debug;
    }

    public void setDebugEnabled(boolean enabled) {
        debug = enabled;
    }

    public void info(String msg,Object arg0, Object arg1)
    {
        if (out == null) return;
        /* a bit of filtering in debug mode */
        if (debug && (msg.startsWith("loaded class") || msg.startsWith("loaded interface"))) {
            return;
        }
        logString(_dateCache.now() + " " + name + " " + format(msg,arg0,arg1));
    }

    public void debug(String msg,Throwable th)
    {
        if (debug)
        {
            if (out == null) return;
            /* a bit of filtering in debug mode */
            if (debug && (msg.startsWith("loaded class") || msg.startsWith("loaded interface"))) {
                return;
            }
            logString(_dateCache.now()+" "+msg);
            logStackTrace(th);
        }
    }

    public void debug(String msg,Object arg0, Object arg1)
    {
        if (debug)
        {
            if (out == null) return;
            /* a bit of filtering in debug mode */
            if (debug && (msg.startsWith("loaded class") || msg.startsWith("loaded interface"))) {
                return;
            }
            logString(_dateCache.now()+" "+format(msg,arg0,arg1));
        }
    }

    public void warn(String msg,Object arg0, Object arg1)
    {
        if (out == null) return;
        logString(_dateCache.now()+" "+format(msg,arg0,arg1));
    }

    public void warn(String msg, Throwable th)
    {
        if (out == null) return;
        logString(_dateCache.now()+" "+msg);
        logStackTrace(th);
    }

    public Logger getLogger(String name) {
        if ((name==null && this.name==null) ||
            (name!=null && name.equals(this.name)))
            return this;
        return new JettyLogger(name);
    }

    /*
     * private helpers
     */

    private synchronized void logString(String msg) {
        out.println(msg);
        out.flush();
    }

    private synchronized void logStackTrace(Throwable th) {
        th.printStackTrace(out);
        out.flush();
    }

    private String format(String msg, Object arg0, Object arg1)
    {
        int i0=msg.indexOf("{}");
        int i1=i0<0?-1:msg.indexOf("{}",i0+2);

        if (arg1!=null && i1>=0)
            msg=msg.substring(0,i1)+arg1+msg.substring(i1+2);
        if (arg0!=null && i0>=0)
            msg=msg.substring(0,i0)+arg0+msg.substring(i0+2);
        return msg;
    }

}
