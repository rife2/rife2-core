/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.migrations.exceptions.IrreversibleMigrationException;
import rife.database.migrations.exceptions.MigrationException;
import rife.database.queries.Query;
import rife.resources.ResourceFinder;
import rife.resources.ResourceWriter;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.resources.exceptions.ResourceWriterErrorException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Executes an explicitly registered series of {@link DbMigration
 * migrations} against a datasource.
 * <p>
 * Migrations are registered by hand with strictly increasing version
 * numbers, nothing is discovered or executed implicitly:
 * <pre>var migrations = new DbMigrations(datasource)
 *    .state(DatabaseResourcesFactory.instance(datasource))
 *    .add(1, new CreateInitialSchema())
 *    .add(2, new AddUserTotals());
 *migrations.migrate();</pre>
 * <p>
 * The current version is stored as the content of a resource through
 * {@link ResourceFinder} and {@link ResourceWriter}. Use
 * {@link rife.resources.DatabaseResources} to store it in the same
 * database, it has to be installed explicitly beforehand, or
 * {@link rife.resources.DirectoryResources} to store it as a plain file.
 * The resource name defaults to
 * {@link RifeConfig.MigrationsConfig#DEFAULT_STATE_RESOURCE} and can be
 * changed through {@code RifeConfig.migrations()} or per instance.
 * <p>
 * The methods with a {@code From} suffix don't use the stored version:
 * the current version is passed in as an argument and the new one is
 * returned, storing it is up to the caller.
 * <p>
 * Each migration executes inside a transaction together with its state
 * update. Note that not every database can roll back DDL, there a failed
 * migration can leave part of its schema changes behind while the
 * version stays unchanged.
 * <p>
 * Migrations are meant to be run as an explicit operation from a single
 * place, concurrent {@code migrate} calls from several application
 * instances against the same database aren't coordinated.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class DbMigrations {
    private final Datasource datasource_;
    private final DbQueryManager manager_;
    private final LinkedHashMap<Integer, DbMigration> migrations_ = new LinkedHashMap<>();
    private int highestVersion_ = 0;
    private ResourceFinder stateFinder_ = null;
    private ResourceWriter stateWriter_ = null;
    private String stateResource_ = null;

    public DbMigrations(Datasource datasource) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        datasource_ = datasource;
        manager_ = new DbQueryManager(datasource);
    }

    /**
     * Registers a migration with an explicit version.
     * <p>
     * Versions have to be positive and strictly increasing in
     * registration order, the registered list is the complete migration
     * history of the application.
     *
     * @param version   the version this migration brings the schema to
     * @param migration the migration to register
     * @return this instance
     * @since 1.10
     */
    public DbMigrations add(int version, DbMigration migration) {
        if (version <= 0) throw new IllegalArgumentException("version has to be positive.");
        if (null == migration) throw new IllegalArgumentException("migration can't be null.");
        if (version <= highestVersion_) {
            throw new IllegalArgumentException("migration versions have to be strictly increasing, version " + version + " isn't higher than " + highestVersion_ + ".");
        }

        migrations_.put(version, migration);
        highestVersion_ = version;
        return this;
    }

    /**
     * Provides the state storage with the default resource name.
     *
     * @param <T>       the type of the combined resource finder and writer
     * @param resources the resource store for the migration state
     * @return this instance
     * @since 1.10
     */
    public <T extends ResourceFinder & ResourceWriter> DbMigrations state(T resources) {
        return state(resources, resources, RifeConfig.migrations().getStateResource());
    }

    /**
     * Provides the state storage with an explicit resource name.
     *
     * @param <T>          the type of the combined resource finder and writer
     * @param resources    the resource store for the migration state
     * @param resourceName the name of the resource that holds the version
     * @return this instance
     * @since 1.10
     */
    public <T extends ResourceFinder & ResourceWriter> DbMigrations state(T resources, String resourceName) {
        return state(resources, resources, resourceName);
    }

    /**
     * Provides separate state reader and writer with an explicit
     * resource name.
     * <p>The content of the state resource is trusted as-is, editing it
     * by hand is equivalent to calling {@link #baseline}.
     *
     * @param finder       the finder that reads the migration state
     * @param writer       the writer that stores the migration state
     * @param resourceName the name of the resource that holds the version
     * @return this instance
     * @since 1.10
     */
    public DbMigrations state(ResourceFinder finder, ResourceWriter writer, String resourceName) {
        if (null == finder) throw new IllegalArgumentException("finder can't be null.");
        if (null == writer) throw new IllegalArgumentException("writer can't be null.");
        if (null == resourceName) throw new IllegalArgumentException("resourceName can't be null.");
        if (resourceName.isEmpty()) throw new IllegalArgumentException("resourceName can't be empty.");

        stateFinder_ = finder;
        stateWriter_ = writer;
        stateResource_ = resourceName;
        return this;
    }

    /**
     * Retrieves the registered migration versions in order.
     *
     * @return the registered versions
     * @since 1.10
     */
    public List<Integer> versions() {
        return List.copyOf(migrations_.keySet());
    }

    /**
     * Retrieves the registered versions that are higher than a provided
     * current version.
     *
     * @param currentVersion the version to compare against
     * @return the pending versions
     * @since 1.10
     */
    public List<Integer> pendingFrom(int currentVersion) {
        var result = new ArrayList<Integer>();
        for (var version : migrations_.keySet()) {
            if (version > currentVersion) {
                result.add(version);
            }
        }
        return result;
    }

    /**
     * Migrates from a provided current version to the highest registered
     * version, without touching any state.
     *
     * @param currentVersion the version the schema is currently at
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int migrateFrom(int currentVersion) {
        return migrateFrom(currentVersion, Math.max(currentVersion, highestVersion_));
    }

    /**
     * Migrates from a provided current version to a target version,
     * without touching any state.
     *
     * @param currentVersion the version the schema is currently at
     * @param toVersion      the version to migrate to
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int migrateFrom(int currentVersion, int toVersion) {
        if (toVersion < currentVersion) {
            throw new IllegalArgumentException("can't migrate to version " + toVersion + " from the higher version " + currentVersion + ", use rollback instead.");
        }

        var version = currentVersion;
        for (var entry : migrations_.entrySet()) {
            if (entry.getKey() > currentVersion && entry.getKey() <= toVersion) {
                executeMigration(entry.getKey(), entry.getValue(), true, null);
                version = entry.getKey();
            }
        }
        return version;
    }

    /**
     * Rolls back the migration that a provided current version
     * corresponds to, without touching any state.
     *
     * @param currentVersion the version the schema is currently at
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int rollbackFrom(int currentVersion) {
        var version = highestRegisteredUpTo(currentVersion);
        if (0 == version) {
            return currentVersion;
        }
        return rollbackFrom(currentVersion, highestRegisteredBelow(version));
    }

    /**
     * Rolls back from a provided current version to a target version,
     * without touching any state.
     *
     * @param currentVersion the version the schema is currently at
     * @param toVersion      the version to roll back to, {@code 0} or a
     *                       registered version
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int rollbackFrom(int currentVersion, int toVersion) {
        if (toVersion < 0) throw new IllegalArgumentException("toVersion can't be negative.");
        if (toVersion != 0 && !migrations_.containsKey(toVersion)) {
            throw new IllegalArgumentException("toVersion has to be 0 or a registered migration version.");
        }
        if (toVersion > currentVersion) {
            throw new IllegalArgumentException("can't roll back to version " + toVersion + " from the lower version " + currentVersion + ", use migrate instead.");
        }

        var descending = new ArrayList<>(migrations_.keySet());
        java.util.Collections.reverse(descending);
        for (var version : descending) {
            if (version <= currentVersion && version > toVersion) {
                var migration = migrations_.get(version);
                if (!migration.isReversible()) {
                    throw new IrreversibleMigrationException(version);
                }
                executeMigration(version, migration, false, null);
            }
        }
        return toVersion;
    }

    /**
     * Records a version in the configured state without executing any
     * migration.
     * <p>
     * This adopts an existing database into the migration sequence: the
     * schema is declared to already be at the provided version and only
     * later migrations will be applied by {@link #migrate}.
     *
     * @param version the version the existing schema corresponds to,
     *                {@code 0} or a registered migration version
     * @return the recorded version
     * @since 1.10
     */
    public int baseline(int version) {
        ensureState();

        if (version < 0) throw new IllegalArgumentException("version can't be negative.");
        if (version != 0 && !migrations_.containsKey(version)) {
            throw new IllegalArgumentException("version has to be 0 or a registered migration version.");
        }

        writeVersion(version);
        return version;
    }

    /**
     * Generates the SQL statements of the pending migrations according
     * to the configured state, without executing anything.
     *
     * @return the SQL statements that {@link #migrate()} would execute
     * @since 1.10
     */
    public List<String> preview() {
        return previewFrom(currentVersion());
    }

    /**
     * Generates the SQL statements that would migrate from a provided
     * current version to the highest registered version, without
     * executing anything.
     *
     * @param currentVersion the version the schema is currently at
     * @return the SQL statements that would be executed
     * @since 1.10
     */
    public List<String> previewFrom(int currentVersion) {
        return previewFrom(currentVersion, highestVersion_);
    }

    /**
     * Generates the SQL statements that would migrate from a provided
     * current version to a target version, without executing anything.
     * <p>
     * Every migration starts with a SQL comment. Action steps appear as
     * a placeholder comment since their Java logic can't be previewed.
     *
     * @param currentVersion the version the schema is currently at
     * @param toVersion      the version to migrate to
     * @return the SQL statements that would be executed
     * @since 1.10
     */
    public List<String> previewFrom(int currentVersion, int toVersion) {
        var result = new ArrayList<String>();
        for (var entry : migrations_.entrySet()) {
            if (entry.getKey() > currentVersion && entry.getKey() <= toVersion) {
                result.add("-- migration " + entry.getKey());
                for (var step : entry.getValue().collectUpSteps(datasource_)) {
                    if (step instanceof Query query) {
                        result.add(query.getSql());
                    } else if (step instanceof String sql) {
                        result.add(sql);
                    } else {
                        result.add("-- java action");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieves the current version from the configured state.
     *
     * @return the current version; or {@code 0} when no migration was
     * applied yet
     * @since 1.10
     */
    public int currentVersion() {
        ensureState();

        String content;
        try {
            content = stateFinder_.getContent(stateResource_);
        } catch (ResourceFinderErrorException e) {
            throw new MigrationException("Unable to read the migration state from resource '" + stateResource_ + "'.", e);
        }
        if (null == content || content.isBlank()) {
            return 0;
        }
        int version;
        try {
            version = Integer.parseInt(content.trim());
        } catch (NumberFormatException e) {
            throw new MigrationException("The migration state resource '" + stateResource_ + "' doesn't contain a version number but '" + content + "'.");
        }
        if (version < 0) {
            throw new MigrationException("The migration state resource '" + stateResource_ + "' contains the negative version " + version + ".");
        }
        return version;
    }

    /**
     * Retrieves the registered versions that haven't been applied yet
     * according to the configured state.
     *
     * @return the pending versions
     * @since 1.10
     */
    public List<Integer> pending() {
        return pendingFrom(currentVersion());
    }

    /**
     * Migrates to the highest registered version, tracking the current
     * version in the configured state.
     *
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int migrate() {
        return migrate(Math.max(currentVersion(), highestVersion_));
    }

    /**
     * Migrates to a target version, tracking the current version in the
     * configured state.
     *
     * @param toVersion the version to migrate to
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int migrate(int toVersion) {
        ensureState();

        var current = currentVersion();
        if (toVersion < current) {
            throw new IllegalArgumentException("can't migrate to version " + toVersion + " from the higher version " + current + ", use rollback instead.");
        }

        var version = current;
        for (var entry : migrations_.entrySet()) {
            if (entry.getKey() > current && entry.getKey() <= toVersion) {
                executeMigration(entry.getKey(), entry.getValue(), true, entry.getKey());
                version = entry.getKey();
            }
        }
        return version;
    }

    /**
     * Rolls back the last applied migration, tracking the current
     * version in the configured state.
     *
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int rollback() {
        ensureState();

        var current = currentVersion();
        var version = highestRegisteredUpTo(current);
        if (0 == version) {
            return current;
        }
        return rollback(highestRegisteredBelow(version));
    }

    /**
     * Rolls back to a target version, tracking the current version in
     * the configured state.
     *
     * @param toVersion the version to roll back to, {@code 0} or a
     *                  registered version
     * @return the version the schema is at afterwards
     * @since 1.10
     */
    public int rollback(int toVersion) {
        ensureState();

        if (toVersion < 0) throw new IllegalArgumentException("toVersion can't be negative.");
        if (toVersion != 0 && !migrations_.containsKey(toVersion)) {
            throw new IllegalArgumentException("toVersion has to be 0 or a registered migration version.");
        }

        var current = currentVersion();
        if (toVersion > current) {
            throw new IllegalArgumentException("can't roll back to version " + toVersion + " from the lower version " + current + ", use migrate instead.");
        }
        var descending = new ArrayList<>(migrations_.keySet());
        java.util.Collections.reverse(descending);
        for (var version : descending) {
            if (version <= current && version > toVersion) {
                var migration = migrations_.get(version);
                if (!migration.isReversible()) {
                    throw new IrreversibleMigrationException(version);
                }
                var resulting = Math.max(highestRegisteredBelow(version), toVersion);
                executeMigration(version, migration, false, resulting);
            }
        }
        return toVersion;
    }

    private void ensureState() {
        if (null == stateFinder_ || null == stateWriter_) {
            throw new IllegalStateException("No migration state was configured, provide it with state() or use the From variants.");
        }
    }

    private int highestRegisteredUpTo(int version) {
        var result = 0;
        for (var registered : migrations_.keySet()) {
            if (registered <= version) {
                result = registered;
            }
        }
        return result;
    }

    private int highestRegisteredBelow(int version) {
        var result = 0;
        for (var registered : migrations_.keySet()) {
            if (registered < version) {
                result = registered;
            }
        }
        return result;
    }

    private void executeMigration(int version, DbMigration migration, boolean up, Integer recordVersion) {
        var steps = up ? migration.collectUpSteps(datasource_) : migration.collectDownSteps(datasource_);
        manager_.inTransaction(() -> {
            for (var step : steps) {
                if (step instanceof Query query) {
                    manager_.executeUpdate(query);
                } else if (step instanceof String sql) {
                    manager_.executeUpdate(sql);
                } else if (step instanceof DbMigrationAction action) {
                    try {
                        action.execute(manager_);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new MigrationException("Error while executing an action step of migration " + version + ".", e);
                    }
                }
            }
            if (recordVersion != null) {
                writeVersion(recordVersion);
            }
        });
    }

    private void writeVersion(int version) {
        try {
            if (!stateWriter_.updateResource(stateResource_, String.valueOf(version))) {
                stateWriter_.addResource(stateResource_, String.valueOf(version));
            }
        } catch (ResourceWriterErrorException e) {
            throw new MigrationException("Unable to store the migration state in resource '" + stateResource_ + "'.", e);
        }
    }
}
