/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TestTruncate extends TestQuery {
    protected CreateTable setupTable(Datasource datasource) {
        return new CreateTable(datasource)
            .table("tablename")
            .column("propint", int.class, CreateTable.NOTNULL);
    }

    protected void execute(CreateTable setup, Truncate query) {
        var datasource = query.getDatasource();
        var manager = new DbQueryManager(datasource);
        manager.executeUpdate(setup);
        try {
            manager.executeUpdate(new Insert(datasource).into("tablename").field("propint", 1));
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource).from("tablename").field("count(*)")));
            manager.executeUpdate(query);
            assertEquals(0, manager.executeGetFirstInt(new Select(datasource).from("tablename").field("count(*)")));
        } finally {
            try {
                manager.executeUpdate(new DropTable(datasource).table(setup.getTable()));
            } catch (DatabaseException e) {
                // clean up as much as possible
            }
        }
    }
}
