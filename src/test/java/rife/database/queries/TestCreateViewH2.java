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

public class TestCreateViewH2 extends TestCreateView {
    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInstantiationH2() {
        var query = new CreateView(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testIncompleteH2() {
        var query = new CreateView(H2);
        query.view("viewname");
        try {
            query.getSql();
            fail();
        } catch (ViewDefinitionRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
        query.as(new Select(H2).from("tablename"));
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testClearH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .as(new Select(H2).from("tablename"));
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCreateFromSelectH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .as(new Select(H2).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(H2), new DropView(H2).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCreateFromStringH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .as("SELECT propstring FROM tablename");
        assertEquals("CREATE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCreateColumnsH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .columns("thestring")
            .as(new Select(H2).from("tablename").field("propstring"));
        assertEquals("CREATE VIEW viewname (thestring) AS SELECT propstring FROM tablename", query.getSql());
        execute(setupTable(H2), new DropView(H2).view("viewname"), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testReplaceH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .replace(true)
            .as(new Select(H2).from("tablename").field("propstring"));
        assertEquals("CREATE OR REPLACE VIEW viewname AS SELECT propstring FROM tablename", query.getSql());
        var initial = new CreateView(H2);
        initial.view("viewname")
            .as(new Select(H2).from("tablename").field("propstring"));
        execute(setupTable(H2), new DropView(H2).view("viewname"), initial, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testParametrizedRejectedH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .as(new Select(H2).from("tablename").whereParameter("propint", "="));
        try {
            query.getSql();
            fail();
        } catch (DbQueryException e) {
            assertTrue(e.getMessage().contains("parameters"));
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCloneH2() {
        var query = new CreateView(H2);
        query.view("viewname")
            .as(new Select(H2).from("tablename").field("propstring"));
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropInstantiationH2() {
        var query = new DropView(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (ViewNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropView");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDropH2() {
        var query = new DropView(H2);
        query.view("viewname");
        assertEquals("DROP VIEW viewname", query.getSql());
    }
}
