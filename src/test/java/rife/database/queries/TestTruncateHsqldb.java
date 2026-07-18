/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestTruncateHsqldb extends TestTruncate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new Truncate(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Truncate");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testTruncateHsqldb() {
        var query = new Truncate(HSQLDB);
        query.table("tablename");
        assertEquals("TRUNCATE TABLE tablename", query.getSql());
        execute(setupTable(HSQLDB), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new Truncate(HSQLDB);
        query.table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
