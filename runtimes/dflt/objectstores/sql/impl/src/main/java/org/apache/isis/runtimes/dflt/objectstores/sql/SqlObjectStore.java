/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;

public final class SqlObjectStore implements ObjectStore {
    private static final String TABLE_NAME = "isis_admin_services";
    private static final String ID_COLUMN = "id";
    private static final String PRIMARYKEY_COLUMN = "pk_id";
    public static final String BASE_NAME = "isis.persistor.sql";
    private static final Logger LOG = Logger.getLogger(SqlObjectStore.class);
    private DatabaseConnectorPool connectionPool;
    private ObjectMappingLookup objectMappingLookup;
    private boolean isInitialized;

    @Override
    public void abortTransaction() {
    }

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        return new CreateObjectCommand() {
            @Override
            public void execute(final PersistenceCommandContext context) {
                DatabaseConnector connection = ((SqlExecutionContext) context).getConnection();
                LOG.debug("  create object " + object);
                ObjectMapping mapping = objectMappingLookup.getMapping(object, connection);
                mapping.createObject(connection, object);
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return new DestroyObjectCommand() {
            @Override
            public void execute(final PersistenceCommandContext context) {
                DatabaseConnector connection = ((SqlExecutionContext) context).getConnection();
                LOG.debug("  destroy object " + object);
                ObjectMapping mapping = objectMappingLookup.getMapping(object, connection);
                mapping.destroyObject(connection, object);
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "DestroyObjectCommand [object=" + object + "]";
            }
        };
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        return new SaveObjectCommand() {
            @Override
            public void execute(final PersistenceCommandContext context) {
                DatabaseConnector connection = ((SqlExecutionContext) context).getConnection();
                LOG.debug("  save object " + object);
                if (object.getSpecification().isCollectionOrIsAggregated()) {
                    /*
                     * ObjectAdapter parent = object.getSpecification().getAggregate(object); LOG.debug(
                     * "change to internal collection being persisted through parent" );
                     * 
                     * // TODO a better plan would be ask the mapper to save the collection // - saveCollection(parent,
                     * collection) mapperLookup.getMapper(connection, parent).save(connection, parent);
                     * connectionPool.release(connection);
                     */
                    throw new NotYetImplementedException(object.toString());
                } else {
                    ObjectMapping mapping = objectMappingLookup.getMapping(object, connection);
                    mapping.save(connection, object);
                    connectionPool.release(connection);
                }
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "SaveObjectCommand [object=" + object + "]";
            }

        };
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("initialised", isInitialized);
        debug.appendln("connection pool", connectionPool);
        debug.appendln("Database:");
        debug.indent();
        connectionPool.debug(debug);
        debug.unindent();
        objectMappingLookup.debugData(debug);
    }

    @Override
    public String debugTitle() {
        return null;
    }

    @Override
    public void endTransaction() {
    }

    @Override
    public void execute(final List<PersistenceCommand> commands) {
        DatabaseConnector connector = connectionPool.acquire();
        connector.begin();

        IsisTransactionManager transactionManager = IsisContext.getTransactionManager();
        MessageBroker messageBroker = IsisContext.getMessageBroker();
        UpdateNotifier updateNotifier = IsisContext.getUpdateNotifier();
        SqlExecutionContext context =
            new SqlExecutionContext(connector, transactionManager, messageBroker, updateNotifier);
        try {
            for (PersistenceCommand command : commands) {
                command.execute(context);
            }
            connector.commit();
        } catch (IsisException e) {
            LOG.warn("Failure during execution", e);
            connector.rollback();
            throw e;
        } finally {
            connectionPool.release(connector);
        }
    }

    public boolean flush(final PersistenceCommand[] commands) {
        return false;
    }

    @Override
    public ObjectAdapter[] getInstances(final PersistenceQuery query) {
        if (query instanceof PersistenceQueryFindByTitle) {
            return findByTitle((PersistenceQueryFindByTitle) query);
        } else if (query instanceof PersistenceQueryFindAllInstances) {
            return getAllInstances((PersistenceQueryFindAllInstances) query);
        } else if (query instanceof PersistenceQueryFindByPattern) {
            return findByPattern((PersistenceQueryFindByPattern) query);
        } else {
            throw new SqlObjectStoreException("Query type not supported: " + query);
        }
    }

    private ObjectAdapter[] findByPattern(final PersistenceQueryFindByPattern query) {
        ObjectSpecification specification = query.getSpecification();
        DatabaseConnector connector = connectionPool.acquire();
        ObjectMapping mapper = objectMappingLookup.getMapping(specification, connector);
        ObjectAdapter instances[] = mapper.getInstances(connector, specification, query);
        connectionPool.release(connector);
        return instances;
    }

    private ObjectAdapter[] getAllInstances(final PersistenceQueryFindAllInstances criteria) {
        ObjectSpecification spec = criteria.getSpecification();
        return allInstances(spec);
    }

    // TODO: allInstances of should find all derived types, too.
    private ObjectAdapter[] allInstances(ObjectSpecification spec) {
        DatabaseConnector connector = connectionPool.acquire();
        ObjectMapping mapper = objectMappingLookup.getMapping(spec, connector);
        ObjectAdapter[] instances = mapper.getInstances(connector, spec);
        Vector<ObjectAdapter> matchingInstances = new Vector<ObjectAdapter>();
        for (int i = 0; i < instances.length; i++) {
            matchingInstances.addElement(instances[i]);
        }
        connectionPool.release(connector);
        ObjectAdapter[] instanceArray = new ObjectAdapter[matchingInstances.size()];
        matchingInstances.copyInto(instanceArray);
        return instanceArray;
    }

    private ObjectAdapter[] findByTitle(final PersistenceQueryFindByTitle criteria) {
        ObjectSpecification spec = criteria.getSpecification();
        DatabaseConnector connector = connectionPool.acquire();
        ObjectMapping mapper = objectMappingLookup.getMapping(spec, connector);

        ObjectAdapter[] instances = mapper.getInstances(connector, spec, criteria.getTitle());
        connectionPool.release(connector);
        return instances;
    }

    @Override
    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) {
        DatabaseConnector connection = connectionPool.acquire();
        ObjectMapping mapper = objectMappingLookup.getMapping(hint, connection);
        ObjectAdapter object = mapper.getObject(connection, oid, hint);
        connectionPool.release(connection);
        return object;
    }

    @Override
    public Oid getOidForService(String name) {
        DatabaseConnector connector = connectionPool.acquire();

        StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append(Sql.identifier(PRIMARYKEY_COLUMN));
        sql.append(" from ");
        sql.append(Sql.tableIdentifier(TABLE_NAME));
        sql.append(" where ");
        sql.append(Sql.identifier(ID_COLUMN));
        sql.append(" = ?");
        connector.addToQueryValues(name);

        Results results = connector.select(sql.toString());
        if (results.next()) {
            int key = results.getInt(PRIMARYKEY_COLUMN);
            connectionPool.release(connector);
            return SqlOid.createPersistent(name, new IntegerPrimaryKey(key));
        } else {
            connectionPool.release(connector);
            return null;
        }
    }

    @Override
    public boolean hasInstances(final ObjectSpecification spec) {
        DatabaseConnector connection = connectionPool.acquire();
        ObjectMapping mapper = objectMappingLookup.getMapping(spec, connection);
        boolean hasInstances = mapper.hasInstances(connection, spec);
        connectionPool.release(connection);
        return hasInstances;
    }

    @Override
    public boolean isFixturesInstalled() {
        return isInitialized;
    }

    @Override
    public void open() {
        Sql.setMetaData(connectionPool.acquire().getMetaData());

        DebugBuilder debug = new DebugString();
        connectionPool.debug(debug);
        LOG.info("Database: " + debug);

        objectMappingLookup.init();

        DatabaseConnector connector = connectionPool.acquire();
        isInitialized = connector.hasTable(Sql.tableIdentifier(TABLE_NAME));
        if (!isInitialized) {

            Defaults.initialise();

            StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(Sql.tableIdentifier(TABLE_NAME));
            sql.append(" (");
            sql.append(Sql.identifier(PRIMARYKEY_COLUMN));
            sql.append(" int, ");
            sql.append(Sql.identifier(ID_COLUMN));
            sql.append(" varchar(255)");
            sql.append(")");
            connector.update(sql.toString());
        }
    }

    @Override
    public String name() {
        return "SQL Object Store";
    }

    @Override
    public void registerService(final String name, final Oid oid) {
        DatabaseConnector connector = connectionPool.acquire();

        StringBuffer sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(Sql.tableIdentifier(TABLE_NAME));
        sql.append(" (");
        sql.append(Sql.identifier(PRIMARYKEY_COLUMN));
        sql.append(", ");
        sql.append(Sql.identifier(ID_COLUMN));
        sql.append(") values (?,?)");

        connector.addToQueryValues(((SqlOid) oid).getPrimaryKey().naturalValue());
        connector.addToQueryValues(name);

        connector.insert(sql.toString());
        connectionPool.release(connector);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        if (field.isOneToManyAssociation()) {
            DatabaseConnector connection = connectionPool.acquire();
            ObjectSpecification spec = object.getSpecification();
            ObjectMapping mapper = objectMappingLookup.getMapping(spec, connection);
            mapper.resolveCollection(connection, object, field);
            connectionPool.release(connection);
        } else {
            resolveImmediately(field.get(object));
        }
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
        DatabaseConnector connector = connectionPool.acquire();
        ObjectMapping mapping = objectMappingLookup.getMapping(object, connector);
        mapping.resolve(connector, object);
        connectionPool.release(connector);
    }

    public void setConnectionPool(final DatabaseConnectorPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void setMapperLookup(final ObjectMappingLookup mapperLookup) {
        this.objectMappingLookup = mapperLookup;
    }

    @Override
    public void close() {
        objectMappingLookup.shutdown();
        connectionPool.shutdown();
    }

    @Override
    public void startTransaction() {
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

}
