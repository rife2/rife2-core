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

public class TestAlterTableOracle extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new AlterTable(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteOracle() {
        var query = new AlterTable(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new AlterTable(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testSingleAlterationOracle() {
        var query = new AlterTable(ORACLE);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var query = new AlterTable(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddColumnOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD (propstring VARCHAR2(50))", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddColumnNotNullOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD (propstring VARCHAR2(50) NOT NULL)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddColumnDefaultOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD (propstring VARCHAR2(50) DEFAULT 'abc')", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddColumnBeanOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD (first_name VARCHAR2(50) NOT NULL)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropColumnOracle() {
        var query = new AlterTable(ORACLE).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testRenameColumnOracle() {
        var query = new AlterTable(ORACLE).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename RENAME COLUMN propstring TO renamedstring", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAlterColumnTypeOracle() {
        var query = new AlterTable(ORACLE).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename MODIFY (propstring VARCHAR2(100))", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAlterColumnNullOracle() {
        var query = new AlterTable(ORACLE).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        assertEquals("ALTER TABLE tablename MODIFY (propint NULL)", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAlterColumnNotNullOracle() {
        var query = new AlterTable(ORACLE).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename MODIFY (propstring NOT NULL)", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAlterColumnDefaultOracle() {
        var query = new AlterTable(ORACLE).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename MODIFY (propstring DEFAULT 'def')", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropColumnDefaultOracle() {
        var query = new AlterTable(ORACLE).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename MODIFY (propstring DEFAULT NULL)", query.getSql());
        execute(setupTableWithString(ORACLE).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddPrimaryKeyOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddForeignKeyOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(ORACLE), query, setupForeignTable(ORACLE), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddUniqueOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD CONSTRAINT uq_name UNIQUE (propstring)", query.getSql());
        execute(setupTableWithString(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testAddCheckOracle() {
        var query = new AlterTable(ORACLE).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(ORACLE), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropConstraintOracle() {
        var query = new AlterTable(ORACLE).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(ORACLE).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropPrimaryKeyOracle() {
        var query = new AlterTable(ORACLE).table("tablename").dropPrimaryKey();
        assertEquals("ALTER TABLE tablename DROP PRIMARY KEY", query.getSql());
        execute(setupTable(ORACLE).primaryKey("pk_tablename", "propint"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testRenameTableOracle() {
        var query = new AlterTable(ORACLE).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(ORACLE), query, null, "renamedtable");
    }
}
