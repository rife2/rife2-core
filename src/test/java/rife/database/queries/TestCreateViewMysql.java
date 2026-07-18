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

public class TestCreateViewMysql extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        var query = new CreateView(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testIncompleteMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(MYSQL).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testClearMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .as(new Select(MYSQL).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateFromSelectMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .as(new Select(MYSQL).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(MYSQL), new DropView(MYSQL).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateFromStringMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateColumnsMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(MYSQL).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(MYSQL), new DropView(MYSQL).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testReplaceMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .replace(true)
            .as(new Select(MYSQL).from("tablename").field("propstring"));
        assertEquals("CREATE OR REPLACE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        var initial = new CreateView(MYSQL);
        initial.view("viewname")
            .as(new Select(MYSQL).from("tablename").field("propstring"));
        execute(setupTable(MYSQL), new DropView(MYSQL).view("viewname"), initial, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testParametrizedRejectedMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .as(new Select(MYSQL).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCloneMysql() {
        var query = new CreateView(MYSQL);
        query.view("viewname")
            .as(new Select(MYSQL).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropInstantiationMysql() {
        var query = new DropView(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDropMysql() {
        var query = new DropView(MYSQL);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
