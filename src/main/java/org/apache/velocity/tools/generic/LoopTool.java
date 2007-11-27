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

import java.util.Iterator;
import java.util.Stack;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <p>
 * A convenience tool to use with #foreach loops. It wraps a list
 * with a custom iterator to let the designer specify a condition
 * to stop looping over the items early.
 * </p>
 * <p>
 * This tool is heavily inspired the now-deprecated IteratorTool,
 * which provided similar functionality but was somewhat more difficult
 * to understand and use.  Rather than try to migrate that implementation
 * via deprecation and new methods, it was simplest to just create an
 * entirely new tool with a simplified API, support for nested loops
 * (which can optionally be given names), and skipping ahead in loops.
 * </p>
 * <p>
 * Most users, of course, will probably never need anything beyond the 
 * simple {@link #watch(Object)}, {@link #stop}, and {@link #skip(int)}
 * methods, if even that much.  However, it is with complicated nested
 * #foreach loops and varying "break" conditions that this tool can
 * probably do the most to simplify your templates.
 * </p>
 * <p>
 * Example of use:
 * <pre>
 *  Template
 *  ---
 *  #set ($list = [1, 2, 3, 4, 5, 6])
 *  #foreach( $item in $loop.watch($list) )
 *  $item
 *  #if( $item > 1 )$loop.skip(1)#end
 *  #if( $item >= 5 )$loop.stop()#end
 *  #end
 *
 *  Output
 *  ------
 *  1 3 5
 *
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.LoopTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: LoopTool.java 590893 2007-11-01 04:40:21Z nbubna $
 */
@DefaultKey("loop")
@ValidScope(Scope.REQUEST)
public class LoopTool
{
    private Stack<ManagedIterator> iterators = new Stack<ManagedIterator>();

    /**
     * <p>Tells the LoopTool to watch the specified Array, Collection, Map,
     * Iterator, Iterable, Enumeration or POJO with an iterator() method
     * while the template iterates over the values within it.
     * </p>
     * <p>Under the covers, this is returning an iterable wrapper that
     * is also pushed onto this tool's stack.  That allows this tool to
     * know which iterator to give later commands (i.e. stop() or skip()).
     * </p>
     * @param obj an object that Velocity's #foreach directive can iterate over
     * @return a {@link ManagedIterator} that this tool instance will track
     */
    public Iterator watch(Object obj)
    {
        Iterator iterator = getIterator(obj);
        if (iterator == null)
        {
            return null;
        }

        ManagedIterator managed = new ManagedIterator(iterator, this);
        iterators.push(managed);
        return managed;
    }

    /**
     * This is just like {@link #watch(Object)} except that it also takes
     * a name which is given to the {@link ManagedIterator} that is returned.
     * This allows the user to send stop or skip commands to that specific
     * iterator even when there are nested iterators within it that are being
     * watched.  If the given name is {@code null}, then this will return 
     * {@code null} even if the object can be watched. Provided names cannot
     * be {@code null}.
     * @see #watch(Object)
     */
    public Iterator watch(Object obj, String name)
    {
        // don't mess around with null names
        if (name == null)
        {
            return null;
        }
        Iterator iterator = getIterator(obj);
        if (iterator == null)
        {
            return null;
        }

        ManagedIterator managed = new ManagedIterator(name, iterator, this);
        iterators.push(managed);
        return managed;
    }

    /**
     * This tells the current loop to stop after the current iteration.
     * This is different from "break" common to most programming languages,
     * in that it does not immediately cease activity in the current iteration.
     * Instead, it merely tells the #foreach loop that this is the last time
     * around.
     */
    public void stop()
    {
        // if we have an iterator on the stack
        if (!iterators.empty())
        {
            // stop the top one, so #foreach doesn't loop again
            iterators.peek().stop();
        }
    }

    /**
     * This is just like {@link #stop()} except that the stop command is issued
     * <strong>only</strong> to the loop/iterator with the specified name.
     * If no such loop is found with that name, then no stop command is issued.
     * @see #stop()
     */
    public void stop(String name)
    {
        // just stop the matching one
        for (ManagedIterator iterator : iterators)
        {
            if (iterator.getName().equals(name))
            {
                iterator.stop();
                break;
            }
        }
    }

    /**
     * This is just like {@link #stop(String)} except that the stop command is issued
     * both to the loop/iterator with the specified name and all loops nested within
     * it.  If no such loop is found with that name, then no stop commands are
     * issued.
     * @see #stop()
     * @see #stop(String)
     */
    public void stopTo(String name)
    {
        if (!iterators.empty())
        {
            // create a backup stack to put things back as they were
            Stack<ManagedIterator> backup = new Stack<ManagedIterator>();
            // look for the iterator with the specified name
            boolean found = false;
            while (!found && !iterators.empty())
            {
                ManagedIterator iterator = iterators.pop();
                if (iterator.getName().equals(name))
                {
                    found = true;
                    iterator.stop();
                }
                else
                {
                    // keep a backup of the ones that don't match
                    backup.push(iterator);
                }
            }

            while (!backup.empty())
            {
                // push the nested iterators back
                ManagedIterator iterator = backup.pop();
                iterators.push(iterator);
                if (found)
                {
                    iterator.stop();
                }
            }
        }
    }

    /**
     * This is just like {@link #stop()} except that the stop command is issued
     * <strong>all</strong> the loops being watched by this tool.
     * @see #stop()
     */
    public void stopAll()
    {
        // just stop them all
        for (ManagedIterator iterator : iterators)
        {
            iterator.stop();
        }
    }

    /**
     * This tells the current loop to skip ahead the specified number of
     * iterations before doing the next iteration.
     */
    public void skip(int number)
    {
        // if we have an iterator on the stack
        if (!iterators.empty())
        {
            // tell the top one to skip the specified number
            iterators.peek().skip(number);
        }
    }

    /**
     * This tells the specified loop to skip ahead the specified number of
     * iterations before doing the next iteration.
     * @see #skip(int)
     */
    public void skip(int number, String name)
    {
        // just tell the matching one to skip
        for (ManagedIterator iterator : iterators)
        {
            if (iterator.getName().equals(name))
            {
                iterator.skip(number);
                break;
            }
        }
    }

    /**
     * Don't let templates call this, but allow subclasses
     * and ManagedIterator to have access.
     */
    protected void pop()
    {
        iterators.pop();
    }


    /**
     * Wraps access to {@link ClassUtils#getIterator} is a 
     * nice little try/catch block to prevent exceptions from
     * escaping into the template.  In the case of such problems,
     * this will return {@code null}.
     */
    protected static Iterator getIterator(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        try
        {
            return ClassUtils.getIterator(obj);
        }
        catch (Exception e)
        {
            //TODO: pick up a log so we can log this
        }
        return null;
    }


    /**
     * Iterator implementation that wraps a standard {@link Iterator}
     * and allows it to be prematurely stopped, skipped ahead, and 
     * associated with a name for advanced nested loop control.
     */
    protected static class ManagedIterator implements Iterator
    {
        private String name;
        private Iterator iterator;
        private LoopTool owner;
        private boolean stopped = false;

        public ManagedIterator(Iterator iterator, LoopTool owner)
        {
            this(iterator.toString(), iterator, owner);
        }

        public ManagedIterator(String name, Iterator iterator, LoopTool owner)
        {
            if (name == null)
            {
                throw new NullPointerException("name cannot be null");
            }
            this.name = name;
            this.iterator = iterator;
            this.owner = owner;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean hasNext()
        {
            if (!stopped)
            {
                boolean hasNext = iterator.hasNext();
                // once this iterator is done, pop it from the owner's stack
                if (!hasNext)
                {
                    owner.pop();
                }
                return hasNext;
            }
            else // if stopped
            {
                return false;
            }
        }

        public Object next()
        {
            return iterator.next();
        }

        public void remove()
        {
            // Let the iterator decide whether to implement this or not
            this.iterator.remove();
        }

        public void stop()
        {
            this.stopped = true;
        }

        public void skip(int number)
        {
            for (int i=0; i < number; i++)
            {
                if (iterator.hasNext())
                {
                    iterator.next();
                }
                else
                {
                    break;
                }
            }
        }
    }

}
