package org.apache.velocity.tools.view;

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

import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;

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
 * the executeQuery(Object) method.
 * </p>
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
 *   #if( $search.hasItems() )
 *   Showing $!search.pageDescription&lt;br&gt;
 *     #set( $i = $search.index )
 *     #foreach( $item in $search.page )
 *       ${i}. $!item &lt;br&gt;
 *       #set( $i = $i + 1 )
 *     #end
 *     &lt;br&gt;
 *     #if ( $search.pagesAvailable &gt; 1 )
 *       #set( $pagelink = $link.setRelative('search.vm').addQueryData("find",$!search.criteria).addQueryData("show",$!search.itemsPerPage) )
 *       #if( $search.prevIndex )
 *           &lt;a href="$pagelink.addQueryData('index',$!search.prevIndex)"&gt;Prev&lt;/a&gt;
 *       #end
 *       #foreach( $index in $search.slip )
 *         #if( $index == $search.index )
 *           &lt;b&gt;$search.pageNumber&lt;/b&gt;
 *         #else
 *           &lt;a href="$pagelink.addQueryData('index',$!index)"&gt;$!search.getPageNumber($index)&lt;/a&gt;
 *         #end
 *       #end
 *       #if( $search.nextIndex )
 *           &lt;a href="$pagelink.addQueryData('index',$!search.nextIndex)"&gt;Next&lt;/a&gt;
 *       #end
 *     #end
 *   #elseif( $search.criteria )
 *   Sorry, no matches were found for "$!search.criteria".
 *   #else
 *   Please enter a search term
 *   #end
 * </pre>
 *
 * <p>The output of this might look like:</p>
 * <pre>
 *   &lt;form method="get" action=""&gt;
 *    &lt;input type="text" value="foo"&gt;
 *    &lt;input type="submit" value="Find"&gt;
 *   &lt;/form&gt;
 *   Showing 1-5 of 8&lt;br&gt;
 *   1. foo&lt;br&gt;
 *   2. bar&lt;br&gt;
 *   3. blah&lt;br&gt;
 *   4. woogie&lt;br&gt;
 *   5. baz&lt;br&gt;&lt;br&gt;
 *   &lt;b&gt;1&lt;/b&gt; &lt;a href=""&gt;2&lt;/a&gt; &lt;a href=""&gt;Next&lt;/a&gt;
 * </pre>
 * <p>Example tools.xml configuration:</p>
 * <pre>
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="com.foo.tools.MySearchTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Revision$ $Date$
 */
@DefaultKey("search")
@InvalidScope({Scope.APPLICATION,Scope.SESSION})
public abstract class AbstractSearchTool extends PagerTool
{
    public static final String DEFAULT_CRITERIA_KEY = "find";

    /** the key under which StoredResults are kept in session */
    protected static final String STORED_RESULTS_KEY =
        StoredResults.class.getName();

    private String criteriaKey = DEFAULT_CRITERIA_KEY;
    private Object criteria;

    /**
     * Sets the criteria *if* it is set in the request parameters.
     */
    public void setup(HttpServletRequest request)
    {
        super.setup(request);

        // only change these settings if they're present in the params
        String findMe = request.getParameter(getCriteriaKey());
        if (findMe != null)
        {
            setCriteria(findMe);
        }
    }

    /*  ---------------------- mutators -----------------------------  */

    public void setCriteriaKey(String key)
    {
        this.criteriaKey = key;
    }

    public String getCriteriaKey()
    {
        return this.criteriaKey;
    }


    /**
     * Sets the criteria and results to null, page index to zero, and
     * items per page to the default.
     */
    public void reset()
    {
        super.reset();
        setCriteria(null);
    }


    /**
     * Sets the criteria for this search.
     *
     * @param criteria - the criteria used for this search
     */
    public void setCriteria(Object criteria)
    {
        this.criteria = criteria;
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
     * Gets the results for the given criteria either in memory
     * or by performing a new query for them.  If the criteria
     * is null, an empty list will be returned.
     *
     * @return {@link List} of all items for the criteria
     */
    public List getItems()
    {
        Object findMe = getCriteria();
        /* return empty list if we have no criteria */
        if (findMe == null)
        {
            return Collections.EMPTY_LIST;
        }

        /* get the current list (should never return null!) */
        List list = super.getItems();
        assert (list != null);

        /* if empty, execute a query for the criteria */
        if (list.isEmpty())
        {
            /* safely perform a new query */
            try
            {
                list = executeQuery(findMe);
            }
            catch (Throwable t)
            {
                getLog().error("executeQuery({}) failed", findMe, t);
            }

            /* because we can't trust executeQuery() not to return null
               and getItems() must _never_ return null... */
            if (list == null)
            {
                list = Collections.EMPTY_LIST;
            }

            /* save the new results */
            setItems(list);
        }
        return list;
    }


    /*  ---------------------- protected methods -----------------------------  */

    protected List getStoredItems()
    {
        StoredResults sr = getStoredResults();

        /* if the criteria equals that of the stored results,
         * then return the stored result list */
        if (sr != null && getCriteria().equals(sr.getCriteria()))
        {
            return sr.getList();
        }
        return null;
    }


    protected void setStoredItems(List items)
    {
        setStoredResults(new StoredResults(getCriteria(), items));
    }


    /**
     * Executes a query for the specified criteria.
     *
     * <p>This method must be implemented! A simple
     * implementation might be something like:</p>
     * <pre>
     * protected List executeQuery(Object crit)
     * {
     *     return MyDbUtils.getFooBarsMatching((String)crit);
     * }
     * </pre>
     *
     * @param criteria search criteria
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
    public static class StoredResults implements java.io.Serializable
    {
        /** serial version id */
        private static final long serialVersionUID = 4503130168585978169L;

        private final transient Object crit;
        private final transient List list;

        /**
         * Creates a new instance.
         *
         * @param crit the criteria for these results
         * @param list the {@link List} of results to store
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
