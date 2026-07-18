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

public class TestAlterTableDerby extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testInstantiationDerby() {
        var query = new AlterTable(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testIncompleteDerby() {
        var query = new AlterTable(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testClearDerby() {
        var query = new AlterTable(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testSingleAlterationDerby() {
        var query = new AlterTable(DERBY);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCloneDerby() {
        var query = new AlterTable(DERBY);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddColumnDerby() {
        var query = new AlterTable(DERBY).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddColumnNotNullDerby() {
        // Derby requires a default value when a not null column is added
        var query = new AlterTable(DERBY).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc' NOT NULL", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddColumnDefaultDerby() {
        var query = new AlterTable(DERBY).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddColumnBeanDerby() {
        // Derby requires a default value when a not null column is added
        var query = new AlterTable(DERBY).table("tablename").addColumn(MappedBean.class, "firstName").defaultValue("first_name", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) DEFAULT 'abc' NOT NULL", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropColumnDerby() {
        var query = new AlterTable(DERBY).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testRenameColumnDerby() {
        var query = new AlterTable(DERBY).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("RENAME COLUMN tablename.propstring TO renamedstring", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAlterColumnTypeDerby() {
        var query = new AlterTable(DERBY).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DATA TYPE VARCHAR(100)", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAlterColumnNullDerby() {
        var query = new AlterTable(DERBY).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propint NULL", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAlterColumnNotNullDerby() {
        var query = new AlterTable(DERBY).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring NOT NULL", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAlterColumnDefaultDerby() {
        var query = new AlterTable(DERBY).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropColumnDefaultDerby() {
        var query = new AlterTable(DERBY).table("tablename").dropColumnDefault("propstring");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddPrimaryKeyDerby() {
        var query = new AlterTable(DERBY).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddForeignKeyDerby() {
        var query = new AlterTable(DERBY).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(DERBY), query, setupForeignTable(DERBY), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddUniqueDerby() {
        var query = new AlterTable(DERBY).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD CONSTRAINT uq_name UNIQUE (propstring)", query.getSql());
        execute(setupTableWithString(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testAddCheckDerby() {
        var query = new AlterTable(DERBY).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropConstraintDerby() {
        var query = new AlterTable(DERBY).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(DERBY).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropPrimaryKeyDerby() {
        var query = new AlterTable(DERBY).table("tablename").dropPrimaryKey();
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testRenameTableDerby() {
        var query = new AlterTable(DERBY).table("tablename").renameTo("renamedtable");
        assertEquals("RENAME TABLE tablename TO renamedtable", query.getSql());
        execute(setupTable(DERBY), query, null, "renamedtable");
    }
}
