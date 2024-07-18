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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;

/**
 * <p>View tool for doing request-based pagination of
 * items in an a list.
 * </p>
 * <p><b>Usage:</b><br>
 * To use this class, you typically push a List of items to it
 * by putting it in the request attributes under the value returned by
 * {@link #getNewItemsKey()} (default is "new.items").
 * You can also set the list of items to be paged in a subclass
 * using the setItems(List) method, or you can always set the
 * item list at another point (even from within the template). This
 * need only happen once per session if a session is available, but the
 * item list can be (re)set as often as you like.
 * </p>
 * <p>
 * Here's an example of how your subclass would be used in a template:
 * <pre>
 *   #if( $pager.hasItems() )
 *   Showing $!pager.pageDescription&lt;br&gt;
 *     #set( $i = $pager.index )
 *     #foreach( $item in $pager.page )
 *       ${i}. $!item &lt;br&gt;
 *       #set( $i = $i + 1 )
 *     #end
 *     &lt;br&gt;
 *     #if ( $pager.pagesAvailable &gt; 1 )
 *       #set( $pagelink = $link.self.param("show",$!pager.itemsPerPage) )
 *       #if( $pager.prevIndex )
 *           &lt;a href="$pagelink.param('index',$!pager.prevIndex)"&gt;Prev&lt;/a&gt;
 *       #end
 *       #foreach( $index in $pager.slip )
 *         #if( $index == $pager.index )
 *           &lt;b&gt;$pager.pageNumber&lt;/b&gt;
 *         #else
 *           &lt;a href="$pagelink.param('index',$!index)"&gt;$!pager.getPageNumber($index)&lt;/a&gt;
 *         #end
 *       #end
 *       #if( $pager.nextIndex )
 *           &lt;a href="$pagelink.param('index',$!pager.nextIndex)"&gt;Next&lt;/a&gt;
 *       #end
 *     #end
 *   #else
 *   No items in list.
 *   #end
 * </pre>
 *
 * <p>The output of this might look like:</p>
 * <pre>
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
 *     &lt;tool class="org.apache.velocity.tools.view.PagerTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 * @since VelocityTools 2.0
 */
@DefaultKey("pager")
@InvalidScope({Scope.APPLICATION,Scope.SESSION})
public class PagerTool extends SafeConfig
{
    public static final String DEFAULT_NEW_ITEMS_KEY = "new.items";
    public static final String DEFAULT_INDEX_KEY = "index";
    public static final String DEFAULT_ITEMS_PER_PAGE_KEY = "show";
    public static final String DEFAULT_SLIP_SIZE_KEY = "slipSize";

    /** the default number of items shown per page */
    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    /** the default max number of page indices to list */
    public static final int DEFAULT_SLIP_SIZE = 20;

    /** the key under which items are stored in session */
    protected static final String STORED_ITEMS_KEY = PagerTool.class.getName();

    private String newItemsKey = DEFAULT_NEW_ITEMS_KEY;
    private String indexKey = DEFAULT_INDEX_KEY;
    private String itemsPerPageKey = DEFAULT_ITEMS_PER_PAGE_KEY;
    private String slipSizeKey = DEFAULT_SLIP_SIZE_KEY;
    private boolean createSession = false;

    private List items;
    private int index = 0;
    private int slipSize = DEFAULT_SLIP_SIZE;
    private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
    protected HttpSession session;

    /**
     * Initializes this tool with the specified {@link HttpServletRequest}.
     * This is required for this tool to operate and will throw a
     * NullPointerException if this is not set or is set to {@code null}.
     * @param request servlet request
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request == null)
        {
            throw new NullPointerException("request should not be null");
        }
        this.session = request.getSession(getCreateSession());
        setup(request);
    }

    /**
     * Sets the index, itemsPerPage, and/or slipSize *if* they are set
     * in the request parameters.  Likewise, this will set the item list
     * to be paged *if* there is a list pushed into the request attributes
     * under the {@link #getNewItemsKey()}.
     *
     * @param request the current HttpServletRequest
     */
    public void setup(HttpServletRequest request)
    {
        ParameterTool params = new ParameterTool(request);

        // only change these settings if they're present in the params
        int index = params.getInt(getIndexKey(), -1);
        if (index >= 0)
        {
            setIndex(index);
        }
        int show = params.getInt(getItemsPerPageKey(), 0);
        if (show > 0)
        {
            setItemsPerPage(show);
        }
        int slipSize = params.getInt(getSlipSizeKey(), 0);
        if (slipSize > 0)
        {
            setSlipSize(slipSize);
        }

        // look for items in the request attributes
        List newItems = (List)request.getAttribute(getNewItemsKey());
        if (newItems != null)
        {
            // only set the items if a list was pushed into the request
            setItems(newItems);
        }
    }


    public void setNewItemsKey(String key)
    {
        this.newItemsKey = key;
    }

    public String getNewItemsKey()
    {
        return this.newItemsKey;
    }

    public void setIndexKey(String key)
    {
        this.indexKey = key;
    }

    public String getIndexKey()
    {
        return this.indexKey;
    }

    public void setItemsPerPageKey(String key)
    {
        this.itemsPerPageKey = key;
    }

    public String getItemsPerPageKey()
    {
        return this.itemsPerPageKey;
    }

    public void setSlipSizeKey(String key)
    {
        this.slipSizeKey = key;
    }

    public String getSlipSizeKey()
    {
        return this.slipSizeKey;
    }

    public void setCreateSession(boolean createSession)
    {
        this.createSession = createSession;
    }

    public boolean getCreateSession()
    {
        return this.createSession;
    }

    /**
     * Sets the item list to null, page index to zero, and
     * items per page to the default.
     */
    public void reset()
    {
        items = null;
        index = 0;
        itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
    }

    /**
     * Sets the List to page through.
     *
     * @param items - the  {@link List} of items to be paged through
     */
    public void setItems(List items)
    {
        this.items = items;
        setStoredItems(items);
    }

    /**
     * Sets the index of the first result in the current page
     *
     * @param index the result index to start the current page with
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
     * Sets the number of items returned in a page of items
     *
     * @param itemsPerPage the number of items to be returned per page
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

    /*  ---------------------- accessors ----------------------------- */

    /**
     * Returns the set number of items to be displayed per page of items
     *
     * @return current number of items shown per page
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
     * Returns the current search result index.
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
    public boolean hasItems()
    {
        return !getItems().isEmpty();
    }

    /**
     * Returns the item list. This is guaranteed
     * to never return <code>null</code>.
     *
     * @return {@link List} of all the items
     */
    public List getItems()
    {
        if (items == null)
        {
            items = getStoredItems();
        }

        return (items != null) ? items : Collections.EMPTY_LIST;
    }

    /**
     * Returns the index of the last item on the current page of results
     * (as determined by the current index, items per page, and
     * the number of items).  If there is no current page, then null is
     * returned.
     *
     * @return index for the last item on this page or <code>null</code>
     *         if none exists
     * @since VelocityTools 1.3
     */
    public Integer getLastIndex()
    {
        if (!hasItems())
        {
            return null;
        }
        return Integer.valueOf(Math.min(getTotal() - 1, index + itemsPerPage - 1));
    }

    /**
     * Returns the index for the next page of items
     * (as determined by the current index, items per page, and
     * the number of items).  If no "next page" exists, then null is
     * returned.
     *
     * @return index for the next page or <code>null</code> if none exists
     */
    public Integer getNextIndex()
    {
        int next = index + itemsPerPage;
        if (next < getTotal())
        {
            return Integer.valueOf(next);
        }
        return null;
    }

    /**
     * Returns the index of the first item on the current page of results
     * (as determined by the current index, items per page, and
     * the number of items).  If there is no current page, then null is
     * returned. This is different than {@link #getIndex()} in that it
     * is adjusted to fit the reality of the items available and is not a
     * mere accessor for the current, user-set index value.
     *
     * @return index for the first item on this page or <code>null</code>
     *         if none exists
     * @since VelocityTools 1.3
     */
    public Integer getFirstIndex()
    {
        if (!hasItems())
        {
            return null;
        }
        return Integer.valueOf(Math.min(getTotal() - 1, index));
    }

    /**
     * Return the index for the previous page of items
     * (as determined by the current index, items per page, and
     * the number of items).  If no "next page" exists, then null is
     * returned.
     *
     * @return index for the previous page or <code>null</code> if none exists
     */
    public Integer getPrevIndex()
    {
        int prev = Math.min(index, getTotal()) - itemsPerPage;
        if (index > 0)
        {
            return Integer.valueOf(Math.max(0, prev));
        }
        return null;
    }

    /**
     * Returns the number of pages that can be made from this list
     * given the set number of items per page.
     * @return number of available pages
     */
    public int getPagesAvailable()
    {
        return (int)Math.ceil(getTotal() / (double)itemsPerPage);
    }


    /**
     * Returns the current "page" of search items.
     *
     * @return a {@link List} of items for the "current page"
     */
    public List getPage()
    {
        /* return null if we have no items */
        if (!hasItems())
        {
            return null;
        }
        /* quietly keep the page indices to legal values for robustness' sake */
        int start = getFirstIndex().intValue();
        int end = getLastIndex().intValue() + 1;
        return getItems().subList(start, end);
    }

    /**
     * Returns the "page number" for the specified index.  Because the page
     * number is used for the user interface, the page numbers are 1-based.
     *
     * @param i the index that you want the page number for
     * @return the approximate "page number" for the specified index or
     *         <code>null</code> if there are no items
     */
    public Integer getPageNumber(int i)
    {
        if (!hasItems())
        {
            return null;
        }
        return Integer.valueOf(1 + i / itemsPerPage);
    }


    /**
     * Returns the "page number" for the current index.  Because the page
     * number is used for the user interface, the page numbers are 1-based.
     *
     * @return the approximate "page number" for the current index or
     *         <code>null</code> if there are no items
     */
    public Integer getPageNumber()
    {
        return getPageNumber(index);
    }

    /**
     * Returns the total number of items available.
     * @return number of items
     * @since VelocityTools 1.3
     */
    public int getTotal()
    {
        if (!hasItems())
        {
            return 0;
        }
        return getItems().size();
    }

    /**
     * <p>Returns a description of the current page.  This implementation
     * displays a 1-based range of result indices and the total number
     * of items.  (e.g. "1 - 10 of 42" or "7 of 7")  If there are no items,
     * this will return "0 of 0".</p>
     *
     * <p>Sub-classes may override this to provide a customized
     * description (such as one in another language).</p>
     *
     * @return a description of the current page
     */
    public String getPageDescription()
    {
        if (!hasItems())
        {
            return "0 of 0";
        }

        StringBuilder out = new StringBuilder();
        int first = getFirstIndex().intValue() + 1;
        int total = getTotal();
        if (first >= total)
        {
            out.append(total);
            out.append(" of ");
            out.append(total);
        }
        else
        {
            int last = getLastIndex().intValue() + 1;
            out.append(first);
            out.append(" - ");
            out.append(last);
            out.append(" of ");
            out.append(total);
        }
        return out.toString();
    }

    /**
     * Returns a <b>S</b>liding <b>L</b>ist of <b>I</b>ndices for <b>P</b>ages
     * of items.
     *
     * <p>Essentially, this returns a list of item indices that correspond
     * to available pages of items (as based on the set items-per-page).
     * This makes it relativly easy to do a google-ish set of links to
     * available pages.</p>
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

        /* start at zero or just under half of max slip size
         * this keeps "forward" and "back" pages about even
         * but gives preference to "forward" pages */
        int slipStart = Math.max(0, (curPg - (slipSize / 2)));

        /* push slip end as far as possible */
        int slipEnd = Math.min(totalPgs, (slipStart + slipSize));

        /* if we're out of "forward" pages, then push the
         * slip start toward zero to maintain slip size */
        if (slipEnd - slipStart < slipSize)
        {
            slipStart = Math.max(0, slipEnd - slipSize);
        }

        /* convert 0-based page numbers to indices and create list */
        List slip = new ArrayList(slipEnd - slipStart);
        for (int i=slipStart; i < slipEnd; i++)
        {
            slip.add(Integer.valueOf(i * itemsPerPage));
        }
        return slip;
    }

    /*  ---------------------- protected methods ------------------------  */

    /**
     * Retrieves stored search items (if any) from the user's
     * session attributes.
     *
     * @return the {@link List} retrieved from memory
     */
    protected List getStoredItems()
    {
        if (session != null)
        {
            return (List)session.getAttribute(STORED_ITEMS_KEY);
        }
        return null;
    }


    /**
     * Stores current search items in the user's session attributes
     * (if one currently exists) in order to do efficient result pagination.
     *
     * <p>Override this to store search items somewhere besides the
     * HttpSession or to prevent storage of items across requests. In
     * the former situation, you must also override getStoredItems().</p>
     *
     * @param items the {@link List} to be stored
     */
    protected void setStoredItems(List items)
    {
        if (session != null)
        {
            session.setAttribute(STORED_ITEMS_KEY, items);
        }
    }

}
