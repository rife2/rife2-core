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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a JSON array as a regular list, with additional fluent
 * construction methods and typed value retrieval.
 * <p>
 * Values are plain Java instances: {@code String}, {@code Long},
 * {@code Double}, {@code Boolean}, {@code null}, {@link JsonObject} and
 * {@link JsonArray}.
 * <p>
 * All the mutation methods convert maps, collections and arrays to
 * {@link JsonObject} and {@code JsonArray} instances, values that are
 * assigned through list iterators bypass this conversion.
 * <p>
 * The typed retrieval methods are lenient: strings are parsed into the
 * requested numeric or boolean types, numbers are truncated when
 * narrower types are requested, and null elements return zero-like
 * values.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class JsonArray extends ArrayList<Object> {
    @Serial private static final long serialVersionUID = 7534198271431717166L;

    /**
     * Creates an empty JSON array.
     *
     * @since 1.10
     */
    public JsonArray() {
    }

    /**
     * Creates a JSON array with the elements of a collection.
     * <p>
     * Nested maps, collections and arrays are converted to
     * {@link JsonObject} and {@code JsonArray} instances.
     *
     * @param elements the collection whose elements become the elements
     *                 of this JSON array, in the collection's iteration order
     * @since 1.10
     */
    public JsonArray(Collection<?> elements) {
        addAll(elements);
    }

    /**
     * Parses a JSON document that is expected to be a JSON array.
     * <p>
     * This is equivalent to {@link Json#parseArray(String)} and also
     * enables RIFE2's standard conversions to convert strings to
     * {@code JsonArray} instances.
     *
     * @param json the JSON document to parse
     * @return the parsed {@code JsonArray}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON array
     * @since 1.10
     */
    public static JsonArray parse(String json) {
        return Json.parseArray(json);
    }

    /**
     * Appends a value to this JSON array.
     * <p>
     * Maps, collections and arrays are converted to {@link JsonObject}
     * and {@code JsonArray} instances so that the typed retrieval
     * methods work on them.
     *
     * @param value the value to append
     * @return this {@code JsonArray} instance
     * @since 1.10
     */
    public JsonArray append(Object value) {
        add(value);
        return this;
    }

    @Override
    public boolean add(Object value) {
        return super.add(Json.normalize(value));
    }

    @Override
    public void add(int index, Object value) {
        super.add(index, Json.normalize(value));
    }

    @Override
    public boolean addAll(Collection<?> elements) {
        var modified = false;
        for (var element : elements) {
            modified |= add(element);
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<?> elements) {
        var position = index;
        for (var element : elements) {
            add(position++, element);
        }
        return !elements.isEmpty();
    }

    @Override
    public Object set(int index, Object value) {
        return super.set(index, Json.normalize(value));
    }

    @Override
    public void replaceAll(UnaryOperator<Object> operator) {
        super.replaceAll(value -> Json.normalize(operator.apply(value)));
    }

    /**
     * Appends a new nested JSON object that is constructed by the
     * provided action.
     *
     * @param action the action that constructs the nested object
     * @return this {@code JsonArray} instance
     * @since 1.10
     */
    public JsonArray object(JsonObjectAction action) {
        var object = new JsonObject();
        action.use(object);
        add(object);
        return this;
    }

    /**
     * Appends a new nested JSON array that is constructed by the
     * provided action.
     *
     * @param action the action that constructs the nested array
     * @return this {@code JsonArray} instance
     * @since 1.10
     */
    public JsonArray array(JsonArrayAction action) {
        var array = new JsonArray();
        action.use(array);
        add(array);
        return this;
    }

    /**
     * Retrieves an element as a string.
     *
     * @param index the index of the element
     * @return the element value as a string; or {@code null} when null
     * @since 1.10
     */
    public String getString(int index) {
        var value = get(index);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Retrieves an element as an int.
     *
     * @param index the index of the element
     * @return the element value as an int; or {@code 0} when null
     * @since 1.10
     */
    public int getInt(int index) {
        var value = get(index);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * Retrieves an element as a long.
     *
     * @param index the index of the element
     * @return the element value as a long; or {@code 0L} when null
     * @since 1.10
     */
    public long getLong(int index) {
        var value = get(index);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * Retrieves an element as a double.
     *
     * @param index the index of the element
     * @return the element value as a double; or {@code 0.0} when null
     * @since 1.10
     */
    public double getDouble(int index) {
        var value = get(index);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * Retrieves an element as a boolean.
     *
     * @param index the index of the element
     * @return the element value as a boolean; or {@code false} when null
     * @since 1.10
     */
    public boolean getBoolean(int index) {
        var value = get(index);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Retrieves an element as a date.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param index the index of the element
     * @return the element value as a {@code Date}; or {@code null} when null
     * @since 1.10
     */
    public Date getDate(int index) {
        return convertElement(index, Date.class);
    }

    /**
     * Retrieves an element as an instant.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param index the index of the element
     * @return the element value as an {@code Instant}; or {@code null} when null
     * @since 1.10
     */
    public Instant getInstant(int index) {
        return convertElement(index, Instant.class);
    }

    /**
     * Retrieves an element as a local date.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param index the index of the element
     * @return the element value as a {@code LocalDate}; or {@code null} when null
     * @since 1.10
     */
    public LocalDate getLocalDate(int index) {
        return convertElement(index, LocalDate.class);
    }

    /**
     * Retrieves an element as a local date and time.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param index the index of the element
     * @return the element value as a {@code LocalDateTime}; or {@code null} when null
     * @since 1.10
     */
    public LocalDateTime getLocalDateTime(int index) {
        return convertElement(index, LocalDateTime.class);
    }

    /**
     * Retrieves an element as a local time.
     * <p>
     * ISO 8601 strings and epoch millisecond numbers convert through
     * RIFE2's standard conversions.
     *
     * @param index the index of the element
     * @return the element value as a {@code LocalTime}; or {@code null} when null
     * @since 1.10
     */
    public LocalTime getLocalTime(int index) {
        return convertElement(index, LocalTime.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertElement(int index, Class<T> type) {
        var value = get(index);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        try {
            return (T) Convert.toType(value, type);
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Unable to convert element " + index + " to " + type.getSimpleName(), e);
        }
    }

    /**
     * Converts the elements of this JSON array into a list of beans.
     * <p>
     * Each element is converted with the same rules as {@link Json#toBean},
     * {@code null} elements stay {@code null}.
     *
     * @param beanClass the class of the beans
     * @return the list with a bean instance for each element
     * @since 1.10
     */
    public <T> List<T> toBeanList(Class<T> beanClass) {
        return Json.toBeanList(this, beanClass);
    }

    /**
     * Retrieves an element as a nested JSON object.
     *
     * @param index the index of the element
     * @return the nested {@code JsonObject}; or {@code null} when null
     * @since 1.10
     */
    public JsonObject getObject(int index) {
        return (JsonObject) get(index);
    }

    /**
     * Retrieves an element as a nested JSON array.
     *
     * @param index the index of the element
     * @return the nested {@code JsonArray}; or {@code null} when null
     * @since 1.10
     */
    public JsonArray getArray(int index) {
        return (JsonArray) get(index);
    }

    /**
     * Serializes this JSON array into its compact string representation.
     *
     * @return the JSON string
     * @since 1.10
     */
    public String toString() {
        return Json.toString(this);
    }

    /**
     * Serializes this JSON array into its multi-line indented string
     * representation.
     *
     * @return the pretty-printed JSON string
     * @since 1.10
     */
    public String toPrettyString() {
        return Json.toPrettyString(this);
    }

    /**
     * Serializes this JSON array in its compact representation to a writer.
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
     * Serializes this JSON array in its multi-line indented representation
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
