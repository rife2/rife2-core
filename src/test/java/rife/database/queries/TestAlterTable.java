/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public abstract class TestAlterTable extends TestQuery {
    public static class MappedBean extends MetaData {
        private String firstName_;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("firstName").columnName("first_name").precision(50).notNull(true));
        }

        public void setFirstName(String firstName) {
            firstName_ = firstName;
        }

        public String getFirstName() {
            return firstName_;
        }
    }

    protected CreateTable setupTable(Datasource datasource) {
        return new CreateTable(datasource)
            .table("tablename")
            .column("propint", int.class, CreateTable.NOTNULL);
    }

    protected CreateTable setupTableWithString(Datasource datasource) {
        return setupTable(datasource)
            .column("propstring", String.class, 50);
    }

    protected CreateTable setupForeignTable(Datasource datasource) {
        return new CreateTable(datasource)
            .table("foreigntable")
            .column("foreignint", int.class, CreateTable.NOTNULL)
            .primaryKey("foreignint");
    }

    protected void execute(CreateTable setup, AlterTable query) {
        execute(setup, query, null, setup.getTable());
    }

    protected void execute(CreateTable setup, AlterTable query, CreateTable foreignSetup, String dropTable) {
        var manager = new DbQueryManager(query.getDatasource());
        if (foreignSetup != null) {
            manager.executeUpdate(foreignSetup);
        }
        manager.executeUpdate(setup);
        try {
            manager.executeUpdate(query);
        } finally {
            try {
                manager.executeUpdate(new DropTable(query.getDatasource()).table(dropTable));
            } catch (DatabaseException e) {
                // clean up as much as possible
            }
            if (foreignSetup != null) {
                try {
                    manager.executeUpdate(new DropTable(query.getDatasource()).table(foreignSetup.getTable()));
                } catch (DatabaseException e) {
                    // clean up as much as possible
                }
            }
        }
    }
}
