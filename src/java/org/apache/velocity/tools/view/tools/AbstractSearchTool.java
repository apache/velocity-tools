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


package org.apache.velocity.tools.view.tools;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * <p>Abstract view tool for doing "searching" and robust
 * pagination of search results.  The goal here is to provide a simple
 * and uniform API for "search tools" that can be used in velocity
 * templates (or even a standard Search.vm template).  In particular,
 * this class provides good support for result pagination and some
 * very simple result caching.
 * </p>
 * <p><b>Usage:</b><br>
 * To use this class, you must extend it and implement
 * the setup(HttpServletRequest) and executeQuery(Object)
 * methods.
 * <p>
 * The setup(HttpServletRequest) method ought to extract
 * from the current request the search criteria, the current
 * list index, and optionally, the number of items to display
 * per page of results.  Upon extracting these parameters, they
 * should be set using the provided setCriteria(Object),
 * setIndex(int), and setItemsPerPage(int) methods. A simple 
 * implementation would be:
 * <pre>
 * public void setup(HttpServletRequest req)
 * {
 *     ParameterParser pp = new ParameterParser(req);
 *     setCriteria(pp.getString("find"));
 *     setIndex(pp.getInt("index", 0));
 *     setItemsPerPage(pp.getInt("show", DEFAULT_ITEMS_PER_PAGE));
 * }
 * </pre>
 * <p>
 * The setCriteria(Object) method takes an Object in order to
 * allow the search criteria to meet your needs.  Your criteria
 * may be as simple as a single string, an array of strings, or
 * whatever you like.  The value passed into this method is that
 * which will ultimately be passed into executeQuery(Object) to
 * perform the search and return a list of results.  A simple
 * implementation might be like:
 * <pre>
 * protected List executeQuery(Object crit)
 * {
 *     return MyDbUtils.getFooBarsMatching((String)crit);
 * }
 * </pre>
 * <p>
 * Here's an example of how your subclass would be used in a template:
 * <pre>
 *   &lt;form name="search" method="get" action="$link.setRelative('search.vm')"&gt;
 *     &lt;input type="text"name="find" value="$!search.criteria"&gt;
 *     &lt;input type="submit" value="Find"&gt;
 *   &lt;/form&gt;
 *   #if( $search.hasResults() )
 *   Showing $!search.pageDescription&lt;br&gt;
 *     #set( $i = $search.index )
 *     #foreach( $result in $search.page )
 *       ${i}. $!result &lt;br&gt;
 *       #set( $i = $i + 1 )
 *     #end
 *     &lt;br&gt;
 *     #if ( $search.pagesAvailable &gt; 1 )
 *       #set( $searchlink = $link.setRelative('search.vm').addQueryData("find",$!search.criteria).addQueryData("show",$!search.itemsPerPage) )
 *       #if( $search.prevIndex )
 *           &lt;a href="$searchlink.addQueryData('index',$!search.prevIndex)"&gt;Prev&lt;/a&gt;
 *       #end
 *       #foreach( $index in $search.slip )
 *         #if( $index == $search.index )
 *           &lt;b&gt;$search.pageNumber&lt;/b&gt;
 *         #else
 *           &lt;a href="$searchlink.addQueryData('index',$!index)"&gt;$!search.getPageNumber($index)&lt;/a&gt;
 *         #end
 *       #end
 *       #if( $search.nextIndex )
 *           &lt;a href="$searchlink.addQueryData('index',$!search.nextIndex)"&gt;Next&lt;/a&gt;
 *       #end
 *     #end
 *   #elseif( $search.criteria )
 *   Sorry, no matches were found for "$!search.criteria".
 *   #else
 *   Please enter a search term
 *   #end
 * </pre>
 *
 * The output of this might look like:<br><br>
 *   <form method="get" action="">
 *    <input type="text" value="foo">
 *    <input type="submit" value="Find">
 *   </form>
 *   Showing 1-5 of 8<br>
 *   1. foo<br>
 *   2. bar<br>
 *   3. blah<br>
 *   4. woogie<br>
 *   5. baz<br><br>
 *   <b>1</b> <a href="">2</a> <a href="">Next</a>
 * </p>
 * <p>
 * <b>Example toolbox.xml configuration:</b>
 * <pre>&lt;tool&gt;
 *   &lt;key&gt;search&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;com.foo.tools.MyAbstractSearchTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre>
 * </p>
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @since VelocityTools 1.0
 * @version $Revision: 1.5 $ $Date: 2003/11/06 00:26:54 $
 */
public abstract class AbstractSearchTool implements ViewTool
{


    /** the default number of results shown per page */
    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    /** the default max number of result page indices to list */
    public static final int DEFAULT_SLIP_SIZE = 20;

    /** the key under which StoredResults are kept in session */
    protected static final String STORED_RESULTS_KEY = 
        StoredResults.class.getName();

    private List results;
    private Object criteria;
    private int index = 0;
    private int slipSize = DEFAULT_SLIP_SIZE;
    private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;

    protected HttpSession session;


    /**
     * Initializes this instance by grabbing the request
     * and session objects from the current ViewContext.
     *
     * @param obj the current ViewContext
     * @throws ClassCastException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        HttpServletRequest request = context.getRequest();
        session = request.getSession(false);
        setup(request);
    }


    /**
     * Abstract method to make it as obvious as possible just
     * where implementing classes should be retrieving and configuring
     * search/display parameters. 
     * <p>A simple implementation would be:
     * <pre>
     * public void setup(HttpServletRequest req)
     * {
     *     ParameterParser pp = new ParameterParser(req);
     *     setCriteria(pp.getString("find"));
     *     setIndex(pp.getInt("index", 0));
     *     setItemsPerPage(pp.getInt("show", DEFAULT_ITEMS_PER_PAGE));
     * }
     * </pre>
     *
     * @param request the current HttpServletRequest
     */
    public abstract void setup(HttpServletRequest request);


    /*  ---------------------- mutators -----------------------------  */


    /**
     * Sets the criteria and results to null, page index to zero, and
     * items per page to the default.
     */
    public void reset()
    {
        results = null;
        criteria = null;
        index = 0;
        itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
    }


    /**
     * Sets the criteria for this search and clears existing results.
     *
     * @param criteria - the criteria used for this search
     */
    public void setCriteria(Object criteria)
    {
        this.criteria = criteria;
        this.results = null;
    }


    /**
     * Sets the index of the first result in the current page
     *
     * @param index - the result index to start the current page with
     */
    public void setIndex(int index)
    {
        if (index < 0)
        {
            /* quietly override to a reasonable value */
            index = 0;
        }
        this.index = index;
    }


    /**
     * Sets the number of items returned in a page of results
     *
     * @param itemsPerPage - the number of items to be returned per page
     */
    public void setItemsPerPage(int itemsPerPage)
    {
        if (itemsPerPage < 1)
        {
            /* quietly override to a reasonable value */
            itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        }
        this.itemsPerPage = itemsPerPage;
    }


    /**
     * Sets the number of result page indices for {@link #getSlip} to list.
     * (for google-ish result page links).
     *
     * @see #getSlip
     * @param slipSize - the number of result page indices to list
     */
    public void setSlipSize(int slipSize)
    {
        if (slipSize < 2)
        {
            /* quietly override to a reasonable value */
            slipSize = DEFAULT_SLIP_SIZE;
        }
        this.slipSize = slipSize;
    }


    /*  ---------------------- accessors -----------------------------  */


    /**
     * Return the criteria object for this request.
     * (for a simple search mechanism, this will typically be
     *  just a java.lang.String)
     *
     * @return criteria object
     */
    public Object getCriteria()
    {
        return criteria;
    }


    /**
     * Return the set number of items to be displayed per page of results
     *
     * @return current number of results shown per page
     */
    public int getItemsPerPage()
    {
        return itemsPerPage;
    }


    /**
     * Returns the number of result page indices {@link #getSlip} 
     * will return per request (if available).
     *
     * @return the number of result page indices {@link #getSlip} 
     *         will try to return
     */
    public int getSlipSize()
    {
        return slipSize;
    }


    /**
     * Return the current search result index.
     *
     * @return the index for the beginning of the current page
     */
    public int getIndex()
    {
        return index;
    }


    /**
     * Checks whether or not the result list is empty.
     *
     * @return <code>true</code> if the result list is not empty.
     */
    public boolean hasResults()
    {
        return !getResults().isEmpty();
    }


    /**
     * Return the results for the given criteria.  This is guaranteed
     * to never return <code>null</code>.
     *
     * @return {@link List} of all results for the criteria
     */
    public List getResults()
    {
        if (results == null)
        {
            results = retrieveResults();
        }
        return results;
    }


    /**
     * Return the index for the next page of results
     * (as determined by the current index, items per page, and 
     * the number of results).  If no "next page" exists, then null is
     * returned.
     *
     * @return index for the next page or <code>null</code> if none exists
     */
    public Integer getNextIndex()
    {
        int next = index + itemsPerPage;
        if (next < getResults().size())
        {
            return new Integer(next);
        }
        return null;
    }


    /**
     * Return the index for the previous page of results
     * (as determined by the current index, items per page, and 
     * the number of results).  If no "next page" exists, then null is
     * returned.
     *
     * @return index for the previous page or <code>null</code> if none exists
     */
    public Integer getPrevIndex()
    {
        int prev = Math.min(index, getResults().size()) - itemsPerPage;
        if (index > 0)
        {
            return new Integer(Math.max(0, prev));
        }
        return null;
    }


    /**
     * Return the number of pages that can be made from this list
     * given the set number of items per page.
     */
    public int getPagesAvailable()
    {
        return (int)Math.ceil(getResults().size() / (double)itemsPerPage);
    }


    /**
     * Return the current "page" of search results.
     *
     * @return a {@link List} of results for the "current page"
     */
    public List getPage()
    {
        /* return null if we have no results */
        if (!hasResults())
        {
            return null;
        }
        /* quietly keep the page indices to legal values for robustness' sake */
        int start = Math.min(getResults().size() - 1, index);
        int end = Math.min(getResults().size(), index + itemsPerPage);
        return getResults().subList(start, end);
    }


    /**
     * Returns the "page number" for the specified index.  Because the page
     * number is used for the user interface, the page numbers are 1-based.
     *
     * @param i the index that you want the page number for
     * @return the approximate "page number" for the specified index or 
     *         <code>null</code> if there are no results
     */
    public Integer getPageNumber(int i)
    {
        if (!hasResults())
        {
            return null;
        }
        return new Integer(1 + i / itemsPerPage);
    }


    /**
     * Returns the "page number" for the current index.  Because the page
     * number is used for the user interface, the page numbers are 1-based.
     *
     * @return the approximate "page number" for the current index or 
     *         <code>null</code> if there are no results
     */
    public Integer getPageNumber()
    {
        return getPageNumber(index);
    }


    /**
     * <p>Returns a description of the current page.  This implementation
     * displays a 1-based range of result indices and the total number 
     * of results.  (e.g. "1 - 10 of 42" or "7 of 7")</p>
     *
     * <p>Sub-classes may override this to provide a customized 
     * description (such as one in another language).</p>
     *
     * @return a description of the current page
     */
    public String getPageDescription()
    {
        StringBuffer out = new StringBuffer();
        int first = index + 1;
        int total = getResults().size();
        if (first >= total)
        {
            out.append(total);
            out.append(" of ");
            out.append(total);
        }
        else
        {
            int last = Math.min(index + itemsPerPage, total);
            out.append(first);
            out.append(" - ");
            out.append(last);
            out.append(" of ");
            out.append(total);
        }
        return out.toString();
    }


    /**
     * Return a <b>S</b>liding <b>L</b>ist of <b>I</b>ndices for <b>P</b>ages
     * of search results.
     *
     * <p>Essentially, this returns a list of result indices that correspond
     * to available pages of search results (as based on the set 
     * items-per-page). This makes it relativly easy to do a google-ish set 
     * of links to result pages.</p>
     *
     * <p>Note that this list of Integers is 0-based to correspond with the
     * underlying result indices and not the displayed page numbers (see
     * {@link #getPageNumber}).</p>
     *
     * @return {@link List} of Integers representing the indices of result 
     *         pages or empty list if there's one or less pages available
     */
    public List getSlip()
    {
        /* return an empty list if there's no pages to list */
        int totalPgs = getPagesAvailable();
        if (totalPgs <= 1)
        {
            return Collections.EMPTY_LIST;
        }

        /* page number is 1-based so decrement it */
        int curPg = getPageNumber().intValue() - 1;

        /* don't include current page in slip size calcs */
        int adjSlipSize = slipSize - 1;

        /* start at zero or just under half of max slip size 
         * this keeps "forward" and "back" pages about even
         * but gives preference to "forward" pages */
        int slipStart = Math.max(0, (curPg - (adjSlipSize / 2)));

        /* push slip end as far as possible */
        int slipEnd = Math.min(totalPgs, (slipStart + adjSlipSize));

        /* if we're out of "forward" pages, then push the 
         * slip start toward zero to maintain slip size */
        if (slipEnd - slipStart < adjSlipSize)
        {
            slipStart = Math.max(0, slipEnd - adjSlipSize);
        }

        /* convert 0-based page numbers to indices and create list */
        List slip = new ArrayList((slipEnd - slipStart) + 1);
        for (int i=slipStart; i < slipEnd; i++)
        {
            slip.add(new Integer(i * itemsPerPage));
        }
        return slip;
    }


    /*  ---------------------- private methods -----------------------------  */


    /**
     * Gets the results for the given criteria either in memory
     * or by performing a new query for them.  This is guaranteed to
     * NOT return null.
     */
    private List retrieveResults()
    {
        /* return empty list if we have no criteria */
        if (criteria == null)
        {
            return Collections.EMPTY_LIST;
        }

        /* get any stored results */
        StoredResults sr = getStoredResults();

        /* if the criteria equals that of the stored results, 
         * then return the stored result list */
        if (sr != null && criteria.equals(sr.getCriteria()))
        {
            return sr.getList();
        }

        /* perform a new query and make sure we don't end up with a null list */
        List list = executeQuery(criteria);
        if (list == null)
        {
            list = Collections.EMPTY_LIST;
        }

        /* save the new results */
        setStoredResults(new StoredResults(criteria, list));
        return list;
    }


    /*  ---------------------- protected methods -----------------------------  */


    /**
     * Executes a query for the specified criteria.
     * 
     * <p>This method must be implemented! A simple
     * implementation might be something like:
     * <pre>
     * protected List executeQuery(Object crit)
     * {
     *     return MyDbUtils.getFooBarsMatching((String)crit);
     * }
     * </pre>
     * 
     * @return a {@link List} of results for this query
     */
    protected abstract List executeQuery(Object criteria);


    /**
     * Retrieves stored search results (if any) from the user's
     * session attributes.
     *
     * @return the {@link StoredResults} retrieved from memory
     */
    protected StoredResults getStoredResults()
    {
        if (session != null)
        {
            return (StoredResults)session.getAttribute(STORED_RESULTS_KEY);
        }
        return null;
    }


    /**
     * Stores current search results in the user's session attributes
     * (if one currently exists) in order to do efficient result pagination.
     *
     * <p>Override this to store search results somewhere besides the
     * HttpSession or to prevent storage of results across requests. In
     * the former situation, you must also override getStoredResults().</p>
     *
     * @param results the {@link StoredResults} to be stored
     */
    protected void setStoredResults(StoredResults results)
    {
        if (session != null)
        {
            session.setAttribute(STORED_RESULTS_KEY, results);
        }
    }


    /*  ---------------------- utility class -----------------------------  */


    /**
     * Simple utility class to hold a criterion and its result list.
     * <p>
     * This class is by default stored in a user's session,
     * so it implements Serializable, but its members are
     * transient. So functionally, it is not serialized and
     * the last results/criteria will not be persisted if
     * the session is serialized.
     * </p>
     */
    public class StoredResults implements java.io.Serializable
    {

        private transient Object crit;
        private transient List list;

        /**
         * Creates a new instance.
         *
         * @param criteria - the criteria for these results
         * @param list - the {@link List} of results to store
         */
        public StoredResults(Object crit, List list)
        {
            this.crit = crit;
            this.list = list;
        }

        /**
         * @return the stored criteria object
         */
        public Object getCriteria()
        {
            return crit;
        }

        /**
         * @return the stored {@link List} of results
         */
        public List getList()
        {
            return list;
        }

    }


}
