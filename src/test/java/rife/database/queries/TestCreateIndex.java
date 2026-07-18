/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;

public abstract class TestCreateIndex extends TestQuery {
    protected CreateTable setupTable(Datasource datasource) {
        return new CreateTable(datasource)
            .table("tablename")
            .column("propint", int.class, CreateTable.NOTNULL)
            .column("propstring", String.class, 50);
    }

    protected void execute(CreateTable setup, CreateIndex query) {
        var manager = new DbQueryManager(query.getDatasource());
        manager.executeUpdate(setup);
        try {
            manager.executeUpdate(query);
        } finally {
            try {
                manager.executeUpdate(new DropTable(query.getDatasource()).table(setup.getTable()));
            } catch (DatabaseException e) {
                // clean up as much as possible
            }
        }
    }

    protected void execute(CreateTable setup, CreateIndex index, DropIndex query) {
        var manager = new DbQueryManager(query.getDatasource());
        manager.executeUpdate(setup);
        try {
            manager.executeUpdate(index);
            manager.executeUpdate(query);
        } finally {
            try {
                manager.executeUpdate(new DropTable(query.getDatasource()).table(setup.getTable()));
            } catch (DatabaseException e) {
                // clean up as much as possible
            }
        }
    }
}
