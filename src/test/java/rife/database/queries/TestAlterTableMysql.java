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

public class TestAlterTableMysql extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        var query = new AlterTable(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testIncompleteMysql() {
        var query = new AlterTable(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testClearMysql() {
        var query = new AlterTable(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testSingleAlterationMysql() {
        var query = new AlterTable(MYSQL);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCloneMysql() {
        var query = new AlterTable(MYSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddColumnMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddColumnNotNullMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddColumnDefaultMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddColumnBeanMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropColumnMysql() {
        var query = new AlterTable(MYSQL).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testRenameColumnMysql() {
        var query = new AlterTable(MYSQL).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename RENAME COLUMN propstring TO renamedstring", query.getSql());
        execute(setupTableWithString(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAlterColumnTypeMysql() {
        var query = new AlterTable(MYSQL).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename MODIFY COLUMN propstring VARCHAR(100)", query.getSql());
        execute(setupTableWithString(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAlterColumnNullMysql() {
        var query = new AlterTable(MYSQL).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAlterColumnNotNullMysql() {
        var query = new AlterTable(MYSQL).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertNotNull(e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAlterColumnDefaultMysql() {
        var query = new AlterTable(MYSQL).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropColumnDefaultMysql() {
        var query = new AlterTable(MYSQL).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DROP DEFAULT", query.getSql());
        execute(setupTableWithString(MYSQL).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddPrimaryKeyMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddForeignKeyMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(MYSQL), query, setupForeignTable(MYSQL), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddUniqueMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD UNIQUE uq_name (propstring)", query.getSql());
        execute(setupTableWithString(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testAddCheckMysql() {
        var query = new AlterTable(MYSQL).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(MYSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropConstraintMysql() {
        var query = new AlterTable(MYSQL).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(MYSQL).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropPrimaryKeyMysql() {
        var query = new AlterTable(MYSQL).table("tablename").dropPrimaryKey();
        assertEquals("ALTER TABLE tablename DROP PRIMARY KEY", query.getSql());
        execute(setupTable(MYSQL).primaryKey("pk_tablename", "propint"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testRenameTableMysql() {
        var query = new AlterTable(MYSQL).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(MYSQL), query, null, "renamedtable");
    }
}
