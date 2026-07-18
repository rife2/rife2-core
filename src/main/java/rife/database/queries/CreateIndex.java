/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.ColumnsRequiredException;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.IndexNameRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;
import rife.validation.ConstrainedUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object representation of a SQL "CREATE INDEX" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CreateIndex extends AbstractQuery implements Cloneable {
    private String name_ = null;
    private String table_ = null;
    private boolean unique_ = false;
    private List<String> columns_ = null;

    public CreateIndex(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        name_ = null;
        table_ = null;
        unique_ = false;
        columns_ = new ArrayList<>();
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getName() {
        return name_;
    }

    public String getTable() {
        return table_;
    }

    public boolean isUnique() {
        return unique_;
    }

    public List<String> getColumns() {
        return columns_;
    }

    public CreateIndex name(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        name_ = name;
        clearGenerated();

        return this;
    }

    public CreateIndex table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (table.isEmpty()) throw new IllegalArgumentException("table can't be empty.");

        table_ = table;
        clearGenerated();

        return this;
    }

    public CreateIndex unique(boolean unique) {
        unique_ = unique;
        clearGenerated();

        return this;
    }

    public CreateIndex column(String column) {
        if (null == column) throw new IllegalArgumentException("column can't be null.");
        if (column.isEmpty()) throw new IllegalArgumentException("column can't be empty.");

        columns_.add(column);
        clearGenerated();

        return this;
    }

    public CreateIndex column(Class beanClass, String propertyName) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (propertyName.isEmpty()) throw new IllegalArgumentException("propertyName can't be empty.");

        var constrained = ConstrainedUtils.getConstrainedInstance(beanClass);
        return column(QueryHelper.getColumnName(constrained, propertyName));
    }

    public CreateIndex columns(String... columns) {
        if (null == columns) throw new IllegalArgumentException("columns can't be null.");
        if (0 == columns.length) throw new IllegalArgumentException("columns can't be empty.");

        columns_.addAll(Arrays.asList(columns));
        clearGenerated();

        return this;
    }

    public CreateIndex columns(Class beanClass, String... propertyNames) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == propertyNames) throw new IllegalArgumentException("propertyNames can't be null.");
        if (0 == propertyNames.length) throw new IllegalArgumentException("propertyNames can't be empty.");

        var constrained = ConstrainedUtils.getConstrainedInstance(beanClass);
        for (var property_name : propertyNames) {
            column(QueryHelper.getColumnName(constrained, property_name));
        }

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == name_) {
                throw new IndexNameRequiredException("CreateIndex");
            } else if (null == table_) {
                throw new TableNameRequiredException("CreateIndex");
            } else if (columns_.isEmpty()) {
                throw new ColumnsRequiredException("CreateIndex");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".create_index");

                if (unique_) {
                    var block = template.getBlock("UNIQUE");
                    if (block.isEmpty()) {
                        throw new UnsupportedSqlFeatureException("UNIQUE", datasource_.getAliasedDriver());
                    }
                    template.setValue("UNIQUE", block);
                }

                template.setValue("NAME", name_);
                template.setValue("TABLE", table_);
                template.setValue("COLUMNS", StringUtils.join(columns_, template.getBlock("SEPARATOR")));

                sql_ = template.getBlock("QUERY");
                if (sql_.isEmpty()) {
                    throw new UnsupportedSqlFeatureException("CREATE INDEX", datasource_.getAliasedDriver());
                }

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public CreateIndex clone() {
        var new_instance = (CreateIndex) super.clone();
        if (new_instance != null &&
            columns_ != null) {
            new_instance.columns_ = new ArrayList<>(columns_);
        }

        return new_instance;
    }
}
