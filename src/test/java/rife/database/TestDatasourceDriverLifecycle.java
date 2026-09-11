/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatasourceDriverLifecycle {
    /**
     * Rebuilds the arrangement a deployed war has: RIFE2 and the JDBC driver in a
     * classloader of their own, where DriverManager's service loader never sees the
     * driver and the first Datasource to load it is what registers it. On the test
     * classpath the driver is already registered, so none of this shows up there.
     */
    private static URLClassLoader webInfLib() throws Exception {
        var rife2 = Datasource.class.getProtectionDomain().getCodeSource().getLocation();
        var driver = Class.forName("org.h2.Driver").getProtectionDomain().getCodeSource().getLocation();
        // the probe below has to load here too, DriverManager resolves drivers against
        // its caller's classloader and would otherwise answer for the test classpath
        var probe = TestDatasourceDriverLifecycle.class.getProtectionDomain().getCodeSource().getLocation();
        return new URLClassLoader(new URL[]{rife2, driver, probe}, ClassLoader.getPlatformClassLoader());
    }

    public static class DriverProbe {
        public static boolean registered(String url) {
            try {
                java.sql.DriverManager.getDriver(url);
                return true;
            } catch (java.sql.SQLException e) {
                return false;
            }
        }
    }

    private static boolean driverRegisteredIn(URLClassLoader loader, String url) throws Exception {
        var probe = Class.forName("rife.database.TestDatasourceDriverLifecycle$DriverProbe", true, loader);
        return (boolean) probe.getMethod("registered", String.class).invoke(null, url);
    }

    @Test
    void cleanupLeavesLaterDatasourcesWorking() throws Exception {
        try (var loader = webInfLib()) {
            var datasource = Class.forName("rife.database.Datasource", true, loader);
            var construct = datasource.getConstructor(String.class, String.class, String.class, String.class, int.class);
            // three datasources in a row, each cleaned up like a request would
            for (var i = 1; i <= 3; i++) {
                var instance = construct.newInstance("org.h2.Driver", "jdbc:h2:mem:lifecycle" + i, "sa", "", 5);
                var connection = datasource.getMethod("getConnection").invoke(instance);
                connection.getClass().getMethod("close").invoke(connection);
                datasource.getMethod("cleanup").invoke(instance);
            }
        }
    }

    @Test
    void closeReleasesTheDriverWithoutStrandingTheNextOne() throws Exception {
        try (var loader = webInfLib()) {
            var datasource = Class.forName("rife.database.Datasource", true, loader);
            var construct = datasource.getConstructor(String.class, String.class, String.class, String.class, int.class);
            for (var i = 1; i <= 3; i++) {
                var instance = construct.newInstance("org.h2.Driver", "jdbc:h2:mem:released" + i, "sa", "", 5);
                var connection = datasource.getMethod("getConnection").invoke(instance);
                connection.getClass().getMethod("close").invoke(connection);
                assertTrue(driverRegisteredIn(loader, "jdbc:h2:mem:released"),
                    "the driver has to be registered while the datasource is open");
                datasource.getMethod("close").invoke(instance);
                // try-with-resources has to release the driver every time, not just once
                assertFalse(driverRegisteredIn(loader, "jdbc:h2:mem:released"),
                    "close() has to deregister the driver it registered");
            }
        }
    }

    @Test
    void shutdownStillDeregistersTheDriverItRegistered() throws Exception {
        try (var loader = webInfLib()) {
            var datasource = Class.forName("rife.database.Datasource", true, loader);
            var construct = datasource.getConstructor(String.class, String.class, String.class, String.class, int.class);
            var first = construct.newInstance("org.h2.Driver", "jdbc:h2:mem:shutdown1", "sa", "", 5);
            var connection = datasource.getMethod("getConnection").invoke(first);
            connection.getClass().getMethod("close").invoke(connection);

            datasource.getMethod("closeAllActiveDatasources").invoke(null);

            // an undeployed application must not leave its driver behind in DriverManager
            var second = construct.newInstance("org.h2.Driver", "jdbc:h2:mem:shutdown2", "sa", "", 5);
            assertThrows(Exception.class, () -> datasource.getMethod("getConnection").invoke(second),
                "the shutdown hook has to deregister the driver it registered");
        }
    }
}
