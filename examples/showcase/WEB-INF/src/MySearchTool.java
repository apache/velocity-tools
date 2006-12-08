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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.AbstractSearchTool;
import org.apache.velocity.tools.view.tools.ParameterParser;

/**
 * This is meant to demonstrate how to extend the AbstractSearchTool.
 * A typical implementation would have no static Set as a database,
 * not override init(), not have store() methods, and have a real 
 * executeQuery(criteria) implementation.  But this works for the 
 * purposes of this demo.
 *
 * @author Nathan Bubna
 * @version $Id: MySearchTool.java 479724 2006-11-27 18:49:37Z nbubna $
 */
public class MySearchTool extends AbstractSearchTool
{
    private static Set DATABASE = new HashSet();
    static
    {
        // some random data to get things started
        DATABASE.add("foo");
        DATABASE.add("bar");
        DATABASE.add("baz");
        DATABASE.add("woogie");
        DATABASE.add("pizza");
        DATABASE.add("cheese");
        DATABASE.add("wine");
        DATABASE.add("avocado");
        DATABASE.add("peanut butter");
        DATABASE.add("salami");
        DATABASE.add("hobbits");
        DATABASE.add("basketball");
        DATABASE.add("four score and seven years");
        DATABASE.add("whatever");
        DATABASE.add("you");
        DATABASE.add("want");
    }

    /**
     * Adds the specified item to our static "database".
     * You would not do this in a normal application!
     */
    public void store(Object item)
    {
        DATABASE.add(item);
    }

    /**
     * Adds the entries of the specified map to our static "database".
     * You would not do this in a normal application!
     */
    public void store(Map items)
    {
        DATABASE.addAll(items.entrySet());
    }

    /**
     * Adds the specified items to our static "database".
     * You would not do this in a normal application!
     */
    public void store(Collection items)
    {
        DATABASE.addAll(items);
    }

    /**
     * Override to create a session in which to store search results.
     * This is done to make the demo work well.  In a normal app, it
     * would probably not be the search tool's responsibility to ensure
     * that there is a session.
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        context.getRequest().getSession(true);

        // then pass on to the super class
        super.init(obj);
    }


    public void setup(HttpServletRequest req)
    {
        ParameterParser pp = new ParameterParser(req);
        setCriteria(pp.getString("find"));
        setIndex(pp.getInt("index", 0));
        setItemsPerPage(pp.getInt("show", DEFAULT_ITEMS_PER_PAGE));
    }

    /**
     * This is a very simplistic implementation that looks for the
     * string value of the criteria in the string value of every item
     * in our stupid static database.  If the criteria string is in
     * the item string, it is added to the result list.
     */
    protected List executeQuery(Object crit)
    {
        try
        {
            String findme = String.valueOf(crit);
            List results = new ArrayList();
            synchronized (DATABASE)
            {
                for (Iterator i = DATABASE.iterator(); i.hasNext(); )
                {
                    String item = String.valueOf(i.next());
                    if (item.indexOf(findme) >= 0)
                    {
                        results.add(item);
                    }
                }
            }
            Collections.sort(results);
            return results;
        }
        catch (Exception e)
        {
            // never return null!
            System.out.println(e);
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

}
