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

public class TestCreateIndexMysql extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        var query = new CreateIndex(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testIncompleteMysql() {
        var query = new CreateIndex(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testClearMysql() {
        var query = new CreateIndex(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateMysql() {
        var query = new CreateIndex(MYSQL);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateUniqueMysql() {
        var query = new CreateIndex(MYSQL);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateMultipleColumnsMysql() {
        var query = new CreateIndex(MYSQL);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateBeanColumnMysql() {
        var query = new CreateIndex(MYSQL);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCloneMysql() {
        var query = new CreateIndex(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropInstantiationMysql() {
        var query = new DropIndex(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropMysql() {
        var query = new DropIndex(MYSQL);
        query.name("idx_test").table("tablename");
        assertEquals("DROP INDEX idx_test ON tablename", query.getSql());
        var index = new CreateIndex(MYSQL);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(MYSQL), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropCloneMysql() {
        var query = new DropIndex(MYSQL);
        query.name("idx_test").table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropRequiresTableMysql() {
        var query = new DropIndex(MYSQL);
        query.name("idx_test");
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }
}
