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

public class TestCreateViewHsqldb extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new CreateView(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testIncompleteHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(HSQLDB).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .as(new Select(HSQLDB).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateFromSelectHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .as(new Select(HSQLDB).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(HSQLDB), new DropView(HSQLDB).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateFromStringHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCreateColumnsHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(HSQLDB).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(HSQLDB), new DropView(HSQLDB).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testReplaceHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .replace(true)
            .as(new Select(HSQLDB).from("tablename"));
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertEquals("OR REPLACE", e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testParametrizedRejectedHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .as(new Select(HSQLDB).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new CreateView(HSQLDB);
        query.view("viewname")
            .as(new Select(HSQLDB).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropInstantiationHsqldb() {
        var query = new DropView(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testDropHsqldb() {
        var query = new DropView(HSQLDB);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
