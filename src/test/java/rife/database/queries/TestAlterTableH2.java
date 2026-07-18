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

public class TestAlterTableH2 extends TestAlterTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInstantiationH2() {
        var query = new AlterTable(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "AlterTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testIncompleteH2() {
        var query = new AlterTable(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testClearH2() {
        var query = new AlterTable(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testSingleAlterationH2() {
        var query = new AlterTable(H2);
        query.table("tablename")
            .addColumn("propstring", String.class, 50);
        try {
            query.dropColumn("propstring");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exactly one alteration"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCloneH2() {
        var query = new AlterTable(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddColumnH2() {
        var query = new AlterTable(H2).table("tablename").addColumn("propstring", String.class, 50);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50)", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddColumnNotNullH2() {
        var query = new AlterTable(H2).table("tablename").addColumn("propstring", String.class, 50, AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddColumnDefaultH2() {
        var query = new AlterTable(H2).table("tablename").addColumn("propstring", String.class, 50).defaultValue("propstring", "abc");
        assertEquals("ALTER TABLE tablename ADD COLUMN propstring VARCHAR(50) DEFAULT 'abc'", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddColumnBeanH2() {
        var query = new AlterTable(H2).table("tablename").addColumn(MappedBean.class, "firstName");
        assertEquals("ALTER TABLE tablename ADD COLUMN first_name VARCHAR(50) NOT NULL", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropColumnH2() {
        var query = new AlterTable(H2).table("tablename").dropColumn("propstring");
        assertEquals("ALTER TABLE tablename DROP COLUMN propstring", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testRenameColumnH2() {
        var query = new AlterTable(H2).table("tablename").renameColumn("propstring", "renamedstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring RENAME TO renamedstring", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAlterColumnTypeH2() {
        var query = new AlterTable(H2).table("tablename").alterColumnType("propstring", String.class, 100);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DATA TYPE VARCHAR(100)", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAlterColumnNullH2() {
        var query = new AlterTable(H2).table("tablename").alterColumnNullable("propint", AlterTable.NULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propint SET NULL", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAlterColumnNotNullH2() {
        var query = new AlterTable(H2).table("tablename").alterColumnNullable("propstring", AlterTable.NOTNULL);
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET NOT NULL", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAlterColumnDefaultH2() {
        var query = new AlterTable(H2).table("tablename").alterColumnDefault("propstring", "def");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring SET DEFAULT 'def'", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropColumnDefaultH2() {
        var query = new AlterTable(H2).table("tablename").dropColumnDefault("propstring");
        assertEquals("ALTER TABLE tablename ALTER COLUMN propstring DROP DEFAULT", query.getSql());
        execute(setupTableWithString(H2).defaultValue("propstring", "abc"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddPrimaryKeyH2() {
        var query = new AlterTable(H2).table("tablename").addPrimaryKey("propint");
        assertEquals("ALTER TABLE tablename ADD PRIMARY KEY (propint)", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddForeignKeyH2() {
        var query = new AlterTable(H2).table("tablename").addForeignKey("foreigntable", "propint", "foreignint");
        assertEquals("ALTER TABLE tablename ADD FOREIGN KEY (propint) REFERENCES foreigntable (foreignint)", query.getSql());
        execute(setupTable(H2), query, setupForeignTable(H2), "tablename");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddUniqueH2() {
        var query = new AlterTable(H2).table("tablename").addUnique("uq_name", "propstring");
        assertEquals("ALTER TABLE tablename ADD CONSTRAINT uq_name UNIQUE (propstring)", query.getSql());
        execute(setupTableWithString(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testAddCheckH2() {
        var query = new AlterTable(H2).table("tablename").addCheck("propint > 0");
        assertEquals("ALTER TABLE tablename ADD CHECK (propint > 0)", query.getSql());
        execute(setupTable(H2), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropConstraintH2() {
        var query = new AlterTable(H2).table("tablename").dropConstraint("uq_name");
        assertEquals("ALTER TABLE tablename DROP CONSTRAINT uq_name", query.getSql());
        execute(setupTableWithString(H2).unique("uq_name", "propstring"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropPrimaryKeyH2() {
        var query = new AlterTable(H2).table("tablename").dropPrimaryKey();
        assertEquals("ALTER TABLE tablename DROP PRIMARY KEY", query.getSql());
        execute(setupTable(H2).primaryKey("pk_tablename", "propint"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testRenameTableH2() {
        var query = new AlterTable(H2).table("tablename").renameTo("renamedtable");
        assertEquals("ALTER TABLE tablename RENAME TO renamedtable", query.getSql());
        execute(setupTable(H2), query, null, "renamedtable");
    }
}
