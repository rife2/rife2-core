/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.exceptions.ViewNameRequiredException;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

/**
 * Object representation of a SQL "DROP VIEW" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class DropView extends AbstractQuery implements Cloneable {
    private String view_ = null;

    public DropView(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        view_ = null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getView() {
        return view_;
    }

    public DropView view(String view) {
        if (null == view) throw new IllegalArgumentException("view can't be null.");
        if (view.isEmpty()) throw new IllegalArgumentException("view can't be empty.");

        view_ = view;
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == view_) {
                throw new ViewNameRequiredException("DropView");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".drop_view");

                template.setValue("NAME", view_);

                sql_ = template.getBlock("QUERY");
                if (sql_.isEmpty()) {
                    throw new UnsupportedSqlFeatureException("DROP VIEW", datasource_.getAliasedDriver());
                }

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public DropView clone() {
        return (DropView) super.clone();
    }
}
