/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.IndexNameRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a SQL "DROP INDEX" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * <p>Some databases, like MySQL, drop an index in the context of a table.
 * Providing the table makes the query portable to those databases, it's
 * simply not emitted for the databases that drop indexes by name alone.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class DropIndex extends AbstractQuery implements Cloneable {
    private List<String> names_ = null;
    private String table_ = null;

    public DropIndex(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        names_ = new ArrayList<>();
        table_ = null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public List<String> getNames() {
        return names_;
    }

    public String getTable() {
        return table_;
    }

    public DropIndex name(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        names_.add(name);
        clearGenerated();

        return this;
    }

    public DropIndex table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (table.isEmpty()) throw new IllegalArgumentException("table can't be empty.");

        table_ = table;
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (names_.isEmpty()) {
                throw new IndexNameRequiredException("DropIndex");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".drop_index");

                // some databases drop an index in the context of its table,
                // there the table is required
                if (template.hasValueId("TABLE")) {
                    if (null == table_) {
                        throw new TableNameRequiredException("DropIndex");
                    }
                    template.setValue("TABLE", table_);
                }

                if (1 == names_.size()) {
                    template.setValue("EXPRESSION", names_.get(0));
                } else {
                    if (template.hasValueId("NAMES")) {
                        template.setValue("NAMES", StringUtils.join(names_, template.getBlock("SEPARATOR")));
                    }
                    var block = template.getBlock("NAMES");
                    if (block.isEmpty()) {
                        throw new UnsupportedSqlFeatureException("MULTIPLE INDEX DROP", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", block);
                }

                sql_ = template.getBlock("QUERY");
                if (template.hasValueId("NAMES")) {
                    template.removeValue("NAMES");
                }
                template.removeValue("EXPRESSION");

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public DropIndex clone() {
        var new_instance = (DropIndex) super.clone();
        if (new_instance != null &&
            names_ != null) {
            new_instance.names_ = new ArrayList<>(names_);
        }

        return new_instance;
    }
}
