/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.AlterationRequiredException;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;
import rife.validation.ConstrainedUtils;

/**
 * Object representation of a SQL "ALTER TABLE" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * <p>Each {@code AlterTable} instance performs exactly one alteration, since
 * that is the only form that all databases support. Several alterations are
 * simply executed as a series of {@code AlterTable} queries.
 * <p>The {@code alterColumnType} alteration restates the complete column
 * definition on the databases that require it, like MySQL and Oracle, the
 * nullability and default that are provided with it are then part of the
 * restated definition. Databases that alter the type in isolation only
 * change the type, there the nullability and the default are altered with
 * the dedicated {@code alterColumnNullable} and {@code alterColumnDefault}
 * alterations.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class AlterTable extends AbstractQuery implements Cloneable {
    /**
     * The alterations that an {@code AlterTable} query can perform.
     *
     * @since 1.10
     */
    public enum Alteration {
        ADD_COLUMN, DROP_COLUMN, RENAME_COLUMN,
        ALTER_COLUMN_TYPE, ALTER_COLUMN_NULLABLE, ALTER_COLUMN_DEFAULT, DROP_COLUMN_DEFAULT,
        ADD_PRIMARY_KEY, ADD_FOREIGN_KEY, ADD_UNIQUE, ADD_CHECK,
        DROP_CONSTRAINT, DROP_PRIMARY_KEY, RENAME_TABLE
    }

    public static final CreateTable.Nullable NULL = CreateTable.NULL;
    public static final CreateTable.Nullable NOTNULL = CreateTable.NOTNULL;

    public static final CreateTable.ViolationAction NOACTION = CreateTable.NOACTION;
    public static final CreateTable.ViolationAction RESTRICT = CreateTable.RESTRICT;
    public static final CreateTable.ViolationAction CASCADE = CreateTable.CASCADE;
    public static final CreateTable.ViolationAction SETNULL = CreateTable.SETNULL;
    public static final CreateTable.ViolationAction SETDEFAULT = CreateTable.SETDEFAULT;

    private String table_ = null;
    private Alteration alteration_ = null;
    private CreateTable definition_ = null;
    private String columnName_ = null;
    private String newName_ = null;
    private String constraintName_ = null;
    private CreateTable.Nullable nullable_ = null;
    private String default_ = null;
    private CreateTable.PrimaryKey primaryKey_ = null;

    public AlterTable(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        table_ = null;
        alteration_ = null;
        definition_ = new CreateTable(datasource_);
        columnName_ = null;
        newName_ = null;
        constraintName_ = null;
        nullable_ = null;
        default_ = null;
        primaryKey_ = null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getTable() {
        return table_;
    }

    public Alteration getAlteration() {
        return alteration_;
    }

    public String getColumnName() {
        return columnName_;
    }

    public String getNewName() {
        return newName_;
    }

    public String getConstraintName() {
        return constraintName_;
    }

    public CreateTable.Nullable getNullable() {
        return nullable_;
    }

    public String getDefault() {
        return default_;
    }

    public CreateTable.Column getColumn() {
        if (definition_.getColumnMapping().isEmpty()) {
            return null;
        }
        return definition_.getColumnMapping().values().iterator().next();
    }

    public CreateTable.PrimaryKey getPrimaryKey() {
        return primaryKey_;
    }

    public CreateTable.ForeignKey getForeignKey() {
        if (definition_.getForeignKeys().isEmpty()) {
            return null;
        }
        return definition_.getForeignKeys().get(0);
    }

    public CreateTable.UniqueConstraint getUniqueConstraint() {
        if (definition_.getUniqueConstraints().isEmpty()) {
            return null;
        }
        return definition_.getUniqueConstraints().get(0);
    }

    public CreateTable.CheckConstraint getCheckConstraint() {
        if (definition_.getCheckConstraints().isEmpty()) {
            return null;
        }
        return definition_.getCheckConstraints().get(0);
    }

    public AlterTable table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (table.isEmpty()) throw new IllegalArgumentException("table can't be empty.");

        table_ = table;
        clearGenerated();

        return this;
    }

    private void initAlteration(Alteration alteration) {
        if (alteration_ != null) {
            throw new IllegalStateException("An AlterTable query performs exactly one alteration, it already contains " + alteration_ + ".");
        }
        alteration_ = alteration;
        clearGenerated();
    }

    private void initColumnAlteration(Alteration alteration, String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        initAlteration(alteration);
        columnName_ = name;
    }

    public AlterTable addColumn(String name, Class type) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, String typeAttribute) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, String typeAttribute) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, int scale) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, scale);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, int scale, String typeAttribute) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, scale, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, int scale, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, scale, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, Class type, int precision, int scale, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, type, precision, scale, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, String customType) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, customType);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(String name, String customType, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ADD_COLUMN);
        definition_.column(name, customType, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable addColumn(Class beanClass, String propertyName)
    throws DbQueryException {
        initAlteration(Alteration.ADD_COLUMN);
        defineColumnFromBean(beanClass, propertyName);
        return this;
    }

    private void defineColumnFromBean(Class beanClass, String propertyName) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (propertyName.isEmpty()) throw new IllegalArgumentException("propertyName can't be empty.");

        var types = QueryHelper.getBeanPropertyTypes(beanClass, new String[]{propertyName}, null);
        var type = types.get(propertyName);
        if (null == type) {
            throw new IllegalArgumentException("the '" + propertyName + "' property couldn't be found in bean class '" + beanClass.getName() + "'.");
        }

        var constrained = ConstrainedUtils.getConstrainedInstance(beanClass);
        var column_name = QueryHelper.getColumnName(constrained, propertyName);
        definition_.column(column_name, type);
        columnName_ = column_name;

        if (constrained != null) {
            var constrained_property = constrained.getConstrainedProperty(propertyName);
            if (constrained_property != null) {
                if (constrained_property.isNotNull()) {
                    definition_.nullable(column_name, NOTNULL);
                }
                if (constrained_property.hasPrecision()) {
                    if (constrained_property.hasScale()) {
                        definition_.precision(column_name, constrained_property.getPrecision(), constrained_property.getScale());
                    } else {
                        definition_.precision(column_name, constrained_property.getPrecision());
                    }
                }
                if (constrained_property.hasDefaultValue()) {
                    definition_.defaultValue(column_name, constrained_property.getDefaultValue());
                }
            }
        }
    }

    private static String resolveColumnName(Class beanClass, String propertyName) {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (propertyName.isEmpty()) throw new IllegalArgumentException("propertyName can't be empty.");

        return QueryHelper.getColumnName(ConstrainedUtils.getConstrainedInstance(beanClass), propertyName);
    }

    private void ensureColumnDefinition() {
        if (alteration_ != Alteration.ADD_COLUMN && alteration_ != Alteration.ALTER_COLUMN_TYPE) {
            throw new IllegalStateException("A column has to be added or have its type altered before it can be modified.");
        }
    }

    public AlterTable precision(String name, int precision) {
        ensureColumnDefinition();
        definition_.precision(name, precision);
        clearGenerated();
        return this;
    }

    public AlterTable precision(String name, int precision, int scale) {
        ensureColumnDefinition();
        definition_.precision(name, precision, scale);
        clearGenerated();
        return this;
    }

    public AlterTable nullable(String name, CreateTable.Nullable nullable) {
        ensureColumnDefinition();
        definition_.nullable(name, nullable);
        clearGenerated();
        return this;
    }

    public AlterTable defaultValue(String name, boolean value) {
        ensureColumnDefinition();
        definition_.defaultValue(name, value);
        clearGenerated();
        return this;
    }

    public AlterTable defaultValue(String name, Object value) {
        ensureColumnDefinition();
        definition_.defaultValue(name, value);
        clearGenerated();
        return this;
    }

    public AlterTable defaultFunction(String name, String defaultFunction) {
        ensureColumnDefinition();
        definition_.defaultFunction(name, defaultFunction);
        clearGenerated();
        return this;
    }

    public AlterTable customAttribute(String name, String attribute) {
        ensureColumnDefinition();
        definition_.customAttribute(name, attribute);
        clearGenerated();
        return this;
    }

    public AlterTable dropColumn(String name) {
        initColumnAlteration(Alteration.DROP_COLUMN, name);
        return this;
    }

    public AlterTable dropColumn(Class beanClass, String propertyName) {
        return dropColumn(resolveColumnName(beanClass, propertyName));
    }

    public AlterTable renameColumn(String name, String newName) {
        if (null == newName) throw new IllegalArgumentException("newName can't be null.");
        if (newName.isEmpty()) throw new IllegalArgumentException("newName can't be empty.");

        initColumnAlteration(Alteration.RENAME_COLUMN, name);
        newName_ = newName;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, String typeAttribute) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, String typeAttribute) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, int scale) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, scale);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, int scale, String typeAttribute) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, scale, typeAttribute);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, int scale, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, scale, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, Class type, int precision, int scale, String typeAttribute, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, type, precision, scale, typeAttribute, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, String customType) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, customType);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(String name, String customType, CreateTable.Nullable nullable) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        definition_.column(name, customType, nullable);
        columnName_ = name;
        return this;
    }

    public AlterTable alterColumnType(Class beanClass, String propertyName) {
        initAlteration(Alteration.ALTER_COLUMN_TYPE);
        defineColumnFromBean(beanClass, propertyName);
        return this;
    }

    public AlterTable alterColumnNullable(String name, CreateTable.Nullable nullable) {
        if (null == nullable) throw new IllegalArgumentException("nullable can't be null.");

        initColumnAlteration(Alteration.ALTER_COLUMN_NULLABLE, name);
        nullable_ = nullable;
        return this;
    }

    public AlterTable alterColumnNullable(Class beanClass, String propertyName, CreateTable.Nullable nullable) {
        return alterColumnNullable(resolveColumnName(beanClass, propertyName), nullable);
    }

    public AlterTable alterColumnDefault(String name, boolean value) {
        return alterColumnDefault(name, Boolean.valueOf(value));
    }

    public AlterTable alterColumnDefault(String name, Object value) {
        if (null == value) throw new IllegalArgumentException("value can't be null, use dropColumnDefault instead.");

        initColumnAlteration(Alteration.ALTER_COLUMN_DEFAULT, name);
        default_ = datasource_.getSqlConversion().getSqlValue(value);
        return this;
    }

    public AlterTable alterColumnDefaultFunction(String name, String defaultFunction) {
        if (null == defaultFunction) throw new IllegalArgumentException("defaultFunction can't be null.");
        if (defaultFunction.isEmpty()) throw new IllegalArgumentException("defaultFunction can't be empty.");

        initColumnAlteration(Alteration.ALTER_COLUMN_DEFAULT, name);
        default_ = defaultFunction;
        return this;
    }

    public AlterTable alterColumnDefault(Class beanClass, String propertyName, Object value) {
        return alterColumnDefault(resolveColumnName(beanClass, propertyName), value);
    }

    public AlterTable dropColumnDefault(String name) {
        initColumnAlteration(Alteration.DROP_COLUMN_DEFAULT, name);
        return this;
    }

    public AlterTable dropColumnDefault(Class beanClass, String propertyName) {
        return dropColumnDefault(resolveColumnName(beanClass, propertyName));
    }

    public AlterTable addPrimaryKey(String column) {
        return addPrimaryKey(null, new String[]{column});
    }

    public AlterTable addPrimaryKey(String[] columns) {
        return addPrimaryKey(null, columns);
    }

    public AlterTable addPrimaryKey(String name, String column) {
        return addPrimaryKey(name, new String[]{column});
    }

    public AlterTable addPrimaryKey(String name, String[] columns) {
        if (name != null && name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");
        if (null == columns) throw new IllegalArgumentException("columns array can't be null.");
        if (0 == columns.length) throw new IllegalArgumentException("columns array can't be empty.");

        initAlteration(Alteration.ADD_PRIMARY_KEY);
        // the primary key is constructed directly since the column lives in
        // the altered table, CreateTable would try to make its own column
        // definition not null
        primaryKey_ = definition_.new PrimaryKey(name, columns);
        return this;
    }

    public AlterTable addForeignKey(String foreignTable, String localColumn, String foreignColumn) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(foreignTable, localColumn, foreignColumn);
        return this;
    }

    public AlterTable addForeignKey(String foreignTable, String localColumn, String foreignColumn, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(foreignTable, localColumn, foreignColumn, onUpdate, onDelete);
        return this;
    }

    public AlterTable addForeignKey(String foreignTable, String[] columnsMapping) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(foreignTable, columnsMapping);
        return this;
    }

    public AlterTable addForeignKey(String foreignTable, String[] columnsMapping, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(foreignTable, columnsMapping, onUpdate, onDelete);
        return this;
    }

    public AlterTable addForeignKey(String name, String foreignTable, String localColumn, String foreignColumn) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(name, foreignTable, localColumn, foreignColumn);
        return this;
    }

    public AlterTable addForeignKey(String name, String foreignTable, String localColumn, String foreignColumn, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(name, foreignTable, localColumn, foreignColumn, onUpdate, onDelete);
        return this;
    }

    public AlterTable addForeignKey(String name, String foreignTable, String[] columnsMapping) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(name, foreignTable, columnsMapping);
        return this;
    }

    public AlterTable addForeignKey(String name, String foreignTable, String[] columnsMapping, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        initAlteration(Alteration.ADD_FOREIGN_KEY);
        definition_.foreignKey(name, foreignTable, columnsMapping, onUpdate, onDelete);
        return this;
    }

    public AlterTable addUnique(String column) {
        initAlteration(Alteration.ADD_UNIQUE);
        definition_.unique(column);
        return this;
    }

    public AlterTable addUnique(String[] columns) {
        initAlteration(Alteration.ADD_UNIQUE);
        definition_.unique(columns);
        return this;
    }

    public AlterTable addUnique(String name, String column) {
        initAlteration(Alteration.ADD_UNIQUE);
        definition_.unique(name, column);
        return this;
    }

    public AlterTable addUnique(String name, String[] columns) {
        initAlteration(Alteration.ADD_UNIQUE);
        definition_.unique(name, columns);
        return this;
    }

    public AlterTable addCheck(String expression) {
        initAlteration(Alteration.ADD_CHECK);
        definition_.check(expression);
        return this;
    }

    public AlterTable addCheck(String name, String expression) {
        initAlteration(Alteration.ADD_CHECK);
        definition_.check(name, expression);
        return this;
    }

    public AlterTable dropConstraint(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        initAlteration(Alteration.DROP_CONSTRAINT);
        constraintName_ = name;
        return this;
    }

    public AlterTable dropPrimaryKey() {
        initAlteration(Alteration.DROP_PRIMARY_KEY);
        return this;
    }

    public AlterTable renameTo(String newName) {
        if (null == newName) throw new IllegalArgumentException("newName can't be null.");
        if (newName.isEmpty()) throw new IllegalArgumentException("newName can't be empty.");

        initAlteration(Alteration.RENAME_TABLE);
        newName_ = newName;
        return this;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == table_) {
                throw new TableNameRequiredException("AlterTable");
            } else if (null == alteration_) {
                throw new AlterationRequiredException("AlterTable");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".alter_table");

                template.setValue("TABLE", table_);

                switch (alteration_) {
                    case ADD_COLUMN -> {
                        template.setValue("COLUMN_DEF", getColumn().getSql(template));
                        sql_ = getBlock(template, "ADD_COLUMN");
                        template.removeValue("COLUMN_DEF");
                    }
                    case DROP_COLUMN -> {
                        template.setValue("NAME", columnName_);
                        sql_ = getBlock(template, "DROP_COLUMN");
                        template.removeValue("NAME");
                    }
                    case RENAME_COLUMN -> {
                        template.setValue("NAME", columnName_);
                        template.setValue("NEW_NAME", newName_);
                        sql_ = getBlock(template, "RENAME_COLUMN");
                        template.removeValue("NAME");
                        template.removeValue("NEW_NAME");
                    }
                    case ALTER_COLUMN_TYPE -> {
                        var column = getColumn();
                        template.setValue("NAME", column.getName());
                        var type = column.getCustomType();
                        if (null == type) {
                            type = datasource_.getSqlConversion().getSqlType(column.getType(), column.getPrecision(), column.getScale());
                        }
                        template.setValue("TYPE", type);
                        if (column.getTypeAttribute() != null) {
                            template.appendValue("TYPE", " ");
                            template.appendValue("TYPE", column.getTypeAttribute());
                        }
                        if (column.getNullable() != null) {
                            template.setValue("NULLABLE", getBlock(template, column.getNullable().toString()));
                        }
                        if (column.getDefault() != null) {
                            template.setValue("V", column.getDefault());
                            template.setValue("DEFAULT", getBlock(template, "DEFAULT"));
                            template.removeValue("V");
                        }
                        if (!column.getCustomAttributes().isEmpty()) {
                            template.setValue("V", StringUtils.join(column.getCustomAttributes(), " "));
                            template.setValue("CUSTOM_ATTRIBUTES", getBlock(template, "CUSTOM_ATTRIBUTES"));
                            template.removeValue("V");
                        }
                        sql_ = getBlock(template, "ALTER_COLUMN_TYPE");
                        template.removeValue("NAME");
                        template.removeValue("TYPE");
                        template.removeValue("NULLABLE");
                        template.removeValue("DEFAULT");
                        template.removeValue("CUSTOM_ATTRIBUTES");
                    }
                    case ALTER_COLUMN_NULLABLE -> {
                        template.setValue("NAME", columnName_);
                        sql_ = getBlock(template, NOTNULL == nullable_ ? "ALTER_COLUMN_NOTNULL" : "ALTER_COLUMN_NULL");
                        template.removeValue("NAME");
                    }
                    case ALTER_COLUMN_DEFAULT -> {
                        template.setValue("NAME", columnName_);
                        template.setValue("V", default_);
                        sql_ = getBlock(template, "ALTER_COLUMN_DEFAULT");
                        template.removeValue("NAME");
                        template.removeValue("V");
                    }
                    case DROP_COLUMN_DEFAULT -> {
                        template.setValue("NAME", columnName_);
                        sql_ = getBlock(template, "ALTER_COLUMN_DROP_DEFAULT");
                        template.removeValue("NAME");
                    }
                    case ADD_PRIMARY_KEY -> {
                        template.setValue("CONSTRAINT_DEF", getPrimaryKey().getSql(template));
                        sql_ = getBlock(template, "ADD_CONSTRAINT");
                        template.removeValue("CONSTRAINT_DEF");
                    }
                    case ADD_FOREIGN_KEY -> {
                        template.setValue("CONSTRAINT_DEF", getForeignKey().getSql(template));
                        sql_ = getBlock(template, "ADD_CONSTRAINT");
                        template.removeValue("CONSTRAINT_DEF");
                    }
                    case ADD_UNIQUE -> {
                        template.setValue("CONSTRAINT_DEF", getUniqueConstraint().getSql(template));
                        sql_ = getBlock(template, "ADD_CONSTRAINT");
                        template.removeValue("CONSTRAINT_DEF");
                    }
                    case ADD_CHECK -> {
                        template.setValue("CONSTRAINT_DEF", getCheckConstraint().getSql(template));
                        sql_ = getBlock(template, "ADD_CONSTRAINT");
                        template.removeValue("CONSTRAINT_DEF");
                    }
                    case DROP_CONSTRAINT -> {
                        template.setValue("NAME", constraintName_);
                        sql_ = getBlock(template, "DROP_CONSTRAINT");
                        template.removeValue("NAME");
                    }
                    case DROP_PRIMARY_KEY -> sql_ = getBlock(template, "DROP_PRIMARY_KEY");
                    case RENAME_TABLE -> {
                        template.setValue("NEW_NAME", newName_);
                        sql_ = getBlock(template, "RENAME_TABLE");
                        template.removeValue("NEW_NAME");
                    }
                }

                template.removeValue("TABLE");

                assert sql_ != null;
                assert !sql_.isEmpty();
            }
        }

        return sql_;
    }

    private String getBlock(Template template, String name)
    throws DbQueryException {
        var block = template.getBlock(name);
        if (block.isEmpty()) {
            throw new UnsupportedSqlFeatureException(name, datasource_.getAliasedDriver());
        }
        return block;
    }

    public AlterTable clone() {
        var new_instance = (AlterTable) super.clone();
        if (new_instance != null) {
            if (definition_ != null) {
                new_instance.definition_ = definition_.clone();
            }
            if (primaryKey_ != null) {
                new_instance.primaryKey_ = primaryKey_.clone();
            }
        }

        return new_instance;
    }
}
