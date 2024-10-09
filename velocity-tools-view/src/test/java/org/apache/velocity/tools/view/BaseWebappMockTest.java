package org.apache.velocity.tools.view;

import org.easymock.EasyMockRule;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.easymock.EasyMock.replay;

public class BaseWebappMockTest
{
    protected static Logger logger = LoggerFactory.getLogger("webapp-mock");
    /**
     * Unique point of passage for non-void calls
     * @param value value to return
     * @param <T> type of returned value
     * @return value
     */
    protected static <T> IAnswer<T> eval(final T value)
    {
        return new IAnswer<T>()
        {
            public T answer() throws Throwable
            {
                String caller = null;
                String mocked = null;
                String test = null;
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement line : stackTrace)
                {
                    String at = line.toString();
                    if (mocked == null)
                    {
                        if (at.startsWith("com.sun.proxy"))
                        {
                            int dot = at.lastIndexOf('.');
                            dot = at.lastIndexOf('.', dot - 1);
                            int par = at.indexOf('(', dot + 1);
                            mocked = at.substring(dot + 1, par) + "()";
                        }
                    }
                    if (at.startsWith("org.apache"))
                    {
                        if (at.contains("Test"))
                        {
                            if (test == null && !at.contains(".answer(") && !(at.contains("$")))
                            {
                                test = at.replaceAll("\\b[a-z]+\\.|\\(.*\\)", "");
                            }
                        }
                        else if (caller == null)
                        {
                            caller = at;
                        }
                    }
                }
                // good place for a breakpoint
                logger.trace("XXX [{}] mocked {} called from {}, returning {}", test, mocked, caller, value);
                return value;
            }
        };
    }

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    protected FilterConfig filterConfig;

    @Mock
    protected ServletContext servletContext;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected HttpServletResponse response;

    @Mock
    protected FilterChain filterChain;

    @Mock
    protected HttpSession httpSession;

    protected void replayAll()
    {
        replay(filterConfig, servletContext, request, response, filterChain, httpSession);
    }


}
