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

public class TestCreateViewPgsql extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new CreateView(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompletePgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(PGSQL).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .as(new Select(PGSQL).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateFromSelectPgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(PGSQL), new DropView(PGSQL).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateFromStringPgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testCreateColumnsPgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(PGSQL), new DropView(PGSQL).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testReplacePgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .replace(true)
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        assertEquals("CREATE OR REPLACE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        var initial = new CreateView(PGSQL);
        initial.view("viewname")
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        execute(setupTable(PGSQL), new DropView(PGSQL).view("viewname"), initial, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testParametrizedRejectedPgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .as(new Select(PGSQL).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var query = new CreateView(PGSQL);
        query.view("viewname")
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropInstantiationPgsql() {
        var query = new DropView(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropPgsql() {
        var query = new DropView(PGSQL);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDropMultiplePgsql() {
        var query = new DropView(PGSQL);
        query.view("view1")
            .view("view2");
        assertEquals("DROP VIEW view1, view2", query.getSql());
        var view1 = new CreateView(PGSQL);
        view1.view("view1")
            .as(new Select(PGSQL).from("tablename").field("propstring"));
        var view2 = new CreateView(PGSQL);
        view2.view("view2")
            .as(new Select(PGSQL).from("tablename").field("propint"));
        execute(setupTable(PGSQL), query, view1, view2);
    }
}
