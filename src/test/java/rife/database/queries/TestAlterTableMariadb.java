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

public class TestAlterTableMariadb extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testInstantiationMariadb() {
        var query = new AlterTable(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testIncompleteMariadb() {
        var query = new AlterTable(MARIADB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testClearMariadb() {
        var query = new AlterTable(MARIADB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testSingleAlterationMariadb() {
        var query = new AlterTable(MARIADB);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCloneMariadb() {
        var query = new AlterTable(MARIADB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddColumnMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddColumnNotNullMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddColumnDefaultMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddColumnBeanMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropColumnMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testRenameColumnMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename RENAME COLUMN propstring TO renamedstring", query.getSql());
        execute(setupTableWithString(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAlterColumnTypeMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename MODIFY COLUMN propstring VARCHAR(100)", query.getSql());
        execute(setupTableWithString(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAlterColumnNullMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAlterColumnNotNullMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAlterColumnDefaultMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropColumnDefaultMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DROP DEFAULT", query.getSql());
        execute(setupTableWithString(MARIADB).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddPrimaryKeyMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddForeignKeyMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(MARIADB), query, setupForeignTable(MARIADB), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddUniqueMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD UNIQUE uq_name (propstring)", query.getSql());
        execute(setupTableWithString(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testAddCheckMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropConstraintMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(MARIADB).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropPrimaryKeyMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").dropPrimaryKey();
        assertEquals("ALTER TABLE tablename DROP PRIMARY KEY", query.getSql());
        execute(setupTable(MARIADB).primaryKey("pk_tablename", "propint"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testRenameTableMariadb() {
        var query = new AlterTable(MARIADB).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(MARIADB), query, null, "renamedtable");
    }
}
