/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.RowProcessor;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.migrations.exceptions.MigrationException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.DropView;
import rife.database.queries.Select;
import rife.database.queries.Update;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.resources.MemoryResources;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real world migration scenarios, written as the evolution stories that
 * applications actually go through.
 */
public class TestDbMigrationsScenarios {
    private void drop(Datasource datasource, String... tables) {
        var manager = new DbQueryManager(datasource);
        for (var table : tables) {
            try {
                manager.executeUpdate(new DropTable(datasource).table(table));
            } catch (DatabaseException e) {
                // nothing left behind
            }
        }
    }

    private void dropView(Datasource datasource, String view) {
        try {
            new DbQueryManager(datasource).executeUpdate(new DropView(datasource).view(view));
        } catch (DatabaseException e) {
            // nothing left behind
        }
    }

    /**
     * The classic application evolution arc: create a table, relate a
     * second one to it, add a flag with a backfill, index a hot column,
     * rename a column, and widen a column that turned out to be too
     * small, with live data surviving every step.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testBlogEvolution(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/blog")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("blog_users")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("login", String.class, 30)
                        .primaryKey("id"));
                }

                public void down() {
                    add(dropTable("blog_users"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(createTable("blog_posts")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("author_id", int.class)
                        .column("title", String.class, 30)
                        .primaryKey("id")
                        .foreignKey("blog_users", "author_id", "id"));
                }

                public void down() {
                    add(dropTable("blog_posts"));
                }
            })
            .add(3, new DbMigration() {
                public void up() {
                    // the flag is added and every existing post is
                    // backfilled as published
                    add(alterTable("blog_posts").addColumn("published", int.class));
                    add(update("blog_posts").field("published", 1));
                }

                public void down() {
                    add(alterTable("blog_posts").dropColumn("published"));
                }
            })
            .add(4, new DbMigration() {
                public void up() {
                    add(createIndex("idx_blog_posts_author").table("blog_posts").column("author_id"));
                }

                public void down() {
                    add(dropIndex("idx_blog_posts_author").table("blog_posts"));
                }
            })
            .add(5, new DbMigration() {
                public void up() {
                    add(alterTable("blog_posts").renameColumn("title", "headline"));
                }

                public void down() {
                    add(alterTable("blog_posts").renameColumn("headline", "title"));
                }
            })
            .add(6, new DbMigration() {
                public void up() {
                    // 30 characters turned out to be too small
                    add(alterTable("blog_posts").alterColumnType("headline", String.class, 150));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            // the first release ships with users and posts
            assertEquals(2, migrations.migrate(2));
            manager.executeUpdate("INSERT INTO blog_users (id, login) VALUES (1, 'gbevin')");
            manager.executeUpdate("INSERT INTO blog_posts (id, author_id, title) VALUES (1, 1, 'Welcome')");

            // the second release adds the publication flag, the index,
            // the rename and the widening, existing data survives
            assertEquals(6, migrations.migrate());
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource)
                .from("blog_posts").field("count(*)").where("published", "=", 1)));
            assertEquals("Welcome", manager.executeGetFirstString(new Select(datasource)
                .from("blog_posts").field("headline").where("id", "=", 1)));

            // the widened column accepts headlines that didn't fit before
            manager.executeUpdate("INSERT INTO blog_posts (id, author_id, headline, published) VALUES (2, 1, '" +
                                  "A headline that is much longer than the original thirty characters could ever hold" + "', 0)");
            assertEquals(2, manager.executeGetFirstInt(new Select(datasource)
                .from("blog_posts").field("count(*)")));
        } finally {
            drop(datasource, "blog_posts", "blog_users");
        }
    }

    /**
     * A release goes out, live data accumulates, the next release
     * migrates further, turns out to be bad and is rolled back, and the
     * data from before that release is still intact.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReleaseAndRollback(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/releases")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("rel_accounts")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("name", String.class, 40)
                        .primaryKey("id"));
                }

                public void down() {
                    add(dropTable("rel_accounts"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(alterTable("rel_accounts").addColumn("balance", int.class));
                    add(update("rel_accounts").field("balance", 0));
                }

                public void down() {
                    add(alterTable("rel_accounts").dropColumn("balance"));
                }
            })
            .add(3, new DbMigration() {
                public void up() {
                    add(createTable("rel_audit")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("account_id", int.class)
                        .primaryKey("id"));
                }

                public void down() {
                    add(dropTable("rel_audit"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            // release one is live and accumulates data
            assertEquals(2, migrations.migrate(2));
            manager.executeUpdate("INSERT INTO rel_accounts (id, name, balance) VALUES (1, 'Erik', 100)");

            // release two goes out and turns out to be bad
            assertEquals(3, migrations.migrate());
            assertEquals(3, migrations.currentVersion());

            // it's rolled back to the previous release, the live data of
            // release one is untouched
            assertEquals(2, migrations.rollback(2));
            assertEquals(100, manager.executeGetFirstInt(new Select(datasource)
                .from("rel_accounts").field("balance").where("id", "=", 1)));
        } finally {
            drop(datasource, "rel_audit", "rel_accounts");
        }
    }

    /**
     * An existing hand-managed database is adopted into the migration
     * sequence with a baseline, only later migrations are applied and
     * the legacy data is untouched.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAdoptLegacyDatabase(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/legacy")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("legacy_orders")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("amount", int.class)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(alterTable("legacy_orders").addColumn("status", String.class, 20));
                    add(update("legacy_orders").field("status", "processed"));
                }
            });
        try {
            // the database predates the migrations and already contains
            // what migration 1 describes, including data
            manager.executeUpdate(new CreateTable(datasource)
                .table("legacy_orders")
                .column("id", int.class, CreateTable.NOTNULL)
                .column("amount", int.class)
                .primaryKey("id"));
            manager.executeUpdate("INSERT INTO legacy_orders (id, amount) VALUES (1, 250)");

            // baselining declares that state, only migration 2 is pending
            assertEquals(1, migrations.baseline(1));
            assertEquals(List.of(2), migrations.pending());

            assertEquals(2, migrations.migrate());
            assertEquals(250, manager.executeGetFirstInt(new Select(datasource)
                .from("legacy_orders").field("amount").where("id", "=", 1)));
            assertEquals("processed", manager.executeGetFirstString(new Select(datasource)
                .from("legacy_orders").field("status").where("id", "=", 1)));
        } finally {
            drop(datasource, "legacy_orders");
        }
    }

    public static class Customer extends MetaData {
        private int id_ = -1;
        private String name_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).sparse(true).notNull(true));
            addConstraint(new ConstrainedProperty("name").columnName("customer_name").precision(40));
        }

        public void setId(int id) {
            id_ = id;
        }

        public int getId() {
            return id_;
        }

        public void setName(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }
    }

    public static class CustomerWithEmail extends Customer {
        private String email_ = null;

        public void activateMetaData() {
            super.activateMetaData();
            addConstraint(new ConstrainedProperty("email").columnName("email_address").precision(60));
        }

        public void setEmail(String email) {
            email_ = email;
        }

        public String getEmail() {
            return email_;
        }
    }

    /**
     * The distinctively RIFE2 story: the schema is created from a
     * constrained bean with mapped column names, the generic query
     * manager works against it, the bean evolves with a new property,
     * the migration adds the mapped column from the bean metadata, and
     * the query manager keeps working with old and new data.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testConstrainedBeanEvolution(Datasource datasource) {
        final var table = "shop_customers";
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/shop")
            .add(1, new DbMigration() {
                public void up() {
                    // the table is derived from the constrained bean, the
                    // mapped column names and the identifier included
                    add(createTable(table).columns(Customer.class));
                }

                public void down() {
                    add(dropTable(table));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // the bean gained a property, its column definition
                    // comes from the bean metadata
                    add(alterTable(table).addColumn(CustomerWithEmail.class, "email"));
                }

                public void down() {
                    add(alterTable(table).dropColumn(CustomerWithEmail.class, "email"));
                }
            });
        try {
            // the first version of the application stores customers
            assertEquals(1, migrations.migrate(1));
            var manager_v1 = GenericQueryManagerFactory.instance(datasource, Customer.class, table);
            var customer = new Customer();
            customer.setId(1);
            customer.setName("Geert");
            manager_v1.save(customer);

            // the application evolves, the migration brings the schema
            // along and the stored data is still there
            assertEquals(2, migrations.migrate());
            var manager_v2 = GenericQueryManagerFactory.instance(datasource, CustomerWithEmail.class, table);
            var restored = manager_v2.restore(1);
            assertNotNull(restored);
            assertEquals("Geert", restored.getName());
            assertNull(restored.getEmail());

            // new data uses the new property through its mapped column
            var evolved = new CustomerWithEmail();
            evolved.setId(2);
            evolved.setName("Erik");
            evolved.setEmail("erik@thauvin.net");
            manager_v2.save(evolved);
            assertEquals("erik@thauvin.net", manager_v2.restore(2).getEmail());
        } finally {
            drop(datasource, table);
        }
    }

    /**
     * The zero-downtime rename dance: expand with the new column, copy
     * the data over, contract by dropping the old column in a later
     * migration, once no deployed application version reads it anymore.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExpandContract(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/expandcontract")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("ec_products")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("descr", String.class, 60)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // expand: the new column appears and receives the data
                    add(alterTable("ec_products").addColumn("description", String.class, 60));
                    add(update("ec_products").fieldCustom("description", "descr"));
                }
            })
            .add(3, new DbMigration() {
                public void up() {
                    // contract: the old column disappears
                    add(alterTable("ec_products").dropColumn("descr"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            manager.executeUpdate("INSERT INTO ec_products (id, descr) VALUES (1, 'A fine product')");

            // the expand release copies the data into the new column
            assertEquals(2, migrations.migrate(2));
            assertEquals("A fine product", manager.executeGetFirstString(new Select(datasource)
                .from("ec_products").field("description").where("id", "=", 1)));

            // the contract release removes the old column for good
            assertEquals(3, migrations.migrate());
            assertEquals("A fine product", manager.executeGetFirstString(new Select(datasource)
                .from("ec_products").field("description").where("id", "=", 1)));
        } finally {
            drop(datasource, "ec_products");
        }
    }

    /**
     * Reference data is seeded and evolved by migrations like any other
     * schema change.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReferenceDataSeeding(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/seeds")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("seed_statuses")
                        .column("code", String.class, 10)
                        .column("label", String.class, 30));
                    add(insert("seed_statuses").field("code", "new").field("label", "New"));
                    add(insert("seed_statuses").field("code", "done").field("label", "Done"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(insert("seed_statuses").field("code", "hold").field("label", "On hold"));
                    add(update("seed_statuses").field("label", "Completed").where("code", "=", "done"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(2, migrations.migrate());
            assertEquals(3, manager.executeGetFirstInt(new Select(datasource)
                .from("seed_statuses").field("count(*)")));
            assertEquals("Completed", manager.executeGetFirstString(new Select(datasource)
                .from("seed_statuses").field("label").where("code", "=", "done")));
        } finally {
            drop(datasource, "seed_statuses");
        }
    }

    /**
     * A reporting view is managed through migrations and replaced when
     * the underlying schema changes, using drop and create so that it
     * works on every database.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReportingViewLifecycle(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/views")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("view_articles")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("title", String.class, 60)
                        .column("published", int.class)
                        .primaryKey("id"));
                    add(createView("published_articles")
                        .as(new Select(datasource()).from("view_articles").field("id").field("title").where("published", "=", 1)));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // the schema evolves and the view is replaced to
                    // expose the new column
                    add(alterTable("view_articles").addColumn("author", String.class, 30));
                    add(dropView("published_articles"));
                    add(createView("published_articles")
                        .as(new Select(datasource()).from("view_articles").field("id").field("title").field("author").where("published", "=", 1)));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            manager.executeUpdate("INSERT INTO view_articles (id, title, published) VALUES (1, 'DDL support', 1)");
            manager.executeUpdate("INSERT INTO view_articles (id, title, published) VALUES (2, 'Draft', 0)");
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource)
                .from("published_articles").field("count(*)")));

            assertEquals(2, migrations.migrate());
            manager.executeUpdate(new Update(datasource)
                .table("view_articles").field("author", "gbevin").where("id", "=", 1));
            assertEquals("gbevin", manager.executeGetFirstString(new Select(datasource)
                .from("published_articles").field("author").where("id", "=", 1)));
        } finally {
            dropView(datasource, "published_articles");
            drop(datasource, "view_articles");
        }
    }

    /**
     * An embedded application versions its own schema with the state in
     * a plain file next to its data: every launch builds the same
     * migration list and calls migrate, which does nothing when the file
     * says the schema is current and applies what's new after an
     * upgrade, across application restarts.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEmbeddedApplicationFileState(Datasource datasource)
    throws Exception {
        var data_dir = java.nio.file.Files.createTempDirectory("embedded_app").toFile();
        try {
            // version one of the application launches for the first time
            var first_launch = new DbMigrations(datasource)
                .state(new rife.resources.DirectoryResources(data_dir), "schema/version")
                .add(1, new DbMigration() {
                    public void up() {
                        add(createTable("app_notes")
                            .column("id", int.class, CreateTable.NOTNULL)
                            .column("note", String.class, 100)
                            .primaryKey("id"));
                    }
                });
            assertEquals(1, first_launch.migrate());

            var manager = new DbQueryManager(datasource);
            manager.executeUpdate("INSERT INTO app_notes (id, note) VALUES (1, 'don''t forget')");

            // the application restarts, nothing is pending, nothing runs
            var second_launch = new DbMigrations(datasource)
                .state(new rife.resources.DirectoryResources(data_dir), "schema/version")
                .add(1, new DbMigration() {
                    public void up() {
                        add(createTable("app_notes")
                            .column("id", int.class, CreateTable.NOTNULL)
                            .column("note", String.class, 100)
                            .primaryKey("id"));
                    }
                });
            assertTrue(second_launch.pending().isEmpty());
            assertEquals(1, second_launch.migrate());

            // the application is upgraded to a version that knows one
            // more migration, the next launch applies just that one
            var upgraded_launch = new DbMigrations(datasource)
                .state(new rife.resources.DirectoryResources(data_dir), "schema/version")
                .add(1, new DbMigration() {
                    public void up() {
                        add(createTable("app_notes")
                            .column("id", int.class, CreateTable.NOTNULL)
                            .column("note", String.class, 100)
                            .primaryKey("id"));
                    }
                })
                .add(2, new DbMigration() {
                    public void up() {
                        add(alterTable("app_notes").addColumn("pinned", int.class));
                        add(update("app_notes").field("pinned", 0));
                    }
                });
            assertEquals(List.of(2), upgraded_launch.pending());
            assertEquals(2, upgraded_launch.migrate());

            // the user's notes survived both restarts and the upgrade
            assertEquals("don't forget", manager.executeGetFirstString(new Select(datasource)
                .from("app_notes").field("note").where("id", "=", 1)));
        } finally {
            drop(datasource, "app_notes");
            rife.tools.FileUtils.deleteDirectory(data_dir);
        }
    }

    /**
     * The DBA review workflow: the SQL of a pending release is generated
     * as a reviewable script before anything touches the database.
     */
    @Test
    void testPreviewForRelease() {
        var datasource = TestDatasources.H2;
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/preview")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("rev_customers")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("name", String.class, 40)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(alterTable("rev_customers").addColumn("email", String.class, 60));
                    add(createIndex("idx_rev_customers_email").table("rev_customers").column("email"));
                }
            });

        // release one is already deployed, the DBA reviews what release
        // two will execute
        migrations.baseline(1);
        var script = String.join("\n", migrations.preview());
        assertEquals("""
            -- migration 2
            ALTER TABLE rev_customers ADD COLUMN email VARCHAR(60)
            CREATE INDEX idx_rev_customers_email ON rev_customers (email)""", script);

        // nothing was executed by the preview
        assertEquals(1, migrations.currentVersion());
    }

    /**
     * A destructive migration protects the data with a guard: an action
     * step verifies that the column to drop no longer holds anything and
     * aborts the migration inside its transaction when it does. The
     * schema, the data and the version are untouched until the guard is
     * satisfied.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGuardedDestructiveMigration(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/guarded")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("guard_posts")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("body", String.class, 60)
                        .column("legacy_notes", String.class, 60)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // the guard refuses to destroy data that is still there
                    add(manager -> {
                        var count = manager.executeGetFirstInt(new Select(manager.getDatasource())
                            .from("guard_posts").field("count(*)").where("legacy_notes IS NOT NULL"));
                        if (count > 0) {
                            throw new MigrationException(count + " rows still hold legacy notes, refusing to drop the column");
                        }
                    });
                    add(alterTable("guard_posts").dropColumn("legacy_notes"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            manager.executeUpdate("INSERT INTO guard_posts (id, body, legacy_notes) VALUES (1, 'A post', 'do not lose me')");

            // the migration refuses while the data is still there
            try {
                migrations.migrate();
                fail();
            } catch (MigrationException e) {
                assertTrue(e.getMessage().contains("refusing"));
            }
            assertEquals(1, migrations.currentVersion());
            assertEquals("do not lose me", manager.executeGetFirstString(new Select(datasource)
                .from("guard_posts").field("legacy_notes").where("id", "=", 1)));

            // once the data has been dealt with, the same migration passes
            manager.executeUpdate(new Update(datasource)
                .table("guard_posts").fieldCustom("legacy_notes", "NULL"));
            assertEquals(2, migrations.migrate());
            assertEquals("A post", manager.executeGetFirstString(new Select(datasource)
                .from("guard_posts").field("body").where("id", "=", 1)));
        } finally {
            drop(datasource, "guard_posts");
        }
    }

    /**
     * Archiving instead of dropping: the "removal" of a table is a
     * rename that keeps the data queryable, the real drop only happens
     * in a later migration once nothing misses it.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testArchiveInsteadOfDrop(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/archive")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("arch_events")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("payload", String.class, 60)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    // the table is retired, not destroyed
                    add(alterTable("arch_events").renameTo("arch_events_archived"));
                }

                public void down() {
                    add(alterTable("arch_events_archived").renameTo("arch_events"));
                }
            })
            .add(3, new DbMigration() {
                public void up() {
                    // the archive is removed for good in its own release
                    add(dropTable("arch_events_archived"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            manager.executeUpdate("INSERT INTO arch_events (id, payload) VALUES (1, 'kept around')");

            // after the retirement the data is still queryable
            assertEquals(2, migrations.migrate(2));
            assertEquals("kept around", manager.executeGetFirstString(new Select(datasource)
                .from("arch_events_archived").field("payload").where("id", "=", 1)));

            // and the retirement can even be undone
            assertEquals(1, migrations.rollback(1));
            assertEquals("kept around", manager.executeGetFirstString(new Select(datasource)
                .from("arch_events").field("payload").where("id", "=", 1)));

            // the final release makes the removal permanent
            assertEquals(3, migrations.migrate());
            try {
                manager.executeGetFirstInt(new Select(datasource)
                    .from("arch_events_archived").field("count(*)"));
                fail();
            } catch (DatabaseException e) {
                // the archive is gone
            }
        } finally {
            drop(datasource, "arch_events", "arch_events_archived");
        }
    }

    /**
     * A data migration that needs Java logic: a single name column is
     * split into first and last names, row by row.
     */
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSplitColumnWithAction(Datasource datasource) {
        var resources = new MemoryResources();
        var migrations = new DbMigrations(datasource)
            .state(resources, "scenarios/split")
            .add(1, new DbMigration() {
                public void up() {
                    add(createTable("split_people")
                        .column("id", int.class, CreateTable.NOTNULL)
                        .column("full_name", String.class, 60)
                        .primaryKey("id"));
                }
            })
            .add(2, new DbMigration() {
                public void up() {
                    add(alterTable("split_people").addColumn("first_name", String.class, 30));
                    add(alterTable("split_people").addColumn("last_name", String.class, 30));
                    add(manager -> {
                        var datasource = manager.getDatasource();
                        var people = new ArrayList<String[]>();
                        manager.executeFetchAll(new Select(datasource).from("split_people").field("id").field("full_name"),
                            (RowProcessor) rs -> people.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("full_name")}));
                        for (var person : people) {
                            var space = person[1].indexOf(' ');
                            var first = space < 0 ? person[1] : person[1].substring(0, space);
                            var last = space < 0 ? "" : person[1].substring(space + 1);
                            manager.executeUpdate(new Update(datasource)
                                .table("split_people")
                                .field("first_name", first)
                                .field("last_name", last)
                                .where("id", "=", Integer.parseInt(person[0])));
                        }
                    });
                    add(alterTable("split_people").dropColumn("full_name"));
                }
            });
        var manager = new DbQueryManager(datasource);
        try {
            assertEquals(1, migrations.migrate(1));
            manager.executeUpdate("INSERT INTO split_people (id, full_name) VALUES (1, 'Geert Bevin')");
            manager.executeUpdate("INSERT INTO split_people (id, full_name) VALUES (2, 'Erik C. Thauvin')");

            assertEquals(2, migrations.migrate());
            assertEquals("Geert", manager.executeGetFirstString(new Select(datasource)
                .from("split_people").field("first_name").where("id", "=", 1)));
            assertEquals("Bevin", manager.executeGetFirstString(new Select(datasource)
                .from("split_people").field("last_name").where("id", "=", 1)));
            assertEquals("C. Thauvin", manager.executeGetFirstString(new Select(datasource)
                .from("split_people").field("last_name").where("id", "=", 2)));
        } finally {
            drop(datasource, "split_people");
        }
    }
}
