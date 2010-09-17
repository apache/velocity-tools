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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <b>NOTE: This tool is considered "beta" quality due to lack of public testing
 * and is not automatically provided via the default tools.xml file.
 * </b>
 *
 * Tool to make it easier to manage usage of client-side dependencies.
 * This is essentially a simple dependency system for javascript and css.
 * This could be cleaned up to use fewer maps, use more classes,
 * and cache formatted values, but this is good enough for now.
 *
 * To use it, create a ui.xml file at the root of the classpath.
 * Follow the example below.  By default, it prepends the request context path
 * and then "css/" to every stylesheet file and the request context path
 * and "js/" to every javascript file path.  You can
 * alter those defaults by changing the type definition. In the example
 * below, the file path for the style type is changed to "/styles/", leaving out
 * the {context}.
 *
 * This is safe in request scope, but the group info (from ui.xml)
 * should only be read once.  It is not re-parsed on every request.
 * <p>
 * Example of use:
 * <pre>
 *  Template
 *  ---
 *  &lt;html&gt;
 *    &lt;head&gt;
 *      $depends.on('profile').print('
 *      ')
 *    &lt;/head&gt;
 *  ...
 *
 *  Output
 *  ------
 *  &lt;html&gt;
 *    &lt;head&gt;
 *      &lt;style rel="stylesheet" type="text/css" href="css/globals.css"/&gt;
 *      &lt;script type="text/javascript" src="js/jquery.js"&gt;&lt;/script&gt;
 *      &lt;script type="text/javascript" src="js/profile.js"&gt;&lt;/script&gt;
 *    &lt;/head&gt;
 *  ...
 *
 * Example tools.xml:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.beta.UiDependencyTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 *
 * Example ui.xml:
 * &lt;ui&gt;
 *   &lt;type name="style"&gt;&lt;![CDATA[&lt;link rel="stylesheet" type="text/css" href="/styles/{file}"&gt;]]&gt;&lt;/type&gt;
 *   &lt;group name="globals"&gt;
 *     &lt;file type="style"&gt;css/globals.css&lt;file/&gt;
 *   &lt;/group&gt;
 *   &lt;group name="jquery"&gt;
 *     &lt;file type="script"&gt;js/jquery.js&lt;file/&gt;
 *   &lt;/group&gt;
 *   &lt;group name="profile"&gt;
 *     &lt;needs&gt;globals&lt;/needs&gt;
 *     &lt;needs&gt;jquery&lt;/needs&gt;
 *     &lt;file type="script"&gt;js/profile.js&lt;file/&gt;
 *   &lt;/group&gt;
 * &lt;/ui&gt;
 * </pre>
 * </p>
 *
 * @author Nathan Bubna
 * @version $Revision: 16660 $
 */
@DefaultKey("depends")
@ValidScope(Scope.REQUEST)
public class UiDependencyTool {

    public static final String GROUPS_KEY_SPACE = UiDependencyTool.class.getName() + ":";
    public static final String TYPES_KEY_SPACE = UiDependencyTool.class.getName() + ":types:";
    public static final String SOURCE_FILE_KEY = "file";
    public static final String DEFAULT_SOURCE_FILE = "ui.xml";
    private static final List<Type> DEFAULT_TYPES;
    static {
        List<Type> types = new ArrayList<Type>();
        // start out with these two types
        types.add(new Type("style", "<link rel=\"stylesheet\" type=\"text/css\" href=\"{context}/css/{file}\"/>"));
        types.add(new Type("script", "<script type=\"text/javascript\" src=\"{context}/js/{file}\"></script>"));
        DEFAULT_TYPES = Collections.unmodifiableList(types);
    }

    private Map<String,Group> groups = null;
    private List<Type> types = DEFAULT_TYPES;
    private Map<String,List<String>> dependencies;
    private Log LOG;
    private String context = "";

    private void debug(String msg, Object... args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("UiDependencyTool: "+msg, args));
        }
    }

    protected static final void trace(Log log, String msg, Object... args) {
        if (log.isTraceEnabled()) {
            log.trace(String.format("UiDependencyTool: "+msg, args));
        }
    }

    public void configure(Map params) {
        ServletContext app = (ServletContext)params.get(ViewContext.SERVLET_CONTEXT_KEY);
        LOG = (Log)params.get(ToolContext.LOG_KEY);

        HttpServletRequest request = (HttpServletRequest)params.get(ViewContext.REQUEST);
        context = request.getContextPath();

        String file = (String)params.get(SOURCE_FILE_KEY);
        if (file == null) {
            file = DEFAULT_SOURCE_FILE;
        } else {
            debug("Loading file: %s", file);
        }

        synchronized (app) {
            // first, see if we've already read this file
            groups = (Map<String,Group>)app.getAttribute(GROUPS_KEY_SPACE+file);
            if (groups == null) {
                groups = new LinkedHashMap<String,Group>();
                // only require file presence, if one is specified
                read(file, (file != DEFAULT_SOURCE_FILE));
                app.setAttribute(GROUPS_KEY_SPACE+file, groups);
                if (types != DEFAULT_TYPES) {
                    app.setAttribute(TYPES_KEY_SPACE+file, types);
                }
            } else {
                // load any custom types too
                List<Type> alt = (List<Type>)app.getAttribute(TYPES_KEY_SPACE+file);
                if (alt != null) {
                    types = alt;
                }
            }
        }
    }

    /**
     * Adds all the files required for the specified group, then returns
     * this instance.  If the group name is null or no such group exists,
     * this will return null to indicate the error.
     */
    public UiDependencyTool on(String name) {
        Map<String,List<String>> groupDeps = getGroupDependencies(name);
        if (groupDeps == null) {
            return null;
        } else {
            addDependencies(groupDeps);
            return this;
        }
    }

    /**
     * Adds the specified file to this instance's list of dependencies
     * of the specified type, then returns this instance.  If either the
     * type or file are null, this will return null to indicate the error.
     */
    public UiDependencyTool on(String type, String file) {
        if (type == null || file == null) {
            return null;
        } else {
            addFile(type, file);
            return this;
        }
    }

    /**
     * Formats and prints all the current dependencies of this tool,
     * using a new line in between the printed/formatted files.
     */
    public String print() {
        return printAll("\n");
    }

    /**
     * If the parameter value is a known type, then this will
     * format and print all of this instance's current dependencies of the
     * specified type, using a new line in between the printed/formatted files.
     * If the parameter value is NOT a known type, then this will treat it
     * as a delimiter and print all of this instance's dependencies of all
     * types, using the specified value as the delimiter in between the
     * printed/formatted files.
     * @see #print(String,String)
     * @see #printAll(String)
     */
    public String print(String typeOrDelim) {
        if (getType(typeOrDelim) == null) {
            // then it's a delimiter
            return printAll(typeOrDelim);
        } else {
            // then it's obviously a type
            return print(typeOrDelim, "\n");
        }
    }

    /**
     * Formats and prints all of this instance's current dependencies of the
     * specified type, using the specified delimiter in between the
     * printed/formatted files.
     */
    public String print(String type, String delim) {
        List<String> files = getDependencies(type);
        if (files == null) {
            return null;
        }

        String format = getFormat(type);
        StringBuilder out = new StringBuilder();
        for (String file : files) {
            out.append(format(format, file));
            out.append(delim);
        }
        return out.toString();
    }

    /**
     * Formats and prints all the current dependencies of this tool,
     * using the specified delimiter in between the printed/formatted files.
     */
    public String printAll(String delim) {
        if (dependencies == null) {
            return null;
        }

        StringBuilder out = new StringBuilder();
        for (Type type : types) {
            if (out.length() > 0) {
                out.append(delim);
            }
            List<String> files = dependencies.get(type.name);
            if (files != null) {
                for (int i=0; i < files.size(); i++) {
                    if (i > 0) {
                        out.append(delim);
                    }
                    out.append(format(type.format, files.get(i)));
                }
            }
        }
        return out.toString();
    }

    /**
     * Sets a custom {context} variable for the formats to use.
     */
    public UiDependencyTool context(String path)
    {
        this.context = path;
        return this;
    }

    /**
     * Retrieves the configured format string for the specified file type.
     */
    public String getFormat(String type) {
        Type t = getType(type);
        if (t == null) {
            return null;
        }
        return t.format;
    }

    /**
     * Sets the format string for the specified file type.
     */
    public void setFormat(String type, String format) {
        if (format == null || type == null) {
            throw new NullPointerException("Type name and format must not be null");
        }
        // do NOT alter the defaults, just copy them
        if (types == DEFAULT_TYPES) {
            types = new ArrayList<Type>();
            for (Type t : DEFAULT_TYPES) {
                types.add(new Type(t.name, t.format));
            }
        }
        Type t = getType(type);
        if (t == null) {
            types.add(new Type(type, format));
        } else {
            t.format = format;
        }
    }

    /**
     * Returns the current dependencies of this instance, organized
     * as an ordered map of file types to lists of the required files
     * of that type.
     */
    public Map<String,List<String>> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the {@link List} of files for the specified file type, if any.
     */
    public List<String> getDependencies(String type) {
        if (dependencies == null) {
            return null;
        }
        return dependencies.get(type);
    }

    /**
     * Returns the dependencies of the specified group, organized
     * as an ordered map of file types to lists of the required files
     * of that type.
     */
    public Map<String,List<String>> getGroupDependencies(String name) {
        Group group = getGroup(name);
        if (group == null) {
            return null;
        }
        return group.getDependencies(this);
    }

    /**
     * Returns an empty String to avoid polluting the template output after a
     * successful call to {@link #on(String)} or {@link #on(String,String)}.
     */
    @Override
    public String toString() {
        return "";
    }


    /**
     * Reads group info out of the specified file and into this instance.
     * If the file cannot be found and required is true, then this will throw
     * an IllegalArgumentException.  Otherwise, it will simply do nothing. Any
     * checked exceptions during the actual reading of the file are caught and
     * wrapped as {@link RuntimeException}s.
     */
    protected void read(String file, boolean required) {
        debug("UiDependencyTool: Reading file from %s", file);
        URL url = toURL(file);
        if (url == null) {
            String msg = "UiDependencyTool: Could not read file from '"+file+"'";
            if (required) {
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            } else {
                LOG.debug(msg);
            }
        } else {
            Digester digester = createDigester();
            try
            {
                digester.parse(url.openStream());
            }
            catch (SAXException saxe)
            {
                LOG.error("UiDependencyTool: Failed to parse '"+file+"'", saxe);
                throw new RuntimeException("While parsing the InputStream", saxe);
            }
            catch (IOException ioe)
            {
                LOG.error("UiDependencyTool: Failed to read '"+file+"'", ioe);
                throw new RuntimeException("While handling the InputStream", ioe);
            }
        }
    }

    /**
     * Creates the {@link Digester} used by {@link #read} to create
     * the group info for this instance out of the specified XML file.
     */
    protected Digester createDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setUseContextClassLoader(true);
        digester.addRule("ui/type", new TypeRule());
        digester.addRule("ui/group", new GroupRule());
        digester.addRule("ui/group/file", new FileRule());
        digester.addRule("ui/group/needs", new NeedsRule());
        digester.push(this);
        return digester;
    }

    /**
     * Applies the format string to the given value.  Currently,
     * this simply replaces '{file}' with the value.  If you
     * want to handle more complicated formats, override this method.
     */
    protected String format(String format, String value) {
        if (format == null) {
            return value;
        }
        return format.replace("{file}", value).replace("{context}", this.context);
    }

    /**
     * NOTE: This method may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected Group getGroup(String name) {
        if (groups == null) {
            return null;
        }
        return groups.get(name);
    }

    /**
     * NOTE: This method may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected Group makeGroup(String name) {
        trace(LOG, "Creating group '%s'", name);
        Group group = new Group(name, LOG);
        groups.put(name, group);
        return group;
    }

    /**
     * Adds the specified files organized by type to this instance's
     * current dependencies.
     */
    protected void addDependencies(Map<String,List<String>> fbt) {
        if (this.dependencies == null) {
            dependencies = new LinkedHashMap<String,List<String>>(fbt.size());
        }
        for (Map.Entry<String,List<String>> entry : fbt.entrySet()) {
            String type = entry.getKey();
            if (getType(type) == null) {
                LOG.error("UiDependencyTool: Type '"+type+"' is unknown and will not be printed unless defined.");
            }
            List<String> existing = dependencies.get(type);
            if (existing == null) {
                existing =  new ArrayList<String>(entry.getValue().size());
                dependencies.put(type, existing);
            }
            for (String file : entry.getValue()) {
                if (!existing.contains(file)) {
                    trace(LOG, "Adding %s: %s", type, file);
                    existing.add(file);
                }
            }
        }
    }

    /**
     * Adds a file to this instance's dependencies under the specified type.
     */
    protected void addFile(String type, String file) {
        List<String> files = null;
        if (dependencies == null) {
            dependencies = new LinkedHashMap<String,List<String>>(types.size());
        } else {
            files = dependencies.get(type);
        }
        if (files == null) {
            files = new ArrayList<String>();
            dependencies.put(type, files);
        }
        if (!files.contains(file)) {
            trace(LOG, "Adding %s: %s", type, file);
            files.add(file);
        }
    }


    /**
     * For internal use only. Use/override get/setFormat instead.
     */
    private Type getType(String type) {
        for (Type t : types) {
            if (t.name.equals(type)) {
                return t;
            }
        }
        return null;
    }

    //TODO: replace this method with ConversionUtils.toURL(file, this)
    //      once VelocityTools 2.0-beta3 or 2.0 final is released.
    private URL toURL(String file) {
        try
        {
            return ClassUtils.getResource(file, this);
        }
        catch (Exception e) {
            return null;
        }
    }


    /**
     * NOTE: This class may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected static class Group {

        private volatile boolean resolved = true;
        private String name;
        private Map<String,Integer> typeCounts = new LinkedHashMap<String,Integer>();
        private Map<String,List<String>> dependencies = new LinkedHashMap<String,List<String>>();
        private List<String> groups;
        private Log LOG;

        public Group(String name, Log log) {
            this.name = name;
            this.LOG = log;
        }

        private void trace(String msg, Object... args) {
            if (LOG.isTraceEnabled()) {
                UiDependencyTool.trace(LOG, "Group "+name+": "+msg, args);
            }
        }

        public void addFile(String type, String value) {
            List<String> files = dependencies.get(type);
            if (files == null) {
                files = new ArrayList<String>();
                dependencies.put(type, files);
            }
            if (!files.contains(value)) {
                trace("Adding %s: %s", type, value);
                files.add(value);
            }
        }

        public void addGroup(String group) {
            if (this.groups == null) {
                this.resolved = false;
                this.groups = new ArrayList<String>();
            }
            if (!this.groups.contains(group)) {
                trace("Adding group %s", group, name);
                this.groups.add(group);
            }
        }

        public Map<String,List<String>> getDependencies(UiDependencyTool parent) {
            resolve(parent);
            return this.dependencies;
        }

        protected void resolve(UiDependencyTool parent) {
            if (!resolved)  {
                // mark first to keep circular from becoming infinite
                resolved = true;
                trace("resolving...");
                for (String name : groups) {
                    Group group = parent.getGroup(name);
                    if (group == null) {
                        throw new NullPointerException("No group named '"+name+"'");
                    }
                    Map<String,List<String>> dependencies = group.getDependencies(parent);
                    for (Map.Entry<String,List<String>> type : dependencies.entrySet()) {
                        for (String value : type.getValue()) {
                            addFileFromGroup(type.getKey(), value);
                        }
                    }
                }
                trace(" is resolved.");
            }
        }

        private void addFileFromGroup(String type, String value) {
            List<String> files = dependencies.get(type);
            if (files == null) {
                files = new ArrayList<String>();
                files.add(value);
                trace("adding %s '%s' first", type, value);
                dependencies.put(type, files);
                typeCounts.put(type, 1);
            } else if (!files.contains(value)) {
                Integer count = typeCounts.get(type);
                if (count == null) {
                    count = 0;
                }
                files.add(count, value);
                trace("adding %s '%s' at %s", type, value, count);
                typeCounts.put(type, ++count);
            }
        }
    }

    /**
     * NOTE: This class may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected static class TypeRule extends Rule {

        private UiDependencyTool parent;

        public void begin(String ns, String el, Attributes attributes) throws Exception {
            parent = (UiDependencyTool)digester.peek();

            for (int i=0; i < attributes.getLength(); i++) {
                String name = attributes.getLocalName(i);
                if ("".equals(name)) {
                    name = attributes.getQName(i);
                }
                if ("name".equals(name)) {
                    digester.push(attributes.getValue(i));
                }
            }
        }

        public void body(String ns, String el, String typeFormat) throws Exception {
            String typeName = (String)digester.pop();
            parent.setFormat(typeName, typeFormat);
        }
    }

    /**
     * NOTE: This class may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected static class GroupRule extends Rule {

        private UiDependencyTool parent;

        public void begin(String ns, String el, Attributes attributes) throws Exception {
            parent = (UiDependencyTool)digester.peek();

            for (int i=0; i < attributes.getLength(); i++) {
                String name = attributes.getLocalName(i);
                if ("".equals(name)) {
                    name = attributes.getQName(i);
                }
                if ("name".equals(name)) {
                    digester.push(parent.makeGroup(attributes.getValue(i)));
                }
            }
        }

        public void end(String ns, String el) throws Exception {
            digester.pop();
        }
    }

    /**
     * NOTE: This class may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected static class FileRule extends Rule {

        public void begin(String ns, String el, Attributes attributes) throws Exception {
            for (int i=0; i < attributes.getLength(); i++) {
                String name = attributes.getLocalName(i);
                if ("".equals(name)) {
                    name = attributes.getQName(i);
                }
                if ("type".equals(name)) {
                    digester.push(attributes.getValue(i));
                }
            }
        }

        public void body(String ns, String el, String value) throws Exception {
            String type = (String)digester.pop();
            Group group = (Group)digester.peek();
            group.addFile(type, value);
        }
    }

    /**
     * NOTE: This class may change or disappear w/o warning; don't depend
     * on it unless you're willing to update your code whenever this changes.
     */
    protected static class NeedsRule extends Rule {

        public void body(String ns, String el, String otherGroup) throws Exception {
            Group group = (Group)digester.peek();
            group.addGroup(otherGroup);
        }
    }


    private static final class Type {

        protected String name;
        protected String format;

        Type(String n, String f) {
            name = n;
            format = f;
        }
    }

}
