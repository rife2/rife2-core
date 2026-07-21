/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import rife.database.Datasource;

import java.util.List;

/**
 * A single declarative database migration that can be rolled back.
 * <p>
 * Extending this class instead of {@link DbMigration} is what makes a
 * migration reversible: the reverse steps are declared in {@link #down}
 * with the same {@code add} methods that {@link #up} uses, and
 * {@link DbMigrations} executes them when the migration is rolled back.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public abstract class ReversibleDbMigration extends DbMigration {
    /**
     * Declares the steps that reverse this migration.
     *
     * @since 1.10
     */
    public abstract void down();

    List<Object> collectDownSteps(Datasource datasource) {
        return collectSteps(datasource, this::down);
    }
}
