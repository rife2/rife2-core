/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.BeanException;
import rife.database.exceptions.DbQueryException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.database.queries.Update;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import static org.junit.jupiter.api.Assertions.*;

public class TestColumnNameMapping {
    public static class MappedBean extends Validation {
        private int identifier_ = -1;
        private String firstName_ = null;
        private String email_ = null;

        protected void activateValidation() {
            addConstraint(new ConstrainedProperty("identifier").identifier(true).notNull(true));
            addConstraint(new ConstrainedProperty("firstName").columnName("first_name").precision(50));
            addConstraint(new ConstrainedProperty("email").precision(50));
        }

        public void setIdentifier(int identifier) {
            identifier_ = identifier;
        }

        public int getIdentifier() {
            return identifier_;
        }

        public void setFirstName(String firstName) {
            firstName_ = firstName;
        }

        public String getFirstName() {
            return firstName_;
        }

        public void setEmail(String email) {
            email_ = email;
        }

        public String getEmail() {
            return email_;
        }
    }

    public static class QuotedBean extends Validation {
        private int identifier_ = -1;
        private String total_ = null;

        protected void activateValidation() {
            addConstraint(new ConstrainedProperty("identifier").identifier(true).notNull(true));
            addConstraint(new ConstrainedProperty("total").columnName("\"total-count\"").precision(30));
        }

        public void setIdentifier(int identifier) {
            identifier_ = identifier;
        }

        public int getIdentifier() {
            return identifier_;
        }

        public void setTotal(String total) {
            total_ = total;
        }

        public String getTotal() {
            return total_;
        }
    }

    public static class DuplicateBean extends Validation {
        private String one_ = null;
        private String two_ = null;

        protected void activateValidation() {
            addConstraint(new ConstrainedProperty("one").columnName("same_col"));
            addConstraint(new ConstrainedProperty("two").columnName("same_col"));
        }

        public void setOne(String one) {
            one_ = one;
        }

        public String getOne() {
            return one_;
        }

        public void setTwo(String two) {
            two_ = two;
        }

        public String getTwo() {
            return two_;
        }
    }

    @Test
    void testGeneratedSqlUsesColumnNames() {
        var datasource = TestDatasources.H2;

        // the create table statement emits the mapped column names
        var create = new CreateTable(datasource);
        create.table("mapped").columns(MappedBean.class);
        var create_sql = create.getSql();
        assertTrue(create_sql.contains("first_name"), create_sql);
        assertFalse(create_sql.contains("firstName"), create_sql);
        assertTrue(create_sql.contains("email"), create_sql);

        // the insert emits the mapped column names while the parameters
        // keep the property names so that beans can provide the values
        var insert = new Insert(datasource);
        insert.into("mapped").fieldsParameters(MappedBean.class);
        var insert_sql = insert.getSql();
        assertTrue(insert_sql.contains("first_name"), insert_sql);
        assertFalse(insert_sql.contains("firstName"), insert_sql);
        assertTrue(insert.getParameters().getOrderedNames().contains("firstName"));

        // the update emits the mapped column names with property parameters
        var update = new Update(datasource);
        update.table("mapped").fieldsParameters(MappedBean.class);
        var update_sql = update.getSql();
        assertTrue(update_sql.contains("first_name"), update_sql);
        assertFalse(update_sql.contains("firstName"), update_sql);
        assertTrue(update.getParameters().getOrderedNames().contains("firstName"));

        // the select field list emits the mapped column names
        var select = new Select(datasource);
        select.from("mapped").fields(MappedBean.class);
        var select_sql = select.getSql();
        assertTrue(select_sql.contains("first_name"), select_sql);
        assertFalse(select_sql.contains("firstName"), select_sql);

        // the where clauses that are generated from beans emit the mapped
        // column names with property parameters
        var select_where = new Select(datasource);
        select_where.from("mapped").whereParameters(MappedBean.class);
        var where_sql = select_where.getSql();
        assertTrue(where_sql.contains("first_name"), where_sql);
        assertFalse(where_sql.contains("firstName"), where_sql);
        assertTrue(select_where.getParameters().getOrderedNames().contains("firstName"));
    }

    @Test
    void testQuotedColumnNamesArePreserved() {
        var datasource = TestDatasources.H2;

        // a quoted column name is emitted verbatim
        var create = new CreateTable(datasource);
        create.table("quoted").columns(QuotedBean.class);
        assertTrue(create.getSql().contains("\"total-count\""), create.getSql());
    }

    @Test
    void testDuplicateColumnNamesAreRejected() {
        var datasource = TestDatasources.H2;

        // several properties can never map to the same column
        var create = new CreateTable(datasource);
        assertThrows(DbQueryException.class, () -> create.table("dup").columns(DuplicateBean.class));
        assertThrows(BeanException.class, () -> new DbBeanFetcher<>(datasource, DuplicateBean.class));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMappedColumnsRoundtrip(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        var create = new CreateTable(datasource);
        create.table("tmp_columnmapping").columns(MappedBean.class);
        try {
            manager.executeUpdate(create);

            var bean = new MappedBean();
            bean.setIdentifier(42);
            bean.setFirstName("Geert");
            bean.setEmail("gbevin@uwyn.com");

            var insert = new Insert(datasource);
            insert.into("tmp_columnmapping").fields(bean);
            assertEquals(1, manager.executeUpdate(insert));

            // the bean is restored with the mapped column translated back
            // to its property
            var select = new Select(datasource);
            select.from("tmp_columnmapping").fields(MappedBean.class).where("identifier", "=", 42);
            var fetcher = new DbBeanFetcher<>(datasource, MappedBean.class);
            manager.executeFetchFirst(select, fetcher);
            var restored = fetcher.getBeanInstance();
            assertNotNull(restored);
            assertEquals(42, restored.getIdentifier());
            assertEquals("Geert", restored.getFirstName());
            assertEquals("gbevin@uwyn.com", restored.getEmail());

            // a where clause generated from the bean matches through the
            // mapped column
            var select_where = new Select(datasource);
            select_where.from("tmp_columnmapping").fields(MappedBean.class).where(bean);
            var fetcher_where = new DbBeanFetcher<>(datasource, MappedBean.class);
            manager.executeFetchFirst(select_where, fetcher_where);
            assertNotNull(fetcher_where.getBeanInstance());
            assertEquals("Geert", fetcher_where.getBeanInstance().getFirstName());
        } finally {
            manager.executeUpdate(new DropTable(datasource).table("tmp_columnmapping"));
        }
    }

    @Test
    void testQuotedColumnRoundtrip() {
        var datasource = TestDatasources.H2;
        var manager = new DbQueryManager(datasource);
        var create = new CreateTable(datasource);
        create.table("tmp_quotedmapping").columns(QuotedBean.class);
        try {
            manager.executeUpdate(create);

            var bean = new QuotedBean();
            bean.setIdentifier(7);
            bean.setTotal("seven");

            var insert = new Insert(datasource);
            insert.into("tmp_quotedmapping").fields(bean);
            assertEquals(1, manager.executeUpdate(insert));

            // the quote characters are stripped when the result set column
            // is mapped back to the property
            var select = new Select(datasource);
            select.from("tmp_quotedmapping").fields(QuotedBean.class);
            var fetcher = new DbBeanFetcher<>(datasource, QuotedBean.class);
            manager.executeFetchFirst(select, fetcher);
            var restored = fetcher.getBeanInstance();
            assertNotNull(restored);
            assertEquals(7, restored.getIdentifier());
            assertEquals("seven", restored.getTotal());
        } finally {
            manager.executeUpdate(new DropTable(datasource).table("tmp_quotedmapping"));
        }
    }
}
