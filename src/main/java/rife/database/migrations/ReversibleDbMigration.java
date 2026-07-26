/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import rife.database.Datasource;

import java.util.List;

/**
 * Provides a single declarative database migration that can be rolled back.
 *
 * <p>Extending this class instead of {@link DbMigration} is what makes a
 * migration reversible. The reverse steps are declared in {@link #down}
 * with the same {@code add} methods that {@link #up} uses, so that
 * {@link DbMigrations} can execute them when the migration is rolled back.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbMigration
 * @see DbMigrations
 * @since 1.10
 */
public abstract class ReversibleDbMigration extends DbMigration {
    /**
     * Declares the steps that reverse this migration.
     * <p>This method will be called while the steps of a rollback are being
     * collected; you declare each of them by calling one of the {@code add}
     * methods, typically undoing what {@link #up} declares in the opposite
     * order.
     *
     * @see #up()
     * @since 1.10
     */
    public abstract void down();

    List<Object> collectDownSteps(Datasource datasource) {
        return collectSteps(datasource, this::down);
    }
}
