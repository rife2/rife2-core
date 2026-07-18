/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestTruncateDerby extends TestTruncate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testInstantiationDerby() {
        var query = new Truncate(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Truncate");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testTruncateDerby() {
        var query = new Truncate(DERBY);
        query.table("tablename");
        assertEquals("TRUNCATE TABLE tablename", query.getSql());
        execute(setupTable(DERBY), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.DERBY)
    void testCloneDerby() {
        var query = new Truncate(DERBY);
        query.table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }
}
