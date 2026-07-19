/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import rife.database.Datasource;
import rife.database.queries.AlterTable;
import rife.database.queries.CreateIndex;
import rife.database.queries.CreateSequence;
import rife.database.queries.CreateTable;
import rife.database.queries.CreateView;
import rife.database.queries.Delete;
import rife.database.queries.DropIndex;
import rife.database.queries.DropSequence;
import rife.database.queries.DropTable;
import rife.database.queries.DropView;
import rife.database.queries.Insert;
import rife.database.queries.Query;
import rife.database.queries.Truncate;
import rife.database.queries.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * A single declarative database migration.
 * <p>
 * A migration declares an ordered series of steps in {@link #up} and
 * optionally the reverse series in {@link #down}, by calling one of the
 * {@code add} methods. The steps are query builders, literal SQL
 * statements, or {@link DbMigrationAction} instances for data transforms
 * that need Java logic. A migration never executes anything itself, the
 * steps are collected and executed by {@link DbMigrations}.
 * <p>
 * The protected factory methods create query builders that are bound to
 * the datasource that is being migrated. Creating a builder doesn't add
 * a step, every step is explicitly added with {@code add}.
 * <p>
 * A migration that doesn't implement {@link #down} is irreversible,
 * rolling it back raises
 * {@link rife.database.migrations.exceptions.IrreversibleMigrationException}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public abstract class DbMigration {
    private Datasource datasource_ = null;
    private List<Object> steps_ = null;

    /**
     * Declares the steps that perform this migration.
     *
     * @since 1.10
     */
    public abstract void up();

    /**
     * Declares the steps that reverse this migration.
     * <p>
     * A migration that doesn't override this method is irreversible.
     *
     * @since 1.10
     */
    public void down() {
    }

    /**
     * Indicates whether this migration can be rolled back.
     *
     * @return {@code true} when the migration implements {@link #down};
     * or {@code false} otherwise
     * @since 1.10
     */
    public boolean isReversible() {
        try {
            return getClass().getMethod("down").getDeclaringClass() != DbMigration.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Adds a query builder step to this migration.
     *
     * @param query the query to execute as this step
     * @return this migration
     * @since 1.10
     */
    protected DbMigration add(Query query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        steps().add(query);
        return this;
    }

    /**
     * Adds a literal SQL step to this migration.
     *
     * @param sql the SQL statement to execute as this step
     * @return this migration
     * @since 1.10
     */
    protected DbMigration add(String sql) {
        if (null == sql) throw new IllegalArgumentException("sql can't be null.");
        if (sql.isEmpty()) throw new IllegalArgumentException("sql can't be empty.");

        steps().add(sql);
        return this;
    }

    /**
     * Adds a Java logic step to this migration.
     *
     * @param action the action to execute as this step
     * @return this migration
     * @since 1.10
     */
    protected DbMigration add(DbMigrationAction action) {
        if (null == action) throw new IllegalArgumentException("action can't be null.");

        steps().add(action);
        return this;
    }

    /**
     * Retrieves the datasource that this migration is being collected for.
     *
     * @return the active datasource
     * @since 1.10
     */
    protected Datasource datasource() {
        if (null == datasource_) {
            throw new IllegalStateException("The datasource is only available while the migration steps are being declared.");
        }
        return datasource_;
    }

    /**
     * Creates a query builder for creating a table,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to create
     * @return the query builder
     * @since 1.10
     */
    protected CreateTable createTable(String table) {
        return new CreateTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for altering a table,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to alter
     * @return the query builder
     * @since 1.10
     */
    protected AlterTable alterTable(String table) {
        return new AlterTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for dropping a table,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to drop
     * @return the query builder
     * @since 1.10
     */
    protected DropTable dropTable(String table) {
        return new DropTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for creating an index,
     * bound to the datasource of this migration.
     *
     * @param name the name of the index to create
     * @return the query builder
     * @since 1.10
     */
    protected CreateIndex createIndex(String name) {
        return new CreateIndex(datasource()).name(name);
    }

    /**
     * Creates a query builder for dropping an index,
     * bound to the datasource of this migration.
     *
     * @param name the name of the index to drop
     * @return the query builder
     * @since 1.10
     */
    protected DropIndex dropIndex(String name) {
        return new DropIndex(datasource()).name(name);
    }

    /**
     * Creates a query builder for creating a view,
     * bound to the datasource of this migration.
     *
     * @param view the name of the view to create
     * @return the query builder
     * @since 1.10
     */
    protected CreateView createView(String view) {
        return new CreateView(datasource()).view(view);
    }

    /**
     * Creates a query builder for dropping a view,
     * bound to the datasource of this migration.
     *
     * @param view the name of the view to drop
     * @return the query builder
     * @since 1.10
     */
    protected DropView dropView(String view) {
        return new DropView(datasource()).view(view);
    }

    /**
     * Creates a query builder for creating a sequence,
     * bound to the datasource of this migration.
     *
     * @param name the name of the sequence to create
     * @return the query builder
     * @since 1.10
     */
    protected CreateSequence createSequence(String name) {
        return new CreateSequence(datasource()).name(name);
    }

    /**
     * Creates a query builder for dropping a sequence,
     * bound to the datasource of this migration.
     *
     * @param name the name of the sequence to drop
     * @return the query builder
     * @since 1.10
     */
    protected DropSequence dropSequence(String name) {
        return new DropSequence(datasource()).name(name);
    }

    /**
     * Creates a query builder for truncating a table,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to truncate
     * @return the query builder
     * @since 1.10
     */
    protected Truncate truncate(String table) {
        return new Truncate(datasource()).table(table);
    }

    /**
     * Creates a query builder for inserting data,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to insert into
     * @return the query builder
     * @since 1.10
     */
    protected Insert insert(String table) {
        return new Insert(datasource()).into(table);
    }

    /**
     * Creates a query builder for updating data,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to update
     * @return the query builder
     * @since 1.10
     */
    protected Update update(String table) {
        return new Update(datasource()).table(table);
    }

    /**
     * Creates a query builder for deleting data,
     * bound to the datasource of this migration.
     *
     * @param table the name of the table to delete from
     * @return the query builder
     * @since 1.10
     */
    protected Delete delete(String table) {
        return new Delete(datasource()).from(table);
    }

    private List<Object> steps() {
        if (null == steps_) {
            throw new IllegalStateException("Steps can only be added while up or down is being collected.");
        }
        return steps_;
    }

    List<Object> collectUpSteps(Datasource datasource) {
        return collectSteps(datasource, true);
    }

    List<Object> collectDownSteps(Datasource datasource) {
        return collectSteps(datasource, false);
    }

    private List<Object> collectSteps(Datasource datasource, boolean up) {
        datasource_ = datasource;
        steps_ = new ArrayList<>();
        try {
            if (up) {
                up();
            } else {
                down();
            }
            return steps_;
        } finally {
            datasource_ = null;
            steps_ = null;
        }
    }
}
