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

public class TestCreateViewOracle extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new CreateView(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(ORACLE).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .as(new Select(ORACLE).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateFromSelectOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .as(new Select(ORACLE).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(ORACLE), new DropView(ORACLE).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateFromStringOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCreateColumnsOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(ORACLE).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(ORACLE), new DropView(ORACLE).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testReplaceOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .replace(true)
            .as(new Select(ORACLE).from("tablename").field("propstring"));
        assertEquals("CREATE OR REPLACE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        var initial = new CreateView(ORACLE);
        initial.view("viewname")
            .as(new Select(ORACLE).from("tablename").field("propstring"));
        execute(setupTable(ORACLE), new DropView(ORACLE).view("viewname"), initial, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testParametrizedRejectedOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .as(new Select(ORACLE).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var query = new CreateView(ORACLE);
        query.view("viewname")
            .as(new Select(ORACLE).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropInstantiationOracle() {
        var query = new DropView(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDropOracle() {
        var query = new DropView(ORACLE);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
