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

public class TestCreateIndexOracle extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new CreateIndex(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteOracle() {
        var query = new CreateIndex(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new CreateIndex(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateOracle() {
        var query = new CreateIndex(ORACLE);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateUniqueOracle() {
        var query = new CreateIndex(ORACLE);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateMultipleColumnsOracle() {
        var query = new CreateIndex(ORACLE);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateBeanColumnOracle() {
        var query = new CreateIndex(ORACLE);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var query = new CreateIndex(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropInstantiationOracle() {
        var query = new DropIndex(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropOracle() {
        var query = new DropIndex(ORACLE);
        query.name("idx_test");
        assertEquals("DROP INDEX idx_test", query.getSql());
        var index = new CreateIndex(ORACLE);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(ORACLE), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropCloneOracle() {
        var query = new DropIndex(ORACLE);
        query.name("idx_test");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
