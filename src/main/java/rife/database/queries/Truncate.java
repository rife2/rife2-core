/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

/**
 * Object representation of a SQL "TRUNCATE TABLE" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class Truncate extends AbstractQuery implements Cloneable {
    private String table_ = null;

    public Truncate(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        table_ = null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getTable() {
        return table_;
    }

    public Truncate table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (table.isEmpty()) throw new IllegalArgumentException("table can't be empty.");

        table_ = table;
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == table_) {
                throw new TableNameRequiredException("Truncate");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".truncate");

                template.setValue("TABLE", table_);

                sql_ = template.getBlock("QUERY");
                if (sql_.isEmpty()) {
                    throw new UnsupportedSqlFeatureException("TRUNCATE", datasource_.getAliasedDriver());
                }

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public Truncate clone() {
        return (Truncate) super.clone();
    }
}
