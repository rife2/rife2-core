/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.exceptions.ViewDefinitionRequiredException;
import rife.database.exceptions.ViewNameRequiredException;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object representation of a SQL "CREATE VIEW" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * <p>The view definition is provided either as a {@link Select} query, which
 * makes the view construction as database-independent as the query builders,
 * or as a literal SQL string. A select query with parameters can't define a
 * view since views don't have placeholders.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CreateView extends AbstractQuery implements Cloneable {
    private String view_ = null;
    private boolean replace_ = false;
    private List<String> columns_ = null;
    private Select select_ = null;
    private String definition_ = null;

    public CreateView(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        view_ = null;
        replace_ = false;
        columns_ = new ArrayList<>();
        select_ = null;
        definition_ = null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getView() {
        return view_;
    }

    public boolean isReplace() {
        return replace_;
    }

    public List<String> getColumns() {
        return columns_;
    }

    public Select getSelect() {
        return select_;
    }

    public String getDefinition() {
        return definition_;
    }

    public CreateView view(String view) {
        if (null == view) throw new IllegalArgumentException("view can't be null.");
        if (view.isEmpty()) throw new IllegalArgumentException("view can't be empty.");

        view_ = view;
        clearGenerated();

        return this;
    }

    public CreateView replace(boolean replace) {
        replace_ = replace;
        clearGenerated();

        return this;
    }

    public CreateView columns(String... columns) {
        if (null == columns) throw new IllegalArgumentException("columns can't be null.");
        if (0 == columns.length) throw new IllegalArgumentException("columns can't be empty.");

        columns_.addAll(Arrays.asList(columns));
        clearGenerated();

        return this;
    }

    public CreateView as(Select select) {
        if (null == select) throw new IllegalArgumentException("select can't be null.");

        select_ = select;
        definition_ = null;
        clearGenerated();

        return this;
    }

    public CreateView as(String definition) {
        if (null == definition) throw new IllegalArgumentException("definition can't be null.");
        if (definition.isEmpty()) throw new IllegalArgumentException("definition can't be empty.");

        definition_ = definition;
        select_ = null;
        clearGenerated();

        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == view_) {
                throw new ViewNameRequiredException("CreateView");
            } else if (null == select_ && null == definition_) {
                throw new ViewDefinitionRequiredException("CreateView");
            } else {
                var definition = definition_;
                if (select_ != null) {
                    // a view can't contain placeholders, a parametrized
                    // select query can never define one
                    if (select_.getParameters() != null &&
                        !select_.getParameters().getOrderedNames().isEmpty()) {
                        throw new DbQueryException("A view can't be defined by a select query with parameters.");
                    }
                    definition = select_.getSql();
                }

                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".create_view");

                if (replace_) {
                    var block = template.getBlock("REPLACE");
                    if (block.isEmpty()) {
                        throw new UnsupportedSqlFeatureException("OR REPLACE", datasource_.getAliasedDriver());
                    }
                    template.setValue("REPLACE", block);
                }

                if (!columns_.isEmpty()) {
                    template.setValue("COLUMNS", StringUtils.join(columns_, template.getBlock("SEPARATOR")));
                    template.setValue("COLUMNS_PART", template.getBlock("COLUMNS_PART"));
                }

                template.setValue("NAME", view_);
                template.setValue("DEFINITION", definition);

                sql_ = template.getBlock("QUERY");
                if (sql_.isEmpty()) {
                    throw new UnsupportedSqlFeatureException("CREATE VIEW", datasource_.getAliasedDriver());
                }

                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    public CreateView clone() {
        var new_instance = (CreateView) super.clone();
        if (new_instance != null) {
            if (columns_ != null) {
                new_instance.columns_ = new ArrayList<>(columns_);
            }
            if (select_ != null) {
                new_instance.select_ = select_.clone();
            }
        }

        return new_instance;
    }
}
