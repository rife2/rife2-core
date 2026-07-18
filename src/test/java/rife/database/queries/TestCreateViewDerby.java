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

public class TestCreateViewDerby extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testInstantiationDerby() {
        var query = new CreateView(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testIncompleteDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(DERBY).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testClearDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .as(new Select(DERBY).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateFromSelectDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .as(new Select(DERBY).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(DERBY), new DropView(DERBY).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateFromStringDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCreateColumnsDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(DERBY).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(DERBY), new DropView(DERBY).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testReplaceDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .replace(true)
            .as(new Select(DERBY).from("tablename"));
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertEquals("OR REPLACE", e.getFeature());
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testParametrizedRejectedDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .as(new Select(DERBY).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCloneDerby() {
        var query = new CreateView(DERBY);
        query.view("viewname")
            .as(new Select(DERBY).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropInstantiationDerby() {
        var query = new DropView(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testDropDerby() {
        var query = new DropView(DERBY);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
