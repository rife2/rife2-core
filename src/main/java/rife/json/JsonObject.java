/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import rife.tools.Convert;
import rife.tools.exceptions.ConversionException;

import java.io.IOException;
import java.io.Serial;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a JSON object as a regular ordered map, with additional
 * fluent construction methods and typed value retrieval.
 * <p>
 * Values are plain Java instances: {@code String}, {@code Long},
 * {@code Double}, {@code Boolean}, {@code null}, {@link JsonObject} and
 * {@link JsonArray}.
 * <p>
 * All the mutation methods convert maps, collections and arrays to
 * {@code JsonObject} and {@code JsonArray} instances, values that are
 * assigned through map entries bypass this conversion.
 * <p>
 * The typed retrieval methods are lenient: strings are parsed into the
 * requested numeric or boolean types, numbers are truncated when
 * narrower types are requested, and absent or null members return the
 * provided default or a zero-like value.
 * <p>
 * For instance:
 * <pre>
 * var json = new JsonObject()
 *     .set("name", "my-app")
 *     .set("version", "1.0.0")
 *     .object("dependencies", d -&gt; d
 *         .set("com.uwyn.rife2:rife2", "1.9.1"))
 *     .array("authors", a -&gt; a
 *         .append("gbevin"));
 * </pre>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class JsonObject extends LinkedHashMap<String, Object> {
    @Serial private static final long serialVersionUID = 4189382919418913857L;

    /**
     * Creates an empty JSON object.
     *
     * @since 1.10
     */
    public JsonObject() {
    }

    /**
     * Creates a JSON object with the members of a map.
     * <p>
     * Nested maps, collections and arrays are converted to
     * {@code JsonObject} and {@link JsonArray} instances.
     *
     * @param members the map whose members become the members of this
     *                JSON object, in the map's iteration order
     * @since 1.10
     */
    public JsonObject(Map<String, ?> members) {
        putAll(members);
    }

    /**
     * Parses a JSON document that is expected to be a JSON object.
     * <p>
     * This is equivalent to {@link Json#parseObject(String)} and also
     * enables RIFE2's standard conversions to convert strings to
     * {@code JsonObject} instances.
     *
     * @param json the JSON document to parse
     * @return the parsed {@code JsonObject}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON object
     * @since 1.10
     */
    public static JsonObject parse(String json) {
        return Json.parseObject(json);
    }

    /**
     * Sets a member of this JSON object.
     * <p>
     * Maps, collections and arrays are converted to {@code JsonObject}
     * and {@link JsonArray} instances so that the typed retrieval
     * methods work on them.
     *
     * @param name  the name of the member
     * @param value the value of the member
     * @return this {@code JsonObject} instance
     * @since 1.10
     */
    public JsonObject set(String name, Object value) {
        put(name, value);
        return this;
    }

    @Override
    public Object put(String name, Object value) {
        return super.put(name, Json.normalize(value));
    }

    @Override
    public void putAll(Map<? extends String, ?> members) {
        for (var entry : members.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object putIfAbsent(String name, Object value) {
        return super.putIfAbsent(name, Json.normalize(value));
    }

    @Override
    public Object replace(String name, Object value) {
        return super.replace(name, Json.normalize(value));
    }

    @Override
    public boolean replace(String name, Object oldValue, Object newValue) {
        return super.replace(name, oldValue, Json.normalize(newValue));
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        super.replaceAll((name, value) -> Json.normalize(function.apply(name, value)));
    }

    @Override
    public Object merge(String name, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return super.merge(name, Json.normalize(value), (existing, provided) -> Json.normalize(remappingFunction.apply(existing, provided)));
    }

    @Override
    public Object compute(String name, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return super.compute(name, (key, value) -> Json.normalize(remappingFunction.apply(key, value)));
    }

    @Override
    public Object computeIfAbsent(String name, Function<? super String, ?> mappingFunction) {
        return super.computeIfAbsent(name, key -> Json.normalize(mappingFunction.apply(key)));
    }

    @Override
    public Object computeIfPresent(String name, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return super.computeIfPresent(name, (key, value) -> Json.normalize(remappingFunction.apply(key, value)));
    }

    /**
     * Sets a member of this JSON object to a new nested JSON object that
     * is constructed by the provided action.
     *
     * @param name   the name of the member
     * @param action the action that constructs the nested object
     * @return this {@code JsonObject} instance
     * @since 1.10
     */
    public JsonObject object(String name, JsonObjectAction action) {
        var object = new JsonObject();
        action.use(object);
        put(name, object);
        return this;
    }

    /**
     * Sets a member of this JSON object to a new nested JSON array that
     * is constructed by the provided action.
     *
     * @param name   the name of the member
     * @param action the action that constructs the nested array
     * @return this {@code JsonObject} instance
     * @since 1.10
     */
    public JsonObject array(String name, JsonArrayAction action) {
        var array = new JsonArray();
        action.use(array);
        put(name, array);
        return this;
    }

    /**
     * Retrieves a member as a string.
     *
     * @param name the name of the member
     * @return the member value as a string; or {@code null} when absent or null
     * @since 1.10
     */
    public String getString(String name) {
        return getString(name, null);
    }

    /**
     * Retrieves a member as a string.
     *
     * @param name         the name of the member
     * @param defaultValue the value to return when the member is absent or null
     * @return the member value as a string; or the default value
     * @since 1.10
     */
    public String getString(String name, String defaultValue) {
        var value = get(name);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * Retrieves a member as an int.
     *
     * @param name the name of the member
     * @return the member value as an int; or {@code 0} when absent or null
     * @since 1.10
     */
    public int getInt(String name) {
        return getInt(name, 0);
    }

    /**
     * Retrieves a member as an int.
     *
     * @param name         the name of the member
     * @param defaultValue the value to return when the member is absent or null
     * @return the member value as an int; or the default value
     * @since 1.10
     */
    public int getInt(String name, int defaultValue) {
        var value = get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * Retrieves a member as a long.
     *
     * @param name the name of the member
     * @return the member value as a long; or {@code 0L} when absent or null
     * @since 1.10
     */
    public long getLong(String name) {
        return getLong(name, 0L);
    }

    /**
     * Retrieves a member as a long.
     *
     * @param name         the name of the member
     * @param defaultValue the value to return when the member is absent or null
     * @return the member value as a long; or the default value
     * @since 1.10
     */
    public long getLong(String name, long defaultValue) {
        var value = get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * Retrieves a member as a double.
     *
     * @param name the name of the member
     * @return the member value as a double; or {@code 0.0} when absent or null
     * @since 1.10
     */
    public double getDouble(String name) {
        return getDouble(name, 0.0);
    }

    /**
     * Retrieves a member as a double.
     *
     * @param name         the name of the member
     * @param defaultValue the value to return when the member is absent or null
     * @return the member value as a double; or the default value
     * @since 1.10
     */
    public double getDouble(String name, double defaultValue) {
        var value = get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * Retrieves a member as a boolean.
     *
     * @param name the name of the member
     * @return the member value as a boolean; or {@code false} when absent or null
     * @since 1.10
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Retrieves a member as a boolean.
     *
     * @param name         the name of the member
     * @param defaultValue the value to return when the member is absent or null
     * @return the member value as a boolean; or the default value
     * @since 1.10
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        var value = get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Retrieves a member as a date.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param name the name of the member
     * @return the member value as a {@code Date}; or {@code null} when absent or null
     * @since 1.10
     */
    public Date getDate(String name) {
        return convertMember(name, Date.class);
    }

    /**
     * Retrieves a member as an instant.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param name the name of the member
     * @return the member value as an {@code Instant}; or {@code null} when absent or null
     * @since 1.10
     */
    public Instant getInstant(String name) {
        return convertMember(name, Instant.class);
    }

    /**
     * Retrieves a member as a local date.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param name the name of the member
     * @return the member value as a {@code LocalDate}; or {@code null} when absent or null
     * @since 1.10
     */
    public LocalDate getLocalDate(String name) {
        return convertMember(name, LocalDate.class);
    }

    /**
     * Retrieves a member as a local date and time.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param name the name of the member
     * @return the member value as a {@code LocalDateTime}; or {@code null} when absent or null
     * @since 1.10
     */
    public LocalDateTime getLocalDateTime(String name) {
        return convertMember(name, LocalDateTime.class);
    }

    /**
     * Retrieves a member as a local time.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param name the name of the member
     * @return the member value as a {@code LocalTime}; or {@code null} when absent or null
     * @since 1.10
     */
    public LocalTime getLocalTime(String name) {
        return convertMember(name, LocalTime.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertMember(String name, Class<T> type) {
        var value = get(name);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        try {
            return (T) Convert.toType(value, type);
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Unable to convert member '" + name + "' to " + type.getSimpleName(), e);
        }
    }

    /**
     * Retrieves a member as a nested JSON object.
     *
     * @param name the name of the member
     * @return the nested {@code JsonObject}; or {@code null} when absent or null
     * @since 1.10
     */
    public JsonObject getObject(String name) {
        return (JsonObject) get(name);
    }

    /**
     * Retrieves a member as a nested JSON array.
     *
     * @param name the name of the member
     * @return the nested {@code JsonArray}; or {@code null} when absent or null
     * @since 1.10
     */
    public JsonArray getArray(String name) {
        return (JsonArray) get(name);
    }

    /**
     * Fills a new bean instance with the members of this JSON object.
     *
     * @param beanClass the class of the bean to fill
     * @return the filled bean instance
     * @since 1.10
     */
    public <T> T toBean(Class<T> beanClass) {
        return Json.toBean(this, beanClass);
    }

    /**
     * Serializes this JSON object into its compact string representation.
     *
     * @return the JSON string
     * @since 1.10
     */
    public String toString() {
        return Json.toString(this);
    }

    /**
     * Serializes this JSON object into its multi-line indented string
     * representation.
     *
     * @return the pretty-printed JSON string
     * @since 1.10
     */
    public String toPrettyString() {
        return Json.toPrettyString(this);
    }

    /**
     * Serializes this JSON object in its compact representation to a writer.
     * <p>
     * The output is streamed, no intermediate string is built.
     *
     * @param writer the writer to serialize to
     * @throws IOException when an error occurred while writing
     * @since 1.10
     */
    public void print(Writer writer)
    throws IOException {
        Json.print(this, writer, -1);
    }

    /**
     * Serializes this JSON object in its multi-line indented representation
     * to a writer.
     * <p>
     * The output is streamed, no intermediate string is built.
     *
     * @param writer the writer to serialize to
     * @throws IOException when an error occurred while writing
     * @since 1.10
     */
    public void prettyPrint(Writer writer)
    throws IOException {
        Json.print(this, writer, 0);
    }
}
