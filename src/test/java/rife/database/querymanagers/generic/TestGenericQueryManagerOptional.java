/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rifetestmodels.OptionalGqmBean;
import rifetestmodels.OptionalManyToOneBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerOptional {
    private GenericQueryManager<OptionalGqmBean> manager_ = null;

    protected void setup(Datasource datasource) {
        manager_ = GenericQueryManagerFactory.instance(datasource, OptionalGqmBean.class);
        manager_.install();
    }

    protected void tearDown() {
        manager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testOptionalBeanRoundTrip(Datasource datasource) {
        setup(datasource);
        try {
            // the idiomatic Optional shape stores its unwrapped values and
            // restores them through the recovered plain-typed setters
            var bean = new OptionalGqmBean();
            bean.setName("stored");
            bean.setCount(42);

            var id = manager_.save(bean);
            var restored = manager_.restore(id);

            assertNotNull(restored);
            assertEquals(Optional.of("stored"), restored.getName());
            assertEquals(Optional.of(42), restored.getCount());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testOptionalManyToOneFailsLoudly(Datasource datasource) {
        // Optional-wrapped relationship properties aren't supported, keep
        // them plainly typed; this pins that they fail loudly instead of
        // silently misbehaving, and flags when that ever changes
        var exception = assertThrows(Exception.class, () -> {
            var manager = GenericQueryManagerFactory.instance(datasource, OptionalManyToOneBean.class);
            manager.install();
            manager.remove();
        });
        assertInstanceOf(DatabaseException.class, exception);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAbsentValuesRestoreEmpty(Datasource datasource) {
        setup(datasource);
        try {
            // empty Optionals store as NULL columns and restore as empty
            var bean = new OptionalGqmBean();

            var id = manager_.save(bean);
            var restored = manager_.restore(id);

            assertNotNull(restored);
            assertEquals(Optional.empty(), restored.getName());
            assertEquals(Optional.empty(), restored.getCount());
        } finally {
            tearDown();
        }
    }
}
