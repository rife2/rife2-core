/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import rife.database.DbQueryManager;

/**
 * Provides a migration step that executes Java logic, for instance to
 * transform data in ways that can't be expressed as a single query.
 *
 * <p>Actions are declared in a migration with
 * {@link DbMigration#add(DbMigrationAction)} and execute in sequence with
 * the other steps. They run when the migration is executed, not when it is
 * declared, which means that you have to use the query manager that is
 * passed in since {@code DbMigration.datasource()} isn't available anymore
 * at that point.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbMigration
 * @since 1.10
 */
@FunctionalInterface
public interface DbMigrationAction {
    /**
     * Executes this migration step.
     * <p>This method will be called in the order in which the step was
     * declared, inside the transaction of the migration that it belongs to.
     *
     * @param manager the query manager of the datasource that is being
     *                migrated
     * @throws Exception when an error occurred during the execution of
     *                   the step
     * @since 1.10
     */
    void execute(DbQueryManager manager) throws Exception;
}
