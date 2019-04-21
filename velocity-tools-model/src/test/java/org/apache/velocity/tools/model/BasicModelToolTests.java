package org.apache.velocity.tools.model;

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


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.ToolboxFactory;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.XmlFactoryConfiguration;
import org.apache.velocity.tools.model.context.ModelTool;
import org.apache.velocity.tools.model.filter.Filter;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.apache.velocity.tools.config.ConfigurationUtils.MODEL_DEFAULTS_PATH;
import static org.junit.Assert.*;

/**
 * <p>Basic model tests</p>
 *
 * @author Claude Brisson
 * @since VelocityTools 3.1
 * @version $Id$
 */
public class BasicModelToolTests extends BaseBookshelfTests
{
    private static final String BLANK_MODEL = "blank_model.xml";
    private static final String BLANK_MODEL_TOOLS = "blank_model_tools.xml";

    public @Test void instanciateToolbox() throws Exception
    {
        XmlFactoryConfiguration config = new XmlFactoryConfiguration();
        config.read(MODEL_DEFAULTS_PATH, true);
        config.setProperty("datasource", initDataSource());
        ToolboxFactory factory = config.createFactory();
        Toolbox toolbox = factory.createToolbox("application");
        Object obj = toolbox.get("model");
        assertNotNull(obj);
        assertTrue(obj instanceof ModelTool);
    }

    public @Test void loadToolbox() throws Exception
    {
        ToolManager manager = new ToolManager();
        manager.setVelocityEngine(createVelocityEngine((String)null));
        FactoryConfiguration config = ConfigurationUtils.find("org/apache/velocity/tools/model/tools.xml");
        FactoryConfiguration modelConfig = ConfigurationUtils.find("blank_model_tools.xml");
        config.addConfiguration(modelConfig);
        manager.configure(config);
        Context context = manager.createContext();
        Object obj = context.get("model");
        assertNotNull(obj);
        assertTrue(obj instanceof ModelTool);
    }

    public @Test void testModelInit() throws Exception
    {
        // test model
        Model model = new Model();
        model.setDataSource(initDataSource());
        model.initialize(getResource("test_init_model.xml"));
        assertEquals(Model.WriteAccess.JAVA, model.getWriteAccess());

        // test entities
        Entity book = model.getEntity("book");
        assertNotNull(book);
        Entity author = model.getEntity("author");
        assertNotNull(author);

        // test attributes
        Attribute countAuthors = book.getAttribute("count_authors");
        assertNotNull(countAuthors);
        assertTrue(countAuthors instanceof ScalarAttribute);
        Attribute countBooks = model.getAttribute("count_books");
        assertNotNull(countBooks);
        assertTrue(countBooks instanceof ScalarAttribute);
        Attribute authors = book.getAttribute("authors");
        assertNotNull(author);
        assertTrue(authors instanceof RowsetAttribute);
    }

    public @Test void testRealData() throws Exception
    {
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.initialize(getResourceReader("test_init_model.xml"));
        ScalarAttribute countBooks = (ScalarAttribute)model.getAttribute("count_books");
        long books = countBooks.getLong();
        assertEquals(books, 1);
    }

    public @Test void testReverseColumns() throws Exception
    {
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.setReverseMode(Model.ReverseMode.COLUMNS);
        model.initialize(getResourceReader("test_init_model.xml"));
        Entity book = model.getEntity("book");
        Collection<Entity.Column> columns = book.getColumns();
        assertNotNull(columns);
        String allCols = columns.stream().map(c -> c.name).collect(Collectors.joining(","));
        assertEquals("book_id,title,published,publisher_id", allCols);
        List<Entity.Column> pk = book.getPrimaryKey();
        assertNotNull(pk);
        assertEquals(1, pk.size());
        assertEquals("book_id", pk.get(0).name);
    }

    public @Test void testBasicFetch() throws Exception
    {
        Map identMapping = new HashMap();
        identMapping.put("*", "lowercase");
        identMapping.put("*.*", "lowercase");
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.setReverseMode(Model.ReverseMode.COLUMNS);
        model.getIdentifiers().setMapping(identMapping);
        model.initialize(getResourceReader("test_init_model.xml"));
        Entity book = model.getEntity("book");
        assertNotNull(book);
        Instance oneBook = book.fetch(1);
        assertNotNull(oneBook);
        String title = oneBook.getString("title");
        assertEquals("The Astonishing Life of Duncan Moonwalker", title);
        Instance otherBook = book.fetch(1);
        assertEquals(oneBook, otherBook);
    }

    public @Test void testCount() throws Exception
    {
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.setReverseMode(Model.ReverseMode.COLUMNS);
        model.initialize(getResourceReader("test_init_model.xml"));
        Entity authors = model.getEntity("author");
        assertNotNull(authors);
        long count = authors.getCount();
        assertEquals(2l, count);
    }

    public @Test void testReverseJoins() throws Exception
    {
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.setReverseMode(Model.ReverseMode.JOINS);
        model.getIdentifiers().setInflector("org.atteo.evo.inflector.English");
        model.getIdentifiers().setMapping("lowercase");
        model.initialize(getResourceReader("test_minimal_model.xml"));

        // test upstream attribute
        Entity bookEntity = model.getEntity("book");
        assertNotNull(bookEntity);
        Attribute bookPublisher = bookEntity.getAttribute("publisher");
        assertNotNull(bookPublisher);
        assertTrue(bookPublisher instanceof RowAttribute);
        Instance book = bookEntity.fetch(1);
        assertNotNull(book);
        Instance publisher = book.retrieve("publisher");
        assertNotNull(publisher);
        assertEquals("Green Penguin Books", publisher.getString("name"));

        // test downstream attribute
        Entity publisherEntity = model.getEntity("publisher");
        assertNotNull(publisherEntity);
        Instance firstPublisher = publisherEntity.fetch(1);
        assertNotNull(firstPublisher);
        Iterator<Instance> books = firstPublisher.query("books");
        assertNotNull(books);
        assertTrue(books.hasNext());
        assertNotNull(books.next());
    }

    public @Test void testAction() throws Exception
    {
        DataSource dataSource = initDataSource();
        Model model = new Model();
        model.setDataSource(dataSource);
        model.setReverseMode(Model.ReverseMode.COLUMNS);
        model.initialize(getResourceReader("test_action.xml"));
        Entity book = model.getEntity("book");
        assertNotNull(book);
        Instance oneBook = book.fetch(1);
        assertNotNull(oneBook);
        String title = oneBook.getString("title");
        assertEquals("The Astonishing Life of Duncan Moonwalker", title);
        Action censor = (Action)book.getAttribute("censor");
        assertNotNull(censor);
        int changed = censor.perform(oneBook);
        assertEquals(1, changed);
        oneBook = book.fetch(1);
        assertNotNull(oneBook);
        String censored = oneBook.getString("title");
        assertEquals("** censored **", censored);
        oneBook.perform("rename", title);
        oneBook = book.fetch(1);
        assertNotNull(oneBook);
        assertEquals(title, oneBook.getString("title"));
    }

    public @Test void testUberspector() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties velProps = new Properties();
        velProps.put("introspector.uberspect.class", "org.apache.velocity.tools.model.context.ModelUberspector, org.apache.velocity.util.introspection.UberspectImpl");
        VelocityEngine engine = createVelocityEngine(velProps);
        ModelTool model = new ModelTool();
        Map<String, Object> props = new HashMap<>();
        props.put("datasource", dataSource);
        props.put("reverse", "full");
        props.put("definition", "blank_model.xml");
        props.put("identifiers.inflector", "org.atteo.evo.inflector.English");
        props.put("identifiers.mapping", "lowercase");
        model.configure(props);
        Context context = new VelocityContext();
        context.put("model", model);
        StringWriter out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "$model.book.fetch(1).publisher.name"));
        assertEquals("Green Penguin Books", out.toString());
        out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "#foreach( $book_author in $model.book.fetch(1).book_authors )$book_author.author.author_id#end"));
        assertEquals("12", out.toString());
    }

    public @Test void testExtended() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties velProps = new Properties();
        velProps.put("introspector.uberspect.class", "org.apache.velocity.tools.model.context.ModelUberspector,org.apache.velocity.util.introspection.UberspectImpl");
        velProps.put("model.datasource", dataSource);
        velProps.put("model.reverse", "extended");
        velProps.put("model.identifiers.mapping", "lowercase");
        velProps.put("model.identifiers.mapping.pub*.*_id", "/^.*_id/id/");
        velProps.put("model.identifiers.mapping.author.*_id", "/^.*_id/id/");
        velProps.put("model.identifiers.inflector", "org.atteo.evo.inflector.English");
        VelocityEngine engine = createVelocityEngine(velProps);

        Map params = new HashMap();
        params.put("velocityEngine", engine);
        ModelTool model = new ModelTool();
        model.configure(params);

        Context context = new VelocityContext();
        context.put("model", model);
        StringWriter out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "$model.book.fetch(1).publisher.id"));
        assertEquals("1", out.toString());
        out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "#foreach( $author in $model.book.fetch(1).authors )$author.id#end"));
        assertEquals("12", out.toString());
    }

    public @Test void testCollision() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties velProps = new Properties();
        velProps.put("introspector.uberspect.class", "org.apache.velocity.tools.model.context.ModelUberspector,org.apache.velocity.util.introspection.UberspectImpl");
        velProps.put("model.datasource", dataSource);
        velProps.put("model.reverse", "extended");
        velProps.put("model.identifiers.mapping", "lowercase");
        velProps.put("model.identifiers.mapping.*.*_id", "/^.*_id/id/");
        velProps.put("model.identifiers.inflector", "org.atteo.evo.inflector.English");
        VelocityEngine engine = createVelocityEngine(velProps);

        Map params = new HashMap();
        params.put("velocityEngine", engine);
        Model model = new Model().configure(params);
        try
        {
            model.initialize();
            fail("initialization should throw");
        }
        catch (ConfigurationException e)
        {
            assertEquals("column name collision: book.id mapped on BOOK.BOOK_ID and on BOOK.PUBLISHER_ID", e.getMessage());
        }
    }

    public @Test void testWithoutInputFilter() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties props = new Properties();
        props.put("datasource", dataSource);
        props.put("reverse", "tables");
        props.put("identifiers.inflector", "org.atteo.evo.inflector.English");
        props.put("identifiers.mapping", "lowercase");

        Model model = new Model().configure(props).initialize();
        Instance book = model.getEntity("book").fetch(1);
        Date date = (Date)book.get("published");
        assertNotNull(date);
        DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2018-05-09", ymd.format(date));
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2008, 8, 8);
        book.put("published", cal);
        try
        {
            book.update();
            fail("should throw when hsqldb receives a Calendar");
        }
        catch (SQLException sqle)
        {
            assertEquals("incompatible data type in conversion", sqle.getMessage());
        }
    }

    public @Test void testInputFilter() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties props = new Properties();
        props.put("datasource", dataSource);
        props.put("reverse", "tables");
        props.put("identifiers.inflector", "org.atteo.evo.inflector.English");
        props.put("identifiers.mapping", "lowercase");
        Filter calendar_to_time = x -> ((Calendar)x).getTime();
        props.put("filters.write.java.util.Calendar", calendar_to_time);

        Model model = new Model().configure(props).initialize();
        Instance book = model.getEntity("book").fetch(1);
        Date date = (Date)book.get("published");
        assertNotNull(date);
        DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2018-05-09", ymd.format(date));
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2008, 8, 8);
        book.put("published", cal);
        book.update();
        book.refresh();
        assertEquals("2008-09-08", ymd.format(book.get("published")));
        book.put("published", date);
        book.update();
        book.refresh();
        assertEquals("2018-05-09", ymd.format(book.get("published")));
    }

    public @Test void testJdbc() throws Exception
    {
        Model model = new Model().setDatabaseURL("jdbc:hsqldb:.");
        model.getCredentials().setUser("sa");
        model.getCredentials().setPassword("");
        model.initialize();
    }

    public @Test void testObfuscation() throws Exception
    {
        Properties velProps = new Properties();
        velProps.load(BasicModelToolTests.class.getClassLoader().getResourceAsStream("org/apache/velocity/tools/model/velocity.properties"));
        VelocityEngine engine = createVelocityEngine(velProps);

        Properties props = new Properties();
        props.put("reverse", "extended");
        props.put("credentials.user", "sa");
        props.put("credentials.password", "");
        props.put("database", "jdbc:hsqldb:.");
        props.put("identifiers.mapping.*", "lowercase");
        props.put("identifiers.mapping.*.*_id", "snake_to_camel");
        props.put("filters.read.book.book_id", "obfuscate");
        props.put("filters.write.book.book_id", "deobfuscate_strings");
        props.put("filters.read.author.author_id", "obfuscate");
        props.put("filters.write.author.author_id", "deobfuscate");
        props.put("velocityEngine", engine);
        Model model = new Model().configure(props).initialize();
        Instance book = model.getEntity("book").fetch(1);
        assertNotNull(book);
        Object bookId = book.get("bookId");
        assertNotNull(bookId);
        assertTrue(bookId instanceof String && ((String)bookId).length() > 5);
        Iterator<Instance> authors = book.query("authors");
        assertNotNull(authors);
        Instance author1 = authors.next();
        Instance author2 = authors.next();
        assertFalse(authors.hasNext());
        assertNotNull(author1);
        assertNotNull(author2);
        Object authorId = author1.get("authorId");
        assertNotNull(authorId);
        assertTrue(authorId instanceof String && ((String)authorId).length() > 5);
        Instance again = author1.query("books").next();
        assertNotNull(again);
        assertEquals(book.get("bookId"), again.get("bookId"));
        try
        {
            Instance author = model.getEntity("author").fetch(1);
            fail("SQLException expected");
        }
        catch (SQLException sqle)
        {
            assertEquals("data exception: invalid character value for cast", sqle.getMessage());
        }
    }

    public static class MyBook
    {
        public void setISBN(String isbn) { this.isbn = isbn.toUpperCase(); }
        public String getISBN() { return isbn; }
        private String isbn = null;
    }

    public static class MyPub extends Instance
    {
        public MyPub(Entity entity) { super(entity); }

        public String getAddress()
        {
            return "some address";
        }
    }

    public static class MyAuthor
    {
    }

    public static class MyFactory
    {
        public static MyAuthor createAuthor()
        {
            return new MyAuthor();
        }
    }

    public @Test void testBean() throws Exception
    {
        Properties velProps = new Properties();
        velProps.load(BasicModelToolTests.class.getClassLoader().getResourceAsStream("org/apache/velocity/tools/model/velocity.properties"));
        VelocityEngine engine = createVelocityEngine(velProps);

        Properties props = new Properties();
        props.put("reverse", "extended");
        props.put("credentials.user", "sa");
        props.put("credentials.password", "");
        props.put("database", "jdbc:hsqldb:.");
        props.put("identifiers.mapping", "lowercase");
        props.put("velocityEngine", engine);
        props.put("instances.classes.book", MyBook.class);
        props.put("instances.classes.publisher", MyPub.class);
        props.put("instances.factory", MyFactory.class);
        Model model = new Model().configure(props).initialize();
        Instance book = model.getEntity("book").fetch(1);
        assertNotNull(book);
        Object bookId = book.get("book_id");
        assertNotNull(bookId);
        assertEquals(1, bookId);
        assertTrue(book instanceof WrappingInstance);
        assertNull(book.put("ISBN", "lowercase characters"));
        assertEquals("LOWERCASE CHARACTERS", book.get("ISBN"));
        Instance publisher = book.retrieve("publisher");
        assertTrue(publisher instanceof MyPub);
        String address = ((MyPub)publisher).getAddress();
        assertNotNull(address);
        assertEquals("some address", address);
        Instance author = model.getEntity("author").fetch(1);
        assertNotNull(author);
        assertTrue(author instanceof WrappingInstance);
        Object wrapped = ((WrappingInstance)author).unwrap(MyAuthor.class);
        assertNotNull(wrapped);
    }

    @BeforeClass
    public static void populateDataSource() throws Exception
    {
        BaseBookshelfTests.populateDataSource();
    }

}
