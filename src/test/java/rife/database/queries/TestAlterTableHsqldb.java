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

public class TestAlterTableHsqldb extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new AlterTable(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testIncompleteHsqldb() {
        var query = new AlterTable(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new AlterTable(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testSingleAlterationHsqldb() {
        var query = new AlterTable(HSQLDB);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new AlterTable(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddColumnHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddColumnNotNullHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddColumnDefaultHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddColumnBeanHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropColumnHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testRenameColumnHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring RENAME TO renamedstring", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAlterColumnTypeHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DATA TYPE VARCHAR(100)", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAlterColumnNullHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propint SET NULL", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAlterColumnNotNullHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET NOT NULL", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAlterColumnDefaultHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropColumnDefaultHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DROP DEFAULT", query.getSql());
        execute(setupTableWithString(HSQLDB).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddPrimaryKeyHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddForeignKeyHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(HSQLDB), query, setupForeignTable(HSQLDB), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddUniqueHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD CONSTRAINT uq_name UNIQUE (propstring)", query.getSql());
        execute(setupTableWithString(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testAddCheckHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropConstraintHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(HSQLDB).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropPrimaryKeyHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").dropPrimaryKey();
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testRenameTableHsqldb() {
        var query = new AlterTable(HSQLDB).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(HSQLDB), query, null, "renamedtable");
    }
}
