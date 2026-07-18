/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;

public abstract class TestCreateView extends TestQuery {
    protected CreateTable setupTable(Datasource datasource) {
        return new CreateTable(datasource)
            .table("tablename")
            .column("propint", int.class, CreateTable.NOTNULL)
            .column("propstring", String.class, 50);
    }

    protected void execute(CreateTable setup, DropView cleanup, CreateView... views) {
        var manager = new DbQueryManager(cleanup.getDatasource());
        manager.executeUpdate(setup);
        try {
            for (var view : views) {
                manager.executeUpdate(view);
            }
            manager.executeUpdate(cleanup);
        } finally {
            try {
                manager.executeUpdate(new DropTable(cleanup.getDatasource()).table(setup.getTable()));
            } catch (DatabaseException e) {
                // clean up as much as possible
            }
        }
    }
}
