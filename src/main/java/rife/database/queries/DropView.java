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

import java.util.ArrayList;
import java.util.List;

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
    private List<String> views_ = null;

    public DropView(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        views_ = new ArrayList<>();
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public List<String> getViews() {
        return views_;
    }

    public DropView view(String view) {
        if (null == view) throw new IllegalArgumentException("view can't be null.");
        if (view.isEmpty()) throw new IllegalArgumentException("view can't be empty.");

        views_.add(view);
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (views_.isEmpty()) {
                throw new ViewNameRequiredException("DropView");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".drop_view");

                if (1 == views_.size()) {
                    template.setValue("EXPRESSION", views_.get(0));
                } else {
                    if (template.hasValueId("VIEWS")) {
                        template.setValue("VIEWS", StringUtils.join(views_, template.getBlock("SEPARATOR")));
                    }
                    var block = template.getBlock("VIEWS");
                    if (block.isEmpty()) {
                        throw new UnsupportedSqlFeatureException("MULTIPLE VIEW DROP", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", block);
                }

                sql_ = template.getBlock("QUERY");
                if (template.hasValueId("VIEWS")) {
                    template.removeValue("VIEWS");
                }
                template.removeValue("EXPRESSION");

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public DropView clone() {
        var new_instance = (DropView) super.clone();
        if (new_instance != null &&
            views_ != null) {
            new_instance.views_ = new ArrayList<>(views_);
        }

        return new_instance;
    }
}
