/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.ColumnsRequiredException;
import rife.database.exceptions.IndexNameRequiredException;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateIndexMariadb extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testInstantiationMariadb() {
        var query = new CreateIndex(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testIncompleteMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test");
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
        query.table("tablename");
        try {
            query.getSql();
            fail();
        } catch (ColumnsRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
        query.column("propstring");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testClearMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateUniqueMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateMultipleColumnsMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateBeanColumnMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCloneMariadb() {
        var query = new CreateIndex(MARIADB);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
        cloned.column("propint");
        assertNotEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropInstantiationMariadb() {
        var query = new DropIndex(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropMariadb() {
        var query = new DropIndex(MARIADB);
        query.name("idx_test").table("tablename");
        assertEquals("DROP INDEX idx_test ON tablename", query.getSql());
        var index = new CreateIndex(MARIADB);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(MARIADB), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropCloneMariadb() {
        var query = new DropIndex(MARIADB);
        query.name("idx_test").table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropRequiresTableMariadb() {
        var query = new DropIndex(MARIADB);
        query.name("idx_test");
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }
}
