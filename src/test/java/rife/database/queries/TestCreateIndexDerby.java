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

public class TestCreateIndexDerby extends TestCreateIndex {
    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testInstantiationDerby() {
        var query = new CreateIndex(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testIncompleteDerby() {
        var query = new CreateIndex(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testClearDerby() {
        var query = new CreateIndex(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateDerby() {
        var query = new CreateIndex(DERBY);
        query.name("idx_test")
            .table("tablename")
            .column("propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateUniqueDerby() {
        var query = new CreateIndex(DERBY);
        query.name("idx_test")
            .table("tablename")
            .unique(true)
            .column("propstring");
        assertEquals("CREATE UNIQUE INDEX idx_test ON tablename (propstring)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateMultipleColumnsDerby() {
        var query = new CreateIndex(DERBY);
        query.name("idx_test")
            .table("tablename")
            .columns("propint", "propstring");
        assertEquals("CREATE INDEX idx_test ON tablename (propint, propstring)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateBeanColumnDerby() {
        var query = new CreateIndex(DERBY);
        query.name("idx_test")
            .table("tablename")
            .column(TestAlterTable.MappedBean.class, "firstName");
        assertEquals("CREATE INDEX idx_test ON tablename (first_name)", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCloneDerby() {
        var query = new CreateIndex(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropInstantiationDerby() {
        var query = new DropIndex(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (IndexNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropIndex");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropDerby() {
        var query = new DropIndex(DERBY);
        query.name("idx_test");
        assertEquals("DROP INDEX idx_test", query.getSql());
        var index = new CreateIndex(DERBY);
        index.name("idx_test")
            .table("tablename")
            .column("propstring");
        execute(setupTable(DERBY), index, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropCloneDerby() {
        var query = new DropIndex(DERBY);
        query.name("idx_test");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
