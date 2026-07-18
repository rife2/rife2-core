/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerColumnMapping {
    public static class MappedIdBean extends MetaData {
        private int userId_ = -1;
        private String firstName_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("userId").identifier(true).notNull(true).columnName("user_id"));
            addConstraint(new ConstrainedProperty("firstName").columnName("first_name").precision(50));
        }

        public void setUserId(int userId) {
            userId_ = userId;
        }

        public int getUserId() {
            return userId_;
        }

        public void setFirstName(String firstName) {
            firstName_ = firstName;
        }

        public String getFirstName() {
            return firstName_;
        }
    }

    public static class FriendlyBean extends MetaData {
        private int id_ = -1;
        private String name_ = null;
        private MappedIdBean friend_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).notNull(true));
            addConstraint(new ConstrainedProperty("name").precision(30));
            addConstraint(new ConstrainedProperty("friend").manyToOne());
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

        public void setFriend(MappedIdBean friend) {
            friend_ = friend;
        }

        public MappedIdBean getFriend() {
            return friend_;
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMappedIdentifierCrudCycle(Datasource datasource) {
        var manager = GenericQueryManagerFactory.instance(datasource, MappedIdBean.class);
        assertEquals("userId", manager.getIdentifierName());
        assertEquals("user_id", manager.getIdentifierColumn());

        manager.install();
        try {
            var bean = new MappedIdBean();
            bean.setFirstName("Erik");
            var id = manager.save(bean);
            assertTrue(id >= 0);

            // the bean is restored through the mapped identifier column
            var restored = manager.restore(id);
            assertNotNull(restored);
            assertEquals(id, restored.getUserId());
            assertEquals("Erik", restored.getFirstName());
            assertEquals(1, manager.count());

            // saving an existing bean goes through the update path with
            // the mapped identifier in the where clause
            restored.setFirstName("Geert");
            assertEquals(id, manager.save(restored));
            assertEquals("Geert", manager.restore(id).getFirstName());
            assertEquals(1, manager.count());

            // custom restore queries use the column names
            var query = manager.getRestoreQuery()
                .where("first_name", "=", "Geert");
            var list = manager.restore(query);
            assertEquals(1, list.size());
            assertEquals(id, list.get(0).getUserId());

            // the bean is deleted through the mapped identifier column
            assertTrue(manager.delete(id));
            assertEquals(0, manager.count());
        } finally {
            manager.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMappedUniqueValidation(Datasource datasource) {
        var manager = GenericQueryManagerFactory.instance(datasource, UniqueMappedBean.class);
        manager.install();
        try {
            var first = new UniqueMappedBean();
            first.setLogin("gbevin");
            manager.save(first);

            // the uniqueness validation queries through the mapped column
            var second = new UniqueMappedBean();
            second.setLogin("gbevin");
            manager.validate(second);
            assertFalse(second.getValidationErrors().isEmpty());
            assertEquals("uniqueness", second.getValidationErrors().iterator().next().getIdentifier().toLowerCase());

            var third = new UniqueMappedBean();
            third.setLogin("ethauvin");
            manager.validate(third);
            assertTrue(third.getValidationErrors().isEmpty());
        } finally {
            manager.remove();
        }
    }

    public static class UniqueMappedBean extends MetaData {
        private int id_ = -1;
        private String login_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).notNull(true));
            addConstraint(new ConstrainedProperty("login").columnName("login_name").unique(true).precision(30));
        }

        public void setId(int id) {
            id_ = id;
        }

        public int getId() {
            return id_;
        }

        public void setLogin(String login) {
            login_ = login;
        }

        public String getLogin() {
            return login_;
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testManyToOneWithMappedIdentifier(Datasource datasource) {
        var friend_manager = GenericQueryManagerFactory.instance(datasource, MappedIdBean.class);
        var main_manager = GenericQueryManagerFactory.instance(datasource, FriendlyBean.class);

        friend_manager.install();
        main_manager.install();
        try {
            var friend = new MappedIdBean();
            friend.setFirstName("Erik");
            friend_manager.save(friend);

            var bean = new FriendlyBean();
            bean.setName("Geert");
            bean.setFriend(friend);
            var id = main_manager.save(bean);

            // the association joins against the mapped identifier column
            // of the friend table
            var restored = main_manager.restore(id);
            assertNotNull(restored);
            assertEquals("Geert", restored.getName());
            assertNotNull(restored.getFriend());
            assertEquals("Erik", restored.getFriend().getFirstName());
            assertEquals(friend.getUserId(), restored.getFriend().getUserId());
        } finally {
            main_manager.remove();
            friend_manager.remove();
        }
    }
}
