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
 * Provides a single declarative database migration.
 *
 * <p>A migration declares an ordered series of steps in {@link #up} by
 * calling one of the {@code add} methods. The steps are query builders,
 * literal SQL statements, or {@link DbMigrationAction} instances for the
 * data transforms that need Java logic. A migration never executes
 * anything itself, since the steps are merely collected and are afterwards
 * executed by {@link DbMigrations}.
 *
 * <p>The protected factory methods create query builders that are bound to
 * the datasource that is being migrated. Creating such a builder doesn't
 * add a step by itself, since every step has to be added explicitly with
 * one of the {@code add} methods.
 *
 * <p>A migration that extends this class directly is irreversible, which
 * means that rolling it back raises
 * {@link rife.database.migrations.exceptions.IrreversibleMigrationException}.
 * When a migration can be reversed, it extends
 * {@link ReversibleDbMigration} instead and also declares the reverse
 * steps in its {@code down} method.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ReversibleDbMigration
 * @see DbMigrationAction
 * @see DbMigrations
 * @since 1.10
 */
public abstract class DbMigration {
    private Datasource datasource_ = null;
    private List<Object> steps_ = null;

    /**
     * Declares the steps that perform this migration.
     * <p>This method will be called while the steps of the migration are
     * being collected; you declare each of them by calling one of the
     * {@code add} methods, in the order in which they have to be executed.
     *
     * @see #add(Query)
     * @see #add(String)
     * @see #add(DbMigrationAction)
     * @since 1.10
     */
    public abstract void up();


    /**
     * Adds a query builder step to this migration.
     * <p>This makes it possible to declare the step with the
     * object-oriented query builders so that the SQL that is eventually
     * executed stays database-independent.
     *
     * @param query the query to execute as this step
     * @return this migration
     * @see #add(String)
     * @see #add(DbMigrationAction)
     * @since 1.10
     */
    protected DbMigration add(Query query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        steps().add(query);
        return this;
    }

    /**
     * Adds a literal SQL step to this migration.
     * <p>This makes it possible to execute statements that the query
     * builders don't cover, at the cost of tying the migration to the SQL
     * dialect that you're writing it in.
     *
     * @param sql the SQL statement to execute as this step
     * @return this migration
     * @see #add(Query)
     * @see #add(DbMigrationAction)
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
     * <p>This makes it possible to perform data transforms that can't be
     * expressed as a single query. The action receives the query manager
     * of the datasource that is being migrated at the moment that the step
     * is executed.
     *
     * @param action the action to execute as this step
     * @return this migration
     * @see #add(Query)
     * @see #add(String)
     * @since 1.10
     */
    protected DbMigration add(DbMigrationAction action) {
        if (null == action) throw new IllegalArgumentException("action can't be null.");

        steps().add(action);
        return this;
    }

    /**
     * Retrieves the datasource that this migration is being collected for.
     * <p>The datasource is only available while the steps are being
     * declared, which is why you can only use it and the query builder
     * factory methods that rely on it from inside {@code up} or
     * {@code down}.
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
     * Creates a query builder for creating a table.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to create
     * @return the {@link CreateTable} query builder
     * @see #alterTable(String)
     * @see #dropTable(String)
     * @since 1.10
     */
    protected CreateTable createTable(String table) {
        return new CreateTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for altering a table.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     * Since each {@link AlterTable} query performs exactly one alteration,
     * you add one of them for every alteration that the migration needs.
     *
     * @param table the name of the table to alter
     * @return the {@link AlterTable} query builder
     * @see #createTable(String)
     * @see #dropTable(String)
     * @since 1.10
     */
    protected AlterTable alterTable(String table) {
        return new AlterTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for dropping a table.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to drop
     * @return the {@link DropTable} query builder
     * @see #createTable(String)
     * @see #alterTable(String)
     * @since 1.10
     */
    protected DropTable dropTable(String table) {
        return new DropTable(datasource()).table(table);
    }

    /**
     * Creates a query builder for creating an index.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param name the name of the index to create
     * @return the {@link CreateIndex} query builder
     * @see #dropIndex(String)
     * @since 1.10
     */
    protected CreateIndex createIndex(String name) {
        return new CreateIndex(datasource()).name(name);
    }

    /**
     * Creates a query builder for dropping an index.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param name the name of the index to drop
     * @return the {@link DropIndex} query builder
     * @see #createIndex(String)
     * @since 1.10
     */
    protected DropIndex dropIndex(String name) {
        return new DropIndex(datasource()).name(name);
    }

    /**
     * Creates a query builder for creating a view.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param view the name of the view to create
     * @return the {@link CreateView} query builder
     * @see #dropView(String)
     * @since 1.10
     */
    protected CreateView createView(String view) {
        return new CreateView(datasource()).view(view);
    }

    /**
     * Creates a query builder for dropping a view.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param view the name of the view to drop
     * @return the {@link DropView} query builder
     * @see #createView(String)
     * @since 1.10
     */
    protected DropView dropView(String view) {
        return new DropView(datasource()).view(view);
    }

    /**
     * Creates a query builder for creating a sequence.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param name the name of the sequence to create
     * @return the {@link CreateSequence} query builder
     * @see #dropSequence(String)
     * @since 1.10
     */
    protected CreateSequence createSequence(String name) {
        return new CreateSequence(datasource()).name(name);
    }

    /**
     * Creates a query builder for dropping a sequence.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param name the name of the sequence to drop
     * @return the {@link DropSequence} query builder
     * @see #createSequence(String)
     * @since 1.10
     */
    protected DropSequence dropSequence(String name) {
        return new DropSequence(datasource()).name(name);
    }

    /**
     * Creates a query builder for truncating a table.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to truncate
     * @return the {@link Truncate} query builder
     * @see #dropTable(String)
     * @see #delete(String)
     * @since 1.10
     */
    protected Truncate truncate(String table) {
        return new Truncate(datasource()).table(table);
    }

    /**
     * Creates a query builder for inserting data.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to insert into
     * @return the {@link Insert} query builder
     * @see #update(String)
     * @see #delete(String)
     * @since 1.10
     */
    protected Insert insert(String table) {
        return new Insert(datasource()).into(table);
    }

    /**
     * Creates a query builder for updating data.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to update
     * @return the {@link Update} query builder
     * @see #insert(String)
     * @see #delete(String)
     * @since 1.10
     */
    protected Update update(String table) {
        return new Update(datasource()).table(table);
    }

    /**
     * Creates a query builder for deleting data.
     * <p>The returned builder is bound to the datasource that is being
     * migrated and still has to be added as a step with {@link #add(Query)}.
     *
     * @param table the name of the table to delete from
     * @return the {@link Delete} query builder
     * @see #insert(String)
     * @see #update(String)
     * @see #truncate(String)
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
        return collectSteps(datasource, this::up);
    }

    List<Object> collectSteps(Datasource datasource, Runnable declaration) {
        datasource_ = datasource;
        steps_ = new ArrayList<>();
        try {
            declaration.run();
            return steps_;
        } finally {
            datasource_ = null;
            steps_ = null;
        }
    }
}
