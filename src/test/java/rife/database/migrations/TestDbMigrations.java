/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.migrations.exceptions.IrreversibleMigrationException;
import rife.database.migrations.exceptions.MigrationException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Select;
import rife.resources.MemoryResources;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDbMigrations {
    public static class CreateUsers extends DbMigration {
        public void up() {
            add(createTable("migrate_users")
                .column("id", int.class, CreateTable.NOTNULL)
                .column("login", String.class, 30)
                .primaryKey("id"));
            add(createIndex("idx_migrate_users_login").table("migrate_users").column("login"));
        }

        public void down() {
            add(dropTable("migrate_users"));
        }
    }

    public static class AddEmail extends DbMigration {
        public void up() {
            add(alterTable("migrate_users").addColumn("email", String.class, 50));
            add(insert("migrate_users").field("id", 1).field("login", "gbevin").field("email", "gbevin@uwyn.com"));
        }

        public void down() {
            add(alterTable("migrate_users").dropColumn("email"));
        }
    }

    public static class Irreversible extends DbMigration {
        public void up() {
            add(update("migrate_users").fieldCustom("login", "lower(login)"));
        }
    }

    private DbMigrations createMigrations(Datasource datasource) {
        return new DbMigrations(datasource)
            .add(1, new CreateUsers())
            .add(2, new AddEmail());
    }

    private void cleanup(Datasource datasource) {
        try {
            new DbQueryManager(datasource).executeUpdate(new DropTable(datasource).table("migrate_users"));
        } catch (DatabaseException e) {
            // no table left behind
        }
    }

    @Test
    void testRegistrationValidation() {
        var datasource = TestDatasources.H2;
        var migrations = new DbMigrations(datasource);

        try {
            migrations.add(0, new CreateUsers());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("positive"));
        }

        migrations.add(3, new CreateUsers());
        try {
            migrations.add(3, new AddEmail());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("strictly increasing"));
        }
        try {
            migrations.add(2, new AddEmail());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("strictly increasing"));
        }

        migrations.add(5, new AddEmail());
        assertEquals(List.of(3, 5), migrations.versions());
        assertEquals(List.of(5), migrations.pendingFrom(3));
    }

    @Test
    void testStepsOutsideCollection() {
        var migration = new CreateUsers();
        try {
            migration.up();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("datasource"));
        }
    }

    @Test
    void testStateRequired() {
        var migrations = createMigrations(TestDatasources.H2);
        try {
            migrations.migrate();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("state"));
        }
    }

    @Test
    void testReversibility() {
        assertTrue(new CreateUsers().isReversible());
        assertFalse(new Irreversible().isReversible());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMigrateFromStateless(Datasource datasource) {
        var migrations = createMigrations(datasource);
        try {
            assertEquals(2, migrations.migrateFrom(0));

            var manager = new DbQueryManager(datasource);
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource).from("migrate_users").field("count(*)")));

            // rolling back one step drops the email column again
            assertEquals(1, migrations.rollbackFrom(2));
            // rolling back the rest removes the table
            assertEquals(0, migrations.rollbackFrom(1, 0));
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMigrateWithState(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            assertEquals(0, migrations.currentVersion());
            assertEquals(List.of(1, 2), migrations.pending());

            assertEquals(2, migrations.migrate());
            assertEquals(2, migrations.currentVersion());
            assertEquals("2", resources.getContent("test/migrations/version"));
            assertTrue(migrations.pending().isEmpty());

            // migrating again does nothing
            assertEquals(2, migrations.migrate());

            assertEquals(1, migrations.rollback());
            assertEquals(1, migrations.currentVersion());
            assertEquals("1", resources.getContent("test/migrations/version"));

            assertEquals(0, migrations.rollback(0));
            assertEquals(0, migrations.currentVersion());
        } catch (rife.resources.exceptions.ResourceFinderErrorException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testPartialMigrateAndTargets(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            assertEquals(1, migrations.migrate(1));
            assertEquals(1, migrations.currentVersion());
            assertEquals(List.of(2), migrations.pending());

            assertEquals(2, migrations.migrate());
            assertEquals(2, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testActionAndSqlSteps(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new AddEmail())
            .add(3, new DbMigration() {
                public void up() {
                    add("UPDATE migrate_users SET login = 'erik'");
                    add(manager -> {
                        var count = manager.executeGetFirstInt(new Select(manager.getDatasource()).from("migrate_users").field("count(*)"));
                        if (count != 1) {
                            throw new IllegalStateException("unexpected row count " + count);
                        }
                    });
                }
            });
        try {
            assertEquals(3, migrations.migrate());
            var manager = new DbQueryManager(datasource);
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource).from("migrate_users").field("count(*)").where("login", "=", "erik")));
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testIrreversibleRollback() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new Irreversible());
        try {
            assertEquals(2, migrations.migrate());
            try {
                migrations.rollback(0);
                fail();
            } catch (IrreversibleMigrationException e) {
                assertEquals(2, e.getVersion());
            }
            // the version is untouched by the refused rollback
            assertEquals(2, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testFailedMigrationKeepsVersion() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add("THIS IS NOT VALID SQL");
                }
            });
        try {
            try {
                migrations.migrate();
                fail();
            } catch (DatabaseException e) {
                // expected
            }
            // the first migration was recorded, the failed one wasn't
            assertEquals(1, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testCorruptState() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        resources.addResource("test/migrations/version", "not a number");
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            migrations.currentVersion();
            fail();
        } catch (MigrationException e) {
            assertTrue(e.getMessage().contains("not a number"));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBaseline(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            // the existing schema corresponds to version 1, only the later
            // migration is applied
            new DbQueryManager(datasource).executeUpdate(new CreateTable(datasource)
                .table("migrate_users")
                .column("id", int.class, CreateTable.NOTNULL)
                .column("login", String.class, 30)
                .primaryKey("id"));

            assertEquals(1, migrations.baseline(1));
            assertEquals(1, migrations.currentVersion());
            assertEquals(List.of(2), migrations.pending());

            assertEquals(2, migrations.migrate());
            assertEquals(2, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }

        try {
            migrations.baseline(4);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("registered"));
        }
    }

    @Test
    void testPreview() {
        var datasource = TestDatasources.H2;
        var migrations = new DbMigrations(datasource)
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add("UPDATE migrate_users SET login = 'erik'");
                    add(manager -> { /* no-op */ });
                }
            });

        var preview = migrations.previewFrom(0);
        assertEquals(6, preview.size());
        assertEquals("-- migration 1", preview.get(0));
        assertTrue(preview.get(1).startsWith("CREATE TABLE migrate_users"));
        assertTrue(preview.get(2).startsWith("CREATE INDEX idx_migrate_users_login"));
        assertEquals("-- migration 2", preview.get(3));
        assertEquals("UPDATE migrate_users SET login = 'erik'", preview.get(4));
        assertEquals("-- java action", preview.get(5));

        // nothing was executed
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstInt(new Select(datasource).from("migrate_users").field("count(*)"));
            fail();
        } catch (DatabaseException e) {
            // the table doesn't exist
        }

        // a partial preview only covers the requested range
        assertEquals(3, migrations.previewFrom(1).size());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFailureStopsLaterMigrations(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add("THIS IS NOT VALID SQL");
                }
            })
            .add(3, new DbMigration() {
                public void up() {
                    add(createTable("migrate_should_not_exist").column("id", int.class));
                }
            });
        try {
            try {
                migrations.migrate();
                fail();
            } catch (DatabaseException e) {
                // expected
            }
            // the version stays at the last completed migration and the
            // migrations after the failed one never ran
            assertEquals(1, migrations.currentVersion());
            var manager = new DbQueryManager(datasource);
            try {
                manager.executeGetFirstInt(new Select(datasource).from("migrate_should_not_exist").field("count(*)"));
                fail();
            } catch (DatabaseException e) {
                // the table doesn't exist
            }
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFailedMigrationRollsBackItsDataSteps(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add(insert("migrate_users").field("id", 1).field("login", "gbevin"));
                    add("THIS IS NOT VALID SQL");
                }
            });
        try {
            try {
                migrations.migrate();
                fail();
            } catch (DatabaseException e) {
                // expected
            }
            // the data step of the failed migration was rolled back with
            // its transaction
            assertEquals(1, migrations.currentVersion());
            var manager = new DbQueryManager(datasource);
            assertEquals(0, manager.executeGetFirstInt(new Select(datasource).from("migrate_users").field("count(*)")));
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFailingActionKeepsVersion(Datasource datasource) {
        var resources = new MemoryResources();
        var checked = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add(manager -> {
                        throw new java.io.IOException("action gone wrong");
                    });
                }
            });
        try {
            // a checked exception is wrapped in a migration exception
            try {
                checked.migrate();
                fail();
            } catch (MigrationException e) {
                assertTrue(e.getCause() instanceof java.io.IOException);
            }
            assertEquals(1, checked.currentVersion());

            // a runtime exception propagates as-is
            var runtime = new DbMigrations(datasource)
                .state(resources, "test/migrations/version")
                .add(1, new CreateUsers())
                .add(2, new DbMigration() {
                    public void up() {
                        add(manager -> {
                            throw new IllegalStateException("runtime gone wrong");
                        });
                    }
                });
            try {
                runtime.migrate();
                fail();
            } catch (IllegalStateException e) {
                assertEquals("runtime gone wrong", e.getMessage());
            }
            assertEquals(1, runtime.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testFailureDuringRollbackKeepsVersion() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add(alterTable("migrate_users").addColumn("email", String.class, 50));
                }

                public void down() {
                    add("THIS IS NOT VALID SQL");
                }
            });
        try {
            assertEquals(2, migrations.migrate());
            try {
                migrations.rollback(0);
                fail();
            } catch (DatabaseException e) {
                // expected
            }
            // the failed rollback leaves the version untouched
            assertEquals(2, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFailingStateWriteRollsBackSteps(Datasource datasource) {
        var resources = new MemoryResources();
        var failing_writer = new rife.resources.ResourceWriter() {
            public void addResource(String name, String content)
            throws rife.resources.exceptions.ResourceWriterErrorException {
                throw new rife.resources.exceptions.ResourceWriterErrorException("state write refused", null);
            }

            public boolean updateResource(String name, String content)
            throws rife.resources.exceptions.ResourceWriterErrorException {
                throw new rife.resources.exceptions.ResourceWriterErrorException("state write refused", null);
            }

            public boolean removeResource(String name) {
                return false;
            }
        };
        var setup = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers());
        var migrations = new DbMigrations(datasource)
            .state(resources, failing_writer, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, new DbMigration() {
                public void up() {
                    add(insert("migrate_users").field("id", 1).field("login", "gbevin"));
                }
            });
        try {
            assertEquals(1, setup.migrate());
            try {
                migrations.migrate();
                fail();
            } catch (MigrationException e) {
                assertTrue(e.getMessage().contains("state"));
            }
            // the version wasn't recorded and the migration's data steps
            // were rolled back with its transaction
            assertEquals(1, setup.currentVersion());
            var manager = new DbQueryManager(datasource);
            assertEquals(0, manager.executeGetFirstInt(new Select(datasource).from("migrate_users").field("count(*)")));
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testResumeAfterFixingFailure() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var broken = new boolean[]{true};
        DbMigration flaky = new DbMigration() {
            public void up() {
                if (broken[0]) {
                    add("THIS IS NOT VALID SQL");
                } else {
                    add(alterTable("migrate_users").addColumn("email", String.class, 50));
                }
            }
        };
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new CreateUsers())
            .add(2, flaky);
        try {
            try {
                migrations.migrate();
                fail();
            } catch (DatabaseException e) {
                // expected
            }
            assertEquals(1, migrations.currentVersion());

            // once the migration is fixed, migrate picks up where it stopped
            broken[0] = false;
            assertEquals(2, migrations.migrate());
            assertEquals(2, migrations.currentVersion());
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testDirectionValidation() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            assertEquals(2, migrations.migrate());

            // migrating downwards and rolling back upwards are refused
            try {
                migrations.migrate(1);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("rollback"));
            }
            assertEquals(1, migrations.rollback());
            try {
                migrations.rollback(2);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("migrate"));
            }
            try {
                migrations.migrateFrom(2, 1);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("rollback"));
            }
            try {
                migrations.rollbackFrom(1, 2);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("migrate"));
            }
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testEdgeSemantics() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();

        // an empty migration set is inert
        var empty = new DbMigrations(datasource)
            .state(resources, "test/migrations/version");
        assertEquals(0, empty.migrate());
        assertEquals(0, empty.rollback());
        assertTrue(empty.versions().isEmpty());
        assertEquals(5, empty.migrateFrom(5));
        assertEquals(5, empty.rollbackFrom(5));

        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");
        try {
            // a target above the highest registered version applies
            // everything that is registered
            assertEquals(2, migrations.migrate(7));
            assertEquals(2, migrations.currentVersion());

            // migrating to the current version is a no-op
            assertEquals(2, migrations.migrate(2));
            // rolling back to the current version is a no-op
            assertEquals(2, migrations.rollback(2));

            // a stateless current version between registered versions
            // rolls back the highest registered one below it
            assertEquals(0, migrations.rollback(0));
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testStateResourceDetails() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");

        // surrounding whitespace in the state content is tolerated
        resources.addResource("test/migrations/version", " 2 ");
        assertEquals(2, migrations.currentVersion());

        // a negative version is refused
        resources.updateResource("test/migrations/version", "-3");
        try {
            migrations.currentVersion();
            fail();
        } catch (MigrationException e) {
            assertTrue(e.getMessage().contains("negative"));
        }

        // a version beyond the registered ones is preserved as-is
        resources.updateResource("test/migrations/version", "9");
        assertEquals(9, migrations.currentVersion());
        assertTrue(migrations.pending().isEmpty());
    }

    @Test
    void testPreviewTouchesNoState() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");

        assertEquals(6, migrations.preview().size());
        try {
            assertNull(resources.getContent("test/migrations/version"));
        } catch (rife.resources.exceptions.ResourceFinderErrorException e) {
            // the resource doesn't exist, no state was written
        }
        assertEquals(0, migrations.currentVersion());
    }

    @Test
    void testRebaseline() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = createMigrations(datasource)
            .state(resources, "test/migrations/version");

        assertEquals(2, migrations.baseline(2));
        assertEquals(2, migrations.currentVersion());
        assertEquals(1, migrations.baseline(1));
        assertEquals(1, migrations.currentVersion());
        assertEquals(0, migrations.baseline(0));
        assertEquals(0, migrations.currentVersion());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIndependentSequences(Datasource datasource) {
        var resources = new MemoryResources();
        var first = new DbMigrations(datasource)
            .state(resources, "test/migrations/first")
            .add(1, new CreateUsers());
        var second = new DbMigrations(datasource)
            .state(resources, "test/migrations/second")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("migrate_other").column("id", int.class));
                }

                public void down() {
                    add(dropTable("migrate_other"));
                }
            });
        try {
            assertEquals(1, first.migrate());
            assertEquals(1, second.migrate());
            assertEquals(1, first.currentVersion());
            assertEquals(1, second.currentVersion());

            // rolling one sequence back doesn't affect the other
            assertEquals(0, second.rollback(0));
            assertEquals(1, first.currentVersion());
            assertEquals(0, second.currentVersion());
        } finally {
            cleanup(datasource);
            try {
                new DbQueryManager(datasource).executeUpdate(new DropTable(datasource).table("migrate_other"));
            } catch (DatabaseException e) {
                // no table left behind
            }
        }
    }

    @Test
    void testRollbackStepOrder() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var order = new java.util.ArrayList<String>();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new DbMigration() {
                public void up() {
                    add(manager -> order.add("up 1"));
                }

                public void down() {
                    add(manager -> order.add("down 1"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(manager -> order.add("up 2"));
                }

                public void down() {
                    add(manager -> order.add("down 2"));
                }
            });

        migrations.migrate();
        migrations.rollback(0);
        // migrations apply in ascending order and roll back in
        // descending order
        assertEquals(List.of("up 1", "up 2", "down 2", "down 1"), order);
    }

    public static abstract class ReversibleBase extends DbMigration {
        public void down() {
            add(dropTable("whatever"));
        }
    }

    @Test
    void testReversibilityThroughInheritance() {
        var inherited = new ReversibleBase() {
            public void up() {
            }
        };
        // a down that is inherited from an intermediate class still makes
        // the migration reversible
        assertTrue(inherited.isReversible());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTruncateAndDeleteFactories(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("migrate_logs").column("id", int.class));
                    add(insert("migrate_logs").field("id", 1));
                    add(insert("migrate_logs").field("id", 2));
                    add(insert("migrate_logs").field("id", 3));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // a targeted removal followed by a full cleanup
                    add(delete("migrate_logs").where("id", "=", 3));
                    add(truncate("migrate_logs"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            assertEquals(3, manager.executeGetFirstInt(new Select(datasource).from("migrate_logs").field("count(*)")));
            assertEquals(2, migrations.migrate());
            assertEquals(0, manager.executeGetFirstInt(new Select(datasource).from("migrate_logs").field("count(*)")));
        } finally {
            try {
                manager.executeUpdate(new DropTable(datasource).table("migrate_logs"));
            } catch (DatabaseException e) {
                // no table left behind
            }
        }
    }

    @Test
    void testSequenceFactories() {
        // sequences aren't supported by every database, H2 is enough to
        // exercise the factories
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "test/migrations/version")
            .add(1, new DbMigration() {
                public void up() {
                    add(createSequence("migrate_seq"));
                }

                public void down() {
                    add(dropSequence("migrate_seq"));
                }
            });
        assertEquals(1, migrations.migrate());
        assertEquals(0, migrations.rollback(0));
    }

    @Test
    void testMigrateFromPartialTargets() {
        var datasource = TestDatasources.H2;
        var migrations = createMigrations(datasource);
        try {
            // the stateless primitive respects an explicit target
            assertEquals(1, migrations.migrateFrom(0, 1));
            assertEquals(2, migrations.migrateFrom(1, 2));
        } finally {
            cleanup(datasource);
        }
    }

    @Test
    void testConfiguredStateResource() {
        var previous = RifeConfig.migrations().getStateResource();
        try {
            RifeConfig.migrations().setStateResource("custom/version");
            assertEquals("custom/version", RifeConfig.migrations().getStateResource());

            var datasource = TestDatasources.H2;
            var resources = new MemoryResources();
            var migrations = new DbMigrations(datasource)
                .state(resources)
                .add(1, new CreateUsers());
            try {
                assertEquals(1, migrations.migrate());
                assertEquals("1", resources.getContent("custom/version"));
            } catch (rife.resources.exceptions.ResourceFinderErrorException e) {
                throw new RuntimeException(e);
            } finally {
                cleanup(datasource);
            }

            try {
                RifeConfig.migrations().setStateResource("");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("empty"));
            }
        } finally {
            RifeConfig.migrations().setStateResource(previous);
        }
    }

    @Test
    void testStepArgumentValidation() {
        var datasource = TestDatasources.H2;
        var migrations = new DbMigrations(datasource)
            .add(1, new DbMigration() {
                public void up() {
                    add((rife.database.queries.Query) null);
                }
            });
        try {
            migrations.migrateFrom(0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("query"));
        }

        var sql_migrations = new DbMigrations(datasource)
            .add(1, new DbMigration() {
                public void up() {
                    add("");
                }
            });
        try {
            sql_migrations.migrateFrom(0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("empty"));
        }
    }

    @Test
    void testDirectoryResourcesState()
    throws Exception {
        var datasource = TestDatasources.H2;
        var directory = java.nio.file.Files.createTempDirectory("migrationsstate").toFile();
        try {
            // the state lives in a plain file on the file system
            var migrations = createMigrations(datasource)
                .state(new rife.resources.DirectoryResources(directory), "state/version");
            assertEquals(2, migrations.migrate());
            assertEquals("2", rife.tools.FileUtils.readString(new java.io.File(directory, "state" + java.io.File.separator + "version")));

            // a fresh instance over the same directory picks the state up
            var resumed = createMigrations(datasource)
                .state(new rife.resources.DirectoryResources(directory), "state/version");
            assertEquals(2, resumed.currentVersion());
            assertTrue(resumed.pending().isEmpty());
            assertEquals(1, resumed.rollback());
            assertEquals("1", rife.tools.FileUtils.readString(new java.io.File(directory, "state" + java.io.File.separator + "version")));
        } finally {
            cleanup(datasource);
            rife.tools.FileUtils.deleteDirectory(directory);
        }
    }

    @Test
    void testDefaultStateResource() {
        assertEquals("rife/migrations/version", RifeConfig.MigrationsConfig.DEFAULT_STATE_RESOURCE);
        assertEquals("rife/migrations/version", RifeConfig.migrations().getStateResource());

        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources)
            .add(1, new CreateUsers());
        try {
            assertEquals(1, migrations.migrate());
            assertEquals("1", resources.getContent(RifeConfig.migrations().getStateResource()));
        } catch (rife.resources.exceptions.ResourceFinderErrorException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup(datasource);
        }
    }
}
