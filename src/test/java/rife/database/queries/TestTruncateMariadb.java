/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestTruncateMariadb extends TestTruncate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testInstantiationMariadb() {
        var query = new Truncate(MARIADB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Truncate");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testTruncateMariadb() {
        var query = new Truncate(MARIADB);
        query.table("tablename");
        assertEquals("TRUNCATE TABLE tablename", query.getSql());
        execute(setupTable(MARIADB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MARIADB)
    void testCloneMariadb() {
        var query = new Truncate(MARIADB);
        query.table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
