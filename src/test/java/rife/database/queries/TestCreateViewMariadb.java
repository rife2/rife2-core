/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.exceptions.ViewDefinitionRequiredException;
import rife.database.exceptions.ViewNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateViewMariadb extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testInstantiationMariadb() {
        var query = new CreateView(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testIncompleteMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(MARIADB).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testClearMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .as(new Select(MARIADB).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateFromSelectMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .as(new Select(MARIADB).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(MARIADB), new DropView(MARIADB).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateFromStringMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCreateColumnsMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(MARIADB).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(MARIADB), new DropView(MARIADB).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testReplaceMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .replace(true)
            .as(new Select(MARIADB).from("tablename").field("propstring"));
        assertEquals("CREATE OR REPLACE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        var initial = new CreateView(MARIADB);
        initial.view("viewname")
            .as(new Select(MARIADB).from("tablename").field("propstring"));
        execute(setupTable(MARIADB), new DropView(MARIADB).view("viewname"), initial, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testParametrizedRejectedMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .as(new Select(MARIADB).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCloneMariadb() {
        var query = new CreateView(MARIADB);
        query.view("viewname")
            .as(new Select(MARIADB).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropInstantiationMariadb() {
        var query = new DropView(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testDropMariadb() {
        var query = new DropView(MARIADB);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
