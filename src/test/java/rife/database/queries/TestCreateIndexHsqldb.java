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

public class TestCreateIndexHsqldb extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new CreateIndex(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testIncompleteHsqldb() {
        var query = new CreateIndex(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new CreateIndex(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateHsqldb() {
        var query = new CreateIndex(HSQLDB);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateUniqueHsqldb() {
        var query = new CreateIndex(HSQLDB);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateMultipleColumnsHsqldb() {
        var query = new CreateIndex(HSQLDB);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateBeanColumnHsqldb() {
        var query = new CreateIndex(HSQLDB);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new CreateIndex(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropInstantiationHsqldb() {
        var query = new DropIndex(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropHsqldb() {
        var query = new DropIndex(HSQLDB);
        query.name("idx_test");
        assertEquals("DROP INDEX idx_test", query.getSql());
        var index = new CreateIndex(HSQLDB);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(HSQLDB), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropCloneHsqldb() {
        var query = new DropIndex(HSQLDB);
        query.name("idx_test");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
