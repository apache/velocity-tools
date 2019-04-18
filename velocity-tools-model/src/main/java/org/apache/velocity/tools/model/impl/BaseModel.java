package org.apache.velocity.tools.model.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.XmlUtils;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.model.Attribute;
import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.Instance;
import org.apache.velocity.tools.model.Model;
import org.apache.velocity.tools.model.WrappingInstance;
import org.apache.velocity.tools.model.config.ConfigDigester;
import org.apache.velocity.tools.model.config.ConfigHelper;
import org.apache.velocity.tools.model.config.Constants;
import org.apache.velocity.tools.model.filter.ValueFilterHandler;
import org.apache.velocity.tools.model.filter.Identifiers;
import org.apache.velocity.tools.model.sql.BasicDataSource;
import org.apache.velocity.tools.model.sql.ConnectionPool;
import org.apache.velocity.tools.model.sql.ConnectionWrapper;
import org.apache.velocity.tools.model.sql.Credentials;
import org.apache.velocity.tools.model.sql.DriverInfos;
import org.apache.velocity.tools.model.sql.StatementPool;
import org.apache.velocity.tools.model.util.Cryptograph;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public abstract class BaseModel extends AttributeHolder implements Constants
{
    public BaseModel()
    {
    }

    /*
     * Configuration
     */

    public Model configure(Map params)
    {
        return configure(new ConfigHelper(params));
    }

    protected Model configure(ConfigHelper config)
    {
        try
        {
            setWriteAccess(config.getEnum(MODEL_WRITE_ACCESS, getWriteAccess()));
            setReverseMode(config.getEnum(MODEL_REVERSE_MODE, getReverseMode()));
            Optional.ofNullable(config.getVelocityEngine()).ifPresent(this::setVelocityEngine);
            Optional.ofNullable(config.getString(MODEL_SCHEMA)).ifPresent(this::setSchema);
            Optional.ofNullable(config.getString(MODEL_IDENTIFIERS_INFLECTOR)).ifPresent(getIdentifiers()::setInflector);
            Object flatMapping = config.get(MODEL_IDENTIFIERS_MAPPING);
            if (flatMapping != null)
            {
                if (flatMapping instanceof String)
                {
                    getIdentifiers().setMapping((String)flatMapping);
                }
                else if(flatMapping instanceof Map)
                {
                    getIdentifiers().setMapping((Map)flatMapping);
                }
                else
                {
                    throw new ConfigurationException("expecting a string or a map for property " + MODEL_IDENTIFIERS_MAPPING);
                }
            }
            getIdentifiers().setMapping(config.getSubProperties(MODEL_IDENTIFIERS_MAPPING));
            getFilters().setReadMapping(config.getSubProperties(MODEL_FILTERS_READ));
            getFilters().setWriteMapping(config.getSubProperties(MODEL_FILTERS_WRITE));
            Object dataSource = config.get(MODEL_DATASOURCE);
            if (dataSource != null)
            {
                try
                {
                    if (dataSource instanceof String)
                    {
                        setDataSource((String)dataSource);
                    }
                    else if (dataSource instanceof DataSource)
                    {
                        setDataSource((DataSource)dataSource);
                    }
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("could not set model datasource", e);
                }
            }

            Optional.ofNullable(config.getString(MODEL_FILTERS_CRYPTOGRAPH)).ifPresent(getFilters()::setCryptographClass);

            Optional.ofNullable(config.get(MODEL_INSTANCES_FACTORY)).ifPresent(getInstances()::setFactory);
            Optional.ofNullable(config.getSubProperties(MODEL_INSTANCES_CLASSES)).ifPresent(getInstances()::setClasses);

            Optional.ofNullable(config.getString(MODEL_DATABASE)).ifPresent(this::setDatabaseURL);
            Optional.ofNullable(config.getString(MODEL_CREDENTIALS_USER)).ifPresent(getCredentials()::setUser);
            Optional.ofNullable(config.getString(MODEL_CREDENTIALS_PASSWORD)).ifPresent(getCredentials()::setPassword);

            String path = config.getString(MODEL_DEFINITION);
            boolean useDefault = false;
            if (path == null)
            {
                URL definition = getDefinition();
                if (definition == null)
                {
                    useDefault = true;
                    path = MODEL_DEFAULT_PATH;
                }
            }
            if (path != null)
            {
                try
                {
                    setDefinition(config.findURL(path));
                }
                catch (ConfigurationException ce)
                {
                    if (!useDefault)
                    {
                        throw ce;
                    }
                }
            }
            return getModel();
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new ConfigurationException("configuration problem", e);
        }
    }

    public NavigableMap<String, Attribute> getConfig()
    {
        return new TreeMap(); // TODO
    }

    /*
     * Initialization
     */

    public Model initialize()
    {
        return initialize(getDefinition());
    }

    public Model initialize(URL url)
    {
        return initialize("default", url);
    }

    public Model initialize(String id, URL url)
    {
        try
        {
            if (url == null)
            {
                initialize(id, (Reader)null);
            }
            else
            {
                setDefinition(url);
                Reader reader = new InputStreamReader(url.openStream());
                InputSource source = new InputSource(reader);
                source.setSystemId(url.toExternalForm());
                initialize(id, source);
            }
        }
        catch (IOException ioe)
        {
            throw new VelocityException("could not initialize model", ioe);
        }
        return getModel();
    }

    public Model initialize(String id, String path)
    {
        return initialize(id, new ConfigHelper().findURL(path));
    }

    public Model initialize(String id)
    {
        return initialize(id, getDefinition());
    }

    public Model initialize(Reader reader)
    {
        return initialize("default", new InputSource(reader));
    }

    public Model initialize(String id, Reader reader) throws ConfigurationException
    {
        return initialize(id, reader == null ? null : new InputSource(reader));

    }

    public Model initialize(String id, InputSource source) throws ConfigurationException
    {
        this.modelId = id;
        try
        {
            readDefinition(source);
            connect();
            getIdentifiers().initialize();
            getFilters().initialize();
            reverseEngineer();
            getInstances().initialize();
            initializeAttributes();
            modelRepository.put(id, getModel());
        }
        catch (ConfigurationException ce)
        {
            throw ce;
        }
        catch (Exception e)
        {
            throw new ConfigurationException("could not initialize model", e);
        }
        return getModel();
    }

    protected void readDefinition(InputSource source) throws Exception
    {
        if (source == null)
        {
            return;
        }
        DocumentBuilderFactory builderFactory = XmlUtils.createDocumentBuilderFactory();
        builderFactory.setXIncludeAware(true);
        Element doc = builderFactory.newDocumentBuilder().parse(source).getDocumentElement();
        String rootTag = doc.getTagName();
        // support the deprecated 'database' root tag
        if ("database".equals(rootTag))
        {
            getLogger().warn("<database> root tag has been deprecated in favor of <model>");
        }
        else if (!"model".equals(rootTag))
        {
            throw new ConfigurationException("expecting a <model> root tag");
        }
        new ConfigDigester(doc, this).process();
    }

    protected void connect() throws Exception
    {
        if (dataSource == null)
        {
            if (databaseURL == null)
            {
                throw new ConfigurationException("cannot connect: no data source");
            }
            else
            {
                setDataSource(new BasicDataSource(databaseURL));
            }
        }
        // override driver properties deduced from database metadata
        // with properties provided by the user
        Connection connection = dataSource.getConnection();
        Properties props = ReverseEngineer.getStockDriverProperties(connection.getMetaData().getURL());
        DriverInfos stockInfos = new DriverInfos();
        ConfigDigester.setProperties(this, props);
        getDriverInfos().setDefaults(stockInfos);
        connectionPool = new ConnectionPool(dataSource, credentials, driverInfos, schema, true, maxConnections);
        transactionConnectionPool = new ConnectionPool(dataSource, credentials, driverInfos, schema, false, maxConnections);
        statementPool = new StatementPool(connectionPool);
    }

    /*
     * Getters and setters
     */

    public String getModelId()
    {
        return modelId;
    }

    public WriteAccess getWriteAccess()
    {
        return writeAccess;
    }

    public Model setWriteAccess(WriteAccess writeAccess)
    {
        this.writeAccess = writeAccess;
        return getModel();
    }

    public ReverseMode getReverseMode()
    {
        return reverseMode;
    }

    public Model setReverseMode(ReverseMode reverseMode)
    {
        this.reverseMode = reverseMode;
        return getModel();
    }

    public VelocityEngine getVelocityEngine()
    {
        return velocityEngine;
    }

    public Model setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
        return getModel();
    }

    public Model setDataSource(String dataSourceName) throws Exception
    {
        Context ctx = InitialContext.doLookup("java:comp/env");
        DataSource dataSource = (DataSource)ctx.lookup(dataSourceName);
        return setDataSource(dataSource);
    }

    public Model setDataSource(DataSource dataSource) throws Exception
    {
        if (this.dataSource != null)
        {
            throw new ConfigurationException("data source cannot be changed (no dynamic reloading)");
        }
        this.dataSource = dataSource;
        return getModel();
    }

    public String getDatabaseURL()
    {
        return databaseURL;
    }

    public Model setDatabaseURL(String databaseURL)
    {
        this.databaseURL = databaseURL;
        return getModel();
    }

    public String getSchema()
    {
        return schema;
    }

    public Model setSchema(String schema)
    {
        this.schema = schema;
        if (connectionPool != null)
        {
            connectionPool.setSchema(schema);
        }
        return getModel();
    }

    public URL getDefinition()
    {
        return definition;
    }

    public Model setDefinition(String path) throws MalformedURLException
    {
        if (path != null && path.contains("://"))
        {
            setDefinition(new URL(path));
        }
        return getModel();
    }

    public Model setDefinition(URL definition)
    {
        this.definition = definition;
        return getModel();
    }

    public Credentials getCredentials()
    {
        return credentials;
    }

    public Identifiers getIdentifiers()
    {
        return identifiers;
    }

    public FiltersSet getFilters()
    {
        return filters;
    }

    public Entity getEntity(String name)
    {
        return entitiesMap.get(name);
    }

    public DriverInfos getDriverInfos()
    {
        return driverInfos;
    }

    protected ConnectionPool getConnectionPool()
    {
        return connectionPool;
    }

    protected StatementPool getStatementPool()
    {
        return statementPool;
    }

    protected ConnectionWrapper getTransactionConnection() throws SQLException
    {
        return transactionConnectionPool.getConnection();
    }

    public NavigableMap<String, Entity> getEntities()
    {
        return Collections.unmodifiableNavigableMap(entitiesMap);
    }

    protected UserInstancesConfig getInstances()
    {
        return userInstancesConfig;
    }

    /*
     * Definition
     */

    public void addEntity(Entity entity)
    {
        entitiesMap.put(entity.getName(), entity);
    }

    protected void reverseEngineer() throws SQLException
    {
        if (connectionPool == null)
        {
            getLogger().warn("connection pool not available: not performing reverse enginering");
            return;
        }
        ConnectionWrapper connection = null;
        try
        {
            connection = connectionPool.getConnection();
            connection.enterBusyState();
            ReverseEngineer reverseEngineer = new ReverseEngineer(connection.getMetaData(), driverInfos);

            // adapt known entities table case if necessary
            for (Entity entity : entitiesMap.values())
            {
                String table = entity.getTable();
                if (table == null) table = entity.getName();
                table = driverInfos.getTableName(table);
                entity.setTable(table);
            }

            if (getReverseMode().reverseColumns())
            {
                // build a temporary map of declared entities per tables
                Map<String, Entity> knownEntitiesByTable = new TreeMap<>();
                for (Entity entity : entitiesMap.values())
                {
                    Entity prev = knownEntitiesByTable.put(entity.getTable(), entity);
                    if (prev != null)
                    {
                        throw new ConfigurationException("entity table name collision: entities " + entity.getName() + " and " + prev.getName() + " both reference table " + entity.getTable());
                    }
                }

                // reverse enginering of tables if asked so
                if (getReverseMode().reverseTables())
                {
                    for (String table : reverseEngineer.getTables())
                    {
                        Entity entity = knownEntitiesByTable.get(table);
                        if (entity == null)
                        {
                            String entityName = getIdentifiers().transformTableName(table);
                            entity = getEntity(entityName);
                            if (entity != null)
                            {
                                if (entity.getTable() != null)
                                {
                                    throw new ConfigurationException("entity table name collision: entity " + entity.getName() + " maps both tables " + entity.getTable() + " and " + table);
                                }
                                getLogger().warn("binding entity {} to table {}", entity.getName(), table);
                                entity.setTable(table);
                                knownEntitiesByTable.put(table, entity);
                            }
                            else
                            {
                                entity = new Entity(entityName, getModel());
                                entity.setTable(table);
                                addEntity(entity);
                                knownEntitiesByTable.put(table, entity);
                            }
                        }
                    }
                }

                // reverse enginering of columns and primary key
                for (Entity entity : knownEntitiesByTable.values())
                {
                    List<Entity.Column> columns = reverseEngineer.getColumns(entity);
                    for (Entity.Column column : columns)
                    {
                        entity.addColumn(column);
                    }
                    entity.setSqlPrimaryKey(reverseEngineer.getPrimaryKey(entity));
                }

                // reverse enginering of joins, if asked so
                Map<Entity, List<Pair<Entity, List<String>>>> potentialJoinTables = new HashMap<>();
                if (getReverseMode().reverseJoins())
                {
                    for (Entity pkEntity : knownEntitiesByTable.values())
                    {
                        List<Pair<String, List<String>>> joins = reverseEngineer.getJoins(pkEntity);
                        for (Pair<String, List<String>> join : joins)
                        {
                            String fkTable = join.getLeft();
                            List<String> fkColumns = join.getRight();
                            Entity fkEntity = knownEntitiesByTable.get(fkTable);
                            if (fkEntity != null)
                            {
                                // define upstream attribute from fk to pk
                                declareUpstreamJoin(pkEntity, fkEntity, fkColumns);

                                // define downstream attribute from pk to fk
                                declareJoinTowardsForeignKey(pkEntity, fkEntity, fkColumns);

                                List<Pair<Entity, List<String>>> fks = potentialJoinTables.get(fkEntity);
                                if (fks == null)
                                {
                                    fks = new ArrayList<Pair<Entity, List<String>>>();
                                    potentialJoinTables.put(fkEntity, fks);
                                }
                                fks.add(Pair.of(pkEntity, fkColumns));
                            }
                        }
                    }
                    // reverse enginering of join tables
                    if (getReverseMode().reverseExtended())
                    {
                        for (Map.Entry<Entity, List<Pair<Entity, List<String>>>> entry : potentialJoinTables.entrySet())
                        {
                            Entity fkEntity = entry.getKey();
                            List<Pair<Entity, List<String>>> pks = entry.getValue();
                            // TODO - joins detection should be configurable
                            // for now:
                            // - join table must reference two different tables with distinct columns
                            // - join table name must be a snake case concatenation of both pk tables
                            if (pks.size() == 2)
                            {
                                List<String> leftFkColumns = pks.get(0).getRight();
                                List<String> rightFkColumns = pks.get(1).getRight();
                                if (Collections.disjoint(leftFkColumns, rightFkColumns))
                                {
                                    Entity leftPK = pks.get(0).getLeft();
                                    Entity rightPK = pks.get(1).getLeft();
                                    String name1 = leftPK.getName() + "_" + rightPK.getName();
                                    String name2 = rightPK.getName() + "_" + leftPK.getName();
                                    if (fkEntity.getName().equals(name1) || fkEntity.getName().equals(name2))
                                    {
                                        declareExtendedJoin(leftPK, leftFkColumns, fkEntity, rightFkColumns, rightPK);
                                        declareExtendedJoin(rightPK, rightFkColumns, fkEntity, leftFkColumns, leftPK);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            if (connection != null)
            {
                connection.leaveBusyState();
            }
        }
    }

    private void declareUpstreamJoin(Entity pkEntity, Entity fkEntity, List<String> fkColumns) throws SQLException
    {
        String upstreamAttributeName;
        if (fkColumns.size() == 1)
        {
            // take fk column name with _id suffix stripped out
            upstreamAttributeName = fkColumns.get(0).toLowerCase(Locale.ROOT);
            if (upstreamAttributeName.length() > 3 && upstreamAttributeName.endsWith("_id"))
            {
                upstreamAttributeName = upstreamAttributeName.substring(0, upstreamAttributeName.length() - 3);
            }
            // except if the resulting is an abbreviation of the target pk entity name
            if (pkEntity.getName().startsWith(upstreamAttributeName))
            {
                upstreamAttributeName = pkEntity.getName();
            }
        }
        else
        {
            upstreamAttributeName = pkEntity.getName(); // hope it's a singular
        }
        Attribute previous = fkEntity.getAttribute(upstreamAttributeName);
        if (previous != null)
        {
            getLogger().warn("explicit declaration of attribute {}.{} supersedes implicit imported key from {}", fkEntity.getName(), upstreamAttributeName, pkEntity.getName());
        }
        else
        {
            fkEntity.declareUpstreamJoin(upstreamAttributeName, pkEntity, fkColumns);
        }
    }

    private void declareJoinTowardsForeignKey(Entity pkEntity, Entity fkEntity, List<String> fkColumns) throws SQLException
    {
        String downstreamAttributeName = getIdentifiers().pluralize(fkEntity.getName());
        Attribute previous = pkEntity.getAttribute(downstreamAttributeName);
        if (previous != null)
        {
            getLogger().warn("explicit declaration of attribute {}.{} supersedes implicit exported key towards {}", pkEntity.getName(), downstreamAttributeName, fkEntity.getName());
        }
        else
        {
            pkEntity.declareDownstreamJoin(downstreamAttributeName, fkEntity, fkColumns);
        }
    }

    private void declareExtendedJoin(Entity leftEntity, List<String> leftFKCols, Entity joinEntity, List<String> rightFKCols, Entity rightEntity) throws SQLException
    {
        String joinAttributeName = getIdentifiers().pluralize(rightEntity.getName());
        Attribute previous = leftEntity.getAttribute(joinAttributeName);
        if (previous != null)
        {
            getLogger().warn("explicit declaration of attribute {}.{} supersedes implicit extended join {}", leftEntity.getName(), joinAttributeName, rightEntity.getName());
        }
        else
        {
            leftEntity.declareExtendedJoin(joinAttributeName, leftFKCols, joinEntity, rightFKCols, rightEntity);
        }
    }

    /*
     * Operations
     */

    public static Model getModel(String id)
    {
        Model ret = modelRepository.get(id);
        if (ret == null)
        {
            throw new ConfigurationException("model id not found: " + id);
        }
        return ret;
    }

    protected final String quoteIdentifier(String identifier)
    {
        return driverInfos.quoteIdentifier(identifier);
    }

    /*
     * Helper Classes
     */

    /**
     * <p>gather filters getters and setters in a subclass to ease configuration</p>
     * <p>Can be configured after initialization.</p>
     */
    public class FiltersSet
    {
        public FiltersSet()
        {
            readFilters = new ValueFilterHandler("filters.read");
            writeFilters = new ValueFilterHandler("filters.write");
        }

        public ValueFilterHandler getReadFilters()
        {
            return readFilters;
        }

        public Model setReadMapping(Map filters) throws Exception
        {
            readFilters.setMapping(filters);
            return getModel();
        }

        public ValueFilterHandler getWriteFilters()
        {
            return writeFilters;
        }

        public Model setWriteMapping(Map filters) throws Exception
        {
            writeFilters.setMapping(filters);
            return getModel();
        }

        public final Model setCryptographClass(String cryptographClass)
        {
            this.cryptographClass = cryptographClass;
            return getModel();
        }

        protected final void initialize()
        {
            if (readFilters.needsCryptograph() || writeFilters.needsCryptograph())
                try
                {
                    Cryptograph cryptograph = initCryptograph();
                    readFilters.setCryptograph(cryptograph);
                    writeFilters.setCryptograph(cryptograph);
                }
                catch (RuntimeException re)
                {
                    throw re;
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("could not initialize cryptograph", e);
                }
        }

        private final Cryptograph initCryptograph() throws Exception
        {
            if (cryptographClass == null)
            {
                throw new ConfigurationException("no cryptograph classname found in filters.cryptograph");
            }
            Class clazz = ClassUtils.getClass(cryptographClass);
            Cryptograph cryptograph = (Cryptograph)clazz.newInstance();
            String secret = getSecret();
            if (secret == null)
            {
                throw new ConfigurationException("no cryptograph secret: either definition file or database url must be provided");
            }
            cryptograph.init(getSecret());
            return cryptograph;
        }

        private final String getSecret()
        {
            return Optional.ofNullable(
                getDefinition()).map(x -> String.valueOf(x)).
                orElse(Optional.ofNullable(getDatabaseURL()).filter(x -> x.length() >= 16).
                    orElse("sixteen chars..."));
        }

        private String cryptographClass = null;
    }

    protected class UserInstancesConfig
    {

        protected Class getFactory()
        {
            return factory;
        }

        public Model setFactory(Object factory)
        {
            if (factory == null || factory instanceof Class)
            {
                this.factory = (Class)factory;
            }
            else if (factory instanceof String)
            {
                try
                {
                    this.factory = ClassUtils.getClass((String)factory);
                }
                catch (ClassNotFoundException cnfe)
                {
                    throw new ConfigurationException("cannot get instances factory", cnfe);
                }
            }
            else
            {
                throw new ConfigurationException("expecting factory class or classname");
            }
            return getModel();
        }

        protected Map<String, Class> getClasses()
        {
            return classes;
        }

        public Model setClasses(Map<String, ?> classes)
        {
            this.classes = new TreeMap<String, Class>();
            try
            {
                for (Map.Entry<String, ?> entry : classes.entrySet())
                {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Class clazz = null;
                    if (value instanceof String)
                    {
                        clazz = ClassUtils.getClass((String)value);
                    }
                    else if (value instanceof Class)
                    {
                        clazz = (Class)value;
                    }
                    this.classes.put(key, clazz);
                }
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new ConfigurationException("could not build instances classes map", cnfe);
            }
            return getModel();
        }

        protected void initialize()
        {
            Set<String> classProvided = new HashSet<String>();
            if (classes != null)
            {
                for (Map.Entry<String, Class> entry : classes.entrySet())
                {
                    String key = entry.getKey();
                    final Entity entity = getEntity(key);
                    final Class clazz = entry.getValue();
                    if (entity == null)
                    {
                        throw new ConfigurationException("instance.classes." + key + ": no entity named " + key);
                    }
                    if (Instance.class.isAssignableFrom(clazz))
                    {
                        try
                        {
                            final Constructor ctor = clazz.getDeclaredConstructor(Entity.class);
                            // ctor.setAccessible(true);
                            entity.setInstanceBuilder(() ->
                            {
                                try
                                {
                                    return (Instance)ctor.newInstance(entity);
                                }
                                catch (IllegalAccessException | InstantiationException | InvocationTargetException e)
                                {
                                    throw new RuntimeException("could not create instance of class " + clazz.getName());
                                }
                            });

                        }
                        catch (NoSuchMethodException nsme)
                        {
                            throw new ConfigurationException("Class " + clazz.getName() + " must declare a public ctor taking an Entity as argument");
                        }
                    }
                    else
                    {
                        entity.setInstanceBuilder(() ->
                        {
                            try
                            {
                                Object obj = clazz.newInstance();
                                return new WrappingInstance(entity, obj);
                            }
                            catch (InstantiationException | IllegalAccessException e)
                            {
                                throw new RuntimeException("could not create instance of class " + clazz.getName());
                            }
                        }, PropertyUtils.getPropertyDescriptors(clazz));

                    }
                    classProvided.add(key);
                }
            }
            if (factory != null)
            {
                for (final Entity entity : entitiesMap.values())
                {
                    if (!classProvided.contains(entity.getName()))
                    {
                        Method method = null;
                        String capitalized = StringUtils.capitalize(entity.getName());
                        for (String prefix : factoryMethodPrefixes)
                        {
                            try
                            {
                                method = factory.getMethod(prefix + capitalized);
                            }
                            catch (NoSuchMethodException e)
                            {
                            }
                        }
                        if (method != null)
                        {
                            // let's try it
                            Object obj;
                            try
                            {
                                obj = method.invoke(null);
                            }
                            catch (IllegalAccessException | InvocationTargetException e)
                            {
                                throw new ConfigurationException("factory instance creation failed for entity " + entity.getName(), e);
                            }
                            if (obj == null)
                            {
                                throw new ConfigurationException("factory instance creation returned null for entity " + entity.getName());
                            }
                            Class clazz = obj.getClass();
                            final Method creationMethod = method;
                            if (Instance.class.isAssignableFrom(clazz))
                            {
                                entity.setInstanceBuilder(() ->
                                {
                                    try
                                    {
                                        return (Instance)creationMethod.invoke(null);
                                    }
                                    catch (IllegalAccessException | InvocationTargetException e)
                                    {
                                        throw new RuntimeException("could not create instance of class " + clazz.getName());
                                    }
                                });
                            }
                            else
                            {
                                entity.setInstanceBuilder(() ->
                                {
                                    try
                                    {
                                        return new WrappingInstance(entity, creationMethod.invoke(null));
                                    }
                                    catch (IllegalAccessException | InvocationTargetException e)
                                    {
                                        throw new RuntimeException("could not create instance of class " + clazz.getName());
                                    }
                                });

                            }
                        }
                    }
                }
            }
        }

        private Class factory = null;

        private Map<String, Class> classes = null;
    }

    private String[] factoryMethodPrefixes = { "create", "new", "get" };

    /*
     * Members
     */

    private String modelId = null;

    public enum WriteAccess { NONE, JAVA, VTL }

    private WriteAccess writeAccess = WriteAccess.JAVA;

    public enum ReverseMode
    {
        NONE, COLUMNS, TABLES, JOINS, FULL, EXTENDED;

        public boolean reverseColumns()
        {
            return ordinal() > 0;
        }

        public boolean reverseTables()
        {
            return ordinal() == 2 || ordinal() > 3;
        }

        public boolean reverseJoins()
        {
            return ordinal() > 2;
        }
        public boolean reverseExtended()
        {
            return ordinal() == 5;
        }
    }

    private ReverseMode reverseMode = ReverseMode.NONE;

    private VelocityEngine velocityEngine = null;

    private String schema = null;

    /**
     * driver properties
     */
    private DriverInfos driverInfos = new DriverInfos();

    /**
     * Entities map
     */
    private NavigableMap<String, Entity> entitiesMap = new TreeMap<>();

    /**
     * Definition file URL
     */
    private URL definition = null;

    /**
     * Data source
     */
    private transient DataSource dataSource = null;

    private String databaseURL = null;

    /**
     * Pool of connections.
     */
    private transient ConnectionPool connectionPool = null;

    /**
     * Max connections.
     */
    private int maxConnections = 50; // applies to connectionPool and transactionConnectionPool

    /**
     * Pool of connections for transactions.
     */
    private transient ConnectionPool transactionConnectionPool = null;

    /**
     * Pool of prepared statements.
     */
    private transient StatementPool statementPool = null;

    private transient Credentials credentials = new Credentials();

    /**
     * Identifiers mapper
     */
    private Identifiers identifiers = new Identifiers();

    /**
     * Value filters
     */

    protected ValueFilterHandler readFilters = null;

    protected ValueFilterHandler writeFilters = null;

    private FiltersSet filters = new FiltersSet();

    private UserInstancesConfig userInstancesConfig = new UserInstancesConfig();

    /**
     * Model repository
     */
    private static Map<String, Model> modelRepository = new HashMap<>();
}
