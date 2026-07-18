/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.AlterationRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestAlterTablePgsql extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new AlterTable(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompletePgsql() {
        var query = new AlterTable(PGSQL);
        query.table("tablename");
        try {
            query.getSql();
            fail();
        } catch (AlterationRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
        query.addColumn("propstring", String.class, 50);
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new AlterTable(PGSQL);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testSingleAlterationPgsql() {
        var query = new AlterTable(PGSQL);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var query = new AlterTable(PGSQL);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
        cloned.clear();
        cloned.table("othertable")
            .dropColumn("propstring");
        assertNotEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddColumnPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddColumnNotNullPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddColumnDefaultPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddColumnBeanPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropColumnPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testRenameColumnPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename RENAME COLUMN propstring TO renamedstring", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAlterColumnTypePgsql() {
        var query = new AlterTable(PGSQL).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring TYPE VARCHAR(100)", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAlterColumnNullPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propint DROP NOT NULL", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAlterColumnNotNullPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET NOT NULL", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAlterColumnDefaultPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropColumnDefaultPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DROP DEFAULT", query.getSql());
        execute(setupTableWithString(PGSQL).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddPrimaryKeyPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddForeignKeyPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(PGSQL), query, setupForeignTable(PGSQL), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddUniquePgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD CONSTRAINT uq_name UNIQUE (propstring)", query.getSql());
        execute(setupTableWithString(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testAddCheckPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropConstraintPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(PGSQL).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropPrimaryKeyPgsql() {
        var query = new AlterTable(PGSQL).table("tablename").dropPrimaryKey();
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testRenameTablePgsql() {
        var query = new AlterTable(PGSQL).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(PGSQL), query, null, "renamedtable");
    }
}
