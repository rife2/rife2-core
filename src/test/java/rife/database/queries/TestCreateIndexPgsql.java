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

public class TestCreateIndexPgsql extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new CreateIndex(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompletePgsql() {
        var query = new CreateIndex(PGSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new CreateIndex(PGSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreatePgsql() {
        var query = new CreateIndex(PGSQL);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateUniquePgsql() {
        var query = new CreateIndex(PGSQL);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateMultipleColumnsPgsql() {
        var query = new CreateIndex(PGSQL);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateBeanColumnPgsql() {
        var query = new CreateIndex(PGSQL);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var query = new CreateIndex(PGSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropInstantiationPgsql() {
        var query = new DropIndex(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropPgsql() {
        var query = new DropIndex(PGSQL);
        query.name("idx_test");
        assertEquals("DROP INDEX idx_test", query.getSql());
        var index = new CreateIndex(PGSQL);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(PGSQL), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropClonePgsql() {
        var query = new DropIndex(PGSQL);
        query.name("idx_test");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
