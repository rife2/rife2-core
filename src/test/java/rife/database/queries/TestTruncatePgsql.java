/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestTruncatePgsql extends TestTruncate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new Truncate(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Truncate");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testTruncatePgsql() {
        var query = new Truncate(PGSQL);
        query.table("tablename");
        assertEquals("TRUNCATE TABLE tablename", query.getSql());
        execute(setupTable(PGSQL), query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var query = new Truncate(PGSQL);
        query.table("tablename");
        var cloned = query.clone();
        assertNotNull(cloned);
        assertNotSame(query, cloned);
        assertEquals(query.getSql(), cloned.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testTruncateMultiplePgsql() {
        var query = new Truncate(PGSQL);
        query.table("tablename")
            .table("tablename2");
        assertEquals("TRUNCATE TABLE tablename, tablename2", query.getSql());
        var manager = new DbQueryManager(PGSQL);
        manager.executeUpdate(setupTable(PGSQL));
        manager.executeUpdate(new CreateTable(PGSQL).table("tablename2").column("propint", int.class));
        try {
            manager.executeUpdate(query);
        } finally {
            try { manager.executeUpdate(new DropTable(PGSQL).table("tablename")); } catch (rife.database.exceptions.DatabaseException e) { }
            try { manager.executeUpdate(new DropTable(PGSQL).table("tablename2")); } catch (rife.database.exceptions.DatabaseException e) { }
        }
    }
}
