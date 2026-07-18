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

import java.util.ArrayList;
import java.util.List;

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
    private List<String> tables_ = null;

    public Truncate(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        tables_ = new ArrayList<>();
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public List<String> getTables() {
        return tables_;
    }

    public Truncate table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (table.isEmpty()) throw new IllegalArgumentException("table can't be empty.");

        tables_.add(table);
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (tables_.isEmpty()) {
                throw new TableNameRequiredException("Truncate");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".truncate");

                if (1 == tables_.size()) {
                    template.setValue("EXPRESSION", tables_.get(0));
                } else {
                    if (template.hasValueId("TABLES")) {
                        template.setValue("TABLES", StringUtils.join(tables_, template.getBlock("SEPARATOR")));
                    }
                    var block = template.getBlock("TABLES");
                    if (block.isEmpty()) {
                        throw new UnsupportedSqlFeatureException("MULTIPLE TABLE TRUNCATE", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", block);
                }

                sql_ = template.getBlock("QUERY");
                if (template.hasValueId("TABLES")) {
                    template.removeValue("TABLES");
                }
                template.removeValue("EXPRESSION");

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public Truncate clone() {
        var new_instance = (Truncate) super.clone();
        if (new_instance != null &&
            tables_ != null) {
            new_instance.tables_ = new ArrayList<>(tables_);
        }

        return new_instance;
    }
}
