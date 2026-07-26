/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import rife.tools.BeanUtils;
import rife.tools.ClassUtils;
import rife.tools.Convert;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.tools.exceptions.ConversionException;
import rife.validation.ConstrainedUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code Json} class provides JSON parsing, generation and bean
 * conversion.
 * <p>Parsing is strict according to RFC 8259 and produces plain Java
 * values: {@link JsonObject}, {@link JsonArray}, {@code String},
 * {@code Long}, {@code Double}, {@code Boolean} and {@code null}. Integral
 * numbers that don't fit into a {@code Long} become {@code Double} values,
 * which means that they can lose precision. When an object contains the
 * same member name multiple times, the last value wins.
 * <p>For instance:
 * <pre>
 * var config = Json.parseObject("""
 *     {"name": "my-app", "port": 8080}""");
 * var port = config.getInt("port", 80);
 * </pre>
 * <p>Beans are converted in both directions with {@link #from} and
 * {@link #toBean}, relying on the same property conventions as the rest of
 * RIFE2. Records are converted through their components in the same
 * fashion.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see JsonObject
 * @see JsonArray
 * @since 1.10
 */
public final class Json {
    private Json() {
    }

    /**
     * Parses a JSON document.
     * <p>This method will parse any JSON document and hand you back
     * whichever value sits at the top level of it.
     *
     * @param json the JSON document to parse
     * @return the parsed value: a {@code JsonObject}, {@code JsonArray},
     * {@code String}, {@code Long}, {@code Double}, {@code Boolean} or {@code null}
     * @throws JsonParseException when the document couldn't be parsed
     * @see #parse(Reader)
     * @see #parseObject(String)
     * @see #parseArray(String)
     * @since 1.10
     */
    public static Object parse(String json) {
        if (json == null) {
            throw new JsonParseException("No JSON document provided", 1, 1);
        }
        return new JsonParser(json).parse();
    }

    /**
     * Parses a JSON document from a reader.
     * <p>The reader will be read out entirely before the parsing starts.
     *
     * @param reader the reader to parse the JSON document from
     * @return the parsed value
     * @throws JsonParseException when the document couldn't be parsed
     * @throws IOException        when an error occurred while reading
     * @see #parse(String)
     * @see #parseObject(Reader)
     * @see #parseArray(Reader)
     * @since 1.10
     */
    public static Object parse(Reader reader)
    throws IOException {
        var builder = new StringBuilder();
        var buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, read);
        }
        return parse(builder.toString());
    }

    /**
     * Parses a JSON document that is expected to be a JSON object.
     * <p>This method will refuse any document whose top-level value isn't
     * a JSON object, so that you don't have to check the type of the
     * result yourself.
     *
     * @param json the JSON document to parse
     * @return the parsed {@code JsonObject}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON object
     * @see #parse(String)
     * @see #parseObject(Reader)
     * @see #parseArray(String)
     * @since 1.10
     */
    public static JsonObject parseObject(String json) {
        if (parse(json) instanceof JsonObject object) {
            return object;
        }
        throw new JsonParseException("Expected a JSON object", 1, 1);
    }

    /**
     * Parses a JSON document from a reader that is expected to be a JSON
     * object.
     * <p>The reader will be read out entirely before the parsing starts.
     *
     * @param reader the reader to parse the JSON document from
     * @return the parsed {@code JsonObject}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON object
     * @throws IOException        when an error occurred while reading
     * @see #parse(Reader)
     * @see #parseObject(String)
     * @see #parseArray(Reader)
     * @since 1.10
     */
    public static JsonObject parseObject(Reader reader)
    throws IOException {
        if (parse(reader) instanceof JsonObject object) {
            return object;
        }
        throw new JsonParseException("Expected a JSON object", 1, 1);
    }

    /**
     * Parses a JSON document that is expected to be a JSON array.
     * <p>This method will refuse any document whose top-level value isn't
     * a JSON array, so that you don't have to check the type of the result
     * yourself.
     *
     * @param json the JSON document to parse
     * @return the parsed {@code JsonArray}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON array
     * @see #parse(String)
     * @see #parseArray(Reader)
     * @see #parseObject(String)
     * @since 1.10
     */
    public static JsonArray parseArray(String json) {
        if (parse(json) instanceof JsonArray array) {
            return array;
        }
        throw new JsonParseException("Expected a JSON array", 1, 1);
    }

    /**
     * Parses a JSON document from a reader that is expected to be a JSON
     * array.
     * <p>The reader will be read out entirely before the parsing starts.
     *
     * @param reader the reader to parse the JSON document from
     * @return the parsed {@code JsonArray}
     * @throws JsonParseException when the document couldn't be parsed or
     *                            isn't a JSON array
     * @throws IOException        when an error occurred while reading
     * @see #parse(Reader)
     * @see #parseArray(String)
     * @see #parseObject(Reader)
     * @since 1.10
     */
    public static JsonArray parseArray(Reader reader)
    throws IOException {
        if (parse(reader) instanceof JsonArray array) {
            return array;
        }
        throw new JsonParseException("Expected a JSON array", 1, 1);
    }

    /**
     * Creates a JSON object from the properties of a bean.
     * <p>Properties that are constrained as not {@code serialized} are not
     * included. Records are converted through their components instead of
     * through their properties.
     *
     * @param bean the bean to convert
     * @return the JSON object with the bean's properties as members
     * @see #fromExcluded(Object, String...)
     * @see #from(Collection)
     * @see #toBean(JsonObject, Class)
     * @since 1.10
     */
    public static JsonObject from(Object bean) {
        return fromExcluded(bean, (String[]) null);
    }

    /**
     * Creates a JSON object from the properties of a bean, excluding
     * particular properties.
     * <p>This method behaves exactly like {@link #from(Object)}, apart from
     * the properties whose names you provide, which are left out of the
     * resulting JSON object.
     *
     * @param bean               the bean to convert
     * @param excludedProperties the names of the properties to exclude
     * @return the JSON object with the bean's properties as members
     * @see #from(Object)
     * @since 1.10
     */
    public static JsonObject fromExcluded(Object bean, String... excludedProperties) {
        return fromBean(bean, excludedProperties, new IdentityHashMap<>());
    }

    /**
     * Creates a JSON array from the elements of a collection.
     * <p>The beans and records that are amongst the elements are converted
     * to JSON objects, just like {@link #from(Object)} does.
     *
     * @param elements the collection to convert
     * @return the JSON array with the converted elements
     * @see #from(Object)
     * @see #toBeanList(JsonArray, Class)
     * @since 1.10
     */
    public static JsonArray from(Collection<?> elements) {
        var array = new JsonArray();
        var active = new IdentityHashMap<Object, Object>();
        for (var element : elements) {
            array.add(toJsonValue(element, active));
        }
        return array;
    }

    /**
     * Serializes any JSON value into its compact string representation.
     * <p>This method accepts strings, numbers, booleans, {@code null},
     * maps, collections, arrays, dates, temporals, enums,
     * {@link JsonObject}, {@link JsonArray}, and beans or records,
     * including the beans that are nested inside maps, collections and
     * arrays, since those are automatically converted with {@link #from}.
     *
     * @param value the value to serialize
     * @return the JSON string
     * @see #toPrettyString(Object)
     * @since 1.10
     */
    public static String toString(Object value) {
        var out = new StringBuilder();
        try {
            write(value, out, -1);
        } catch (IOException e) {
            // a StringBuilder never throws
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    /**
     * Serializes any JSON value into its multi-line indented string
     * representation.
     * <p>This method accepts the same values as {@link #toString(Object)}.
     *
     * @param value the value to serialize
     * @return the pretty-printed JSON string
     * @see #toString(Object)
     * @since 1.10
     */
    public static String toPrettyString(Object value) {
        var out = new StringBuilder();
        try {
            write(value, out, 0);
        } catch (IOException e) {
            // a StringBuilder never throws
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    private static JsonObject fromBean(Object bean, String[] excludedProperties, IdentityHashMap<Object, Object> active) {
        if (active.put(bean, bean) != null) {
            throw new IllegalArgumentException("Cyclical bean reference detected for " + bean.getClass().getName());
        }
        try {
            if (bean.getClass().isRecord()) {
                return fromRecord(bean, excludedProperties, active);
            }
            var object = new JsonObject();
            BeanUtils.processPropertyValues(bean, null, excludedProperties, null,
                (name, descriptor, value, constrained) -> {
                    if (constrained != null && !constrained.isSerialized()) {
                        return;
                    }
                    object.put(name, toJsonValue(value, active));
                });
            return object;
        } catch (BeanUtilsException e) {
            if (e.getCause() instanceof IllegalArgumentException argument) {
                throw argument;
            }
            throw new IllegalArgumentException("Unable to convert bean " + bean.getClass().getName() + " to JSON", e);
        } finally {
            active.remove(bean);
        }
    }

    private static JsonObject fromRecord(Object record, String[] excludedProperties, IdentityHashMap<Object, Object> active) {
        var excluded = excludedProperties == null ? Set.of() : new HashSet<>(Arrays.asList(excludedProperties));
        var object = new JsonObject();
        for (var component : record.getClass().getRecordComponents()) {
            if (excluded.contains(component.getName())) {
                continue;
            }
            try {
                var accessor = component.getAccessor();
                accessor.trySetAccessible();
                object.put(component.getName(), toJsonValue(accessor.invoke(record), active));
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Unable to convert record " + record.getClass().getName() + " to JSON", e);
            }
        }
        return object;
    }

    private static Object toJsonValue(Object value, IdentityHashMap<Object, Object> active) {
        if (value == null ||
            value instanceof Boolean ||
            value instanceof Number ||
            value instanceof JsonObject ||
            value instanceof JsonArray) {
            return value;
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return value.toString();
        }
        if (value instanceof Enum<?> constant) {
            return constant.name();
        }
        if (value instanceof Date date) {
            return date.toInstant().toString();
        }
        if (value instanceof TemporalAccessor temporal) {
            return temporal.toString();
        }
        if (value instanceof Map<?, ?> map) {
            var object = new JsonObject();
            for (var entry : map.entrySet()) {
                object.put(String.valueOf(entry.getKey()), toJsonValue(entry.getValue(), active));
            }
            return object;
        }
        if (value instanceof Collection<?> collection) {
            var array = new JsonArray();
            for (var element : collection) {
                array.add(toJsonValue(element, active));
            }
            return array;
        }
        if (value.getClass().isArray()) {
            var array = new JsonArray();
            var length = Array.getLength(value);
            for (var i = 0; i < length; ++i) {
                array.add(toJsonValue(Array.get(value, i), active));
            }
            return array;
        }
        return fromBean(value, null, active);
    }

    /**
     * Fills a new bean instance with the members of a JSON object.
     * <p>Members without a matching bean property are ignored, while nested
     * JSON objects are converted to the types of their matching bean
     * properties. The elements of arrays, collections and maps are
     * converted to the component and generic types that the property
     * setters declare.
     * <p>Properties that are constrained as not {@code serialized} or not
     * {@code editable} are never filled in.
     * <p>Records are instantiated through their canonical constructor with
     * the members that match their components, while members that are
     * absent become {@code null} or the primitive default values.
     *
     * @param json      the JSON object with the values to fill in
     * @param beanClass the class of the bean to fill
     * @return the filled bean instance
     * @see #toBeanList(JsonArray, Class)
     * @see #from(Object)
     * @since 1.10
     */
    public static <T> T toBean(JsonObject json, Class<T> beanClass) {
        if (beanClass.isRecord()) {
            return toRecord(json, beanClass);
        }

        T bean;
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to instantiate bean " + beanClass.getName(), e);
        }

        var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        for (var entry : json.entrySet()) {
            var name = entry.getKey();
            if (!ConstrainedUtils.serializeConstrainedProperty(constrained, name, null) ||
                !ConstrainedUtils.editConstrainedProperty(constrained, name, null)) {
                continue;
            }
            Class<?> type;
            try {
                type = BeanUtils.getPropertyType(beanClass, name);
            } catch (BeanUtilsException e) {
                // members without a matching bean property are ignored
                continue;
            }

            var value = entry.getValue();
            try {
                if (value == null && type.isPrimitive()) {
                    continue;
                }
                var converted = convertToPropertyValue(beanClass, value, type, propertyGenericType(beanClass, name, type));
                BeanUtils.setPropertyValue(bean, name, converted);
            } catch (ConversionException | BeanUtilsException e) {
                throw new IllegalArgumentException("Unable to set bean property '" + name + "' of " + beanClass.getName(), e);
            }
        }
        return bean;
    }

    /**
     * Fills a list of new bean instances with the members of the JSON
     * objects in a JSON array.
     * <p>Each element is converted with the same rules as {@link #toBean},
     * while elements that are {@code null} stay {@code null}.
     *
     * @param json      the JSON array with the JSON objects to convert
     * @param beanClass the class of the beans to fill
     * @return the list with a bean instance for each element
     * @see #toBean(JsonObject, Class)
     * @see #from(Collection)
     * @since 1.10
     */
    public static <T> List<T> toBeanList(JsonArray json, Class<T> beanClass) {
        var list = new ArrayList<T>(json.size());
        for (var i = 0; i < json.size(); ++i) {
            var element = json.get(i);
            if (element == null) {
                list.add(null);
            } else if (element instanceof JsonObject object) {
                list.add(toBean(object, beanClass));
            } else {
                throw new IllegalArgumentException("Unable to convert element " + i + " to a bean, it isn't a JSON object");
            }
        }
        return list;
    }

    private static <T> T toRecord(JsonObject json, Class<T> recordClass) {
        var components = recordClass.getRecordComponents();
        var types = new Class<?>[components.length];
        var values = new Object[components.length];
        for (var i = 0; i < components.length; ++i) {
            var component = components[i];
            types[i] = component.getType();
            var value = json.get(component.getName());
            if (value == null && component.getType().isPrimitive()) {
                values[i] = Convert.getDefaultValue(component.getType());
                continue;
            }
            try {
                values[i] = convertToPropertyValue(recordClass, value, component.getType(), component.getGenericType());
            } catch (ConversionException e) {
                throw new IllegalArgumentException("Unable to set record component '" + component.getName() + "' of " + recordClass.getName(), e);
            }
        }
        try {
            var constructor = recordClass.getDeclaredConstructor(types);
            constructor.trySetAccessible();
            return constructor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to instantiate record " + recordClass.getName(), e);
        }
    }

    private static Type propertyGenericType(Class<?> beanClass, String name, Class<?> type) {
        var setter = "set" + StringUtils.capitalize(name);
        Type optional_argument = null;
        for (var method : beanClass.getMethods()) {
            if (method.getParameterCount() == 1 &&
                method.getName().equals(setter)) {
                if (method.getParameterTypes()[0] == type) {
                    return method.getGenericParameterTypes()[0];
                }
                if (method.getParameterTypes()[0] == Optional.class &&
                    method.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized) {
                    optional_argument = parameterized.getActualTypeArguments()[0];
                }
            }
        }
        if (optional_argument != null) {
            return optional_argument;
        }

        try {
            var getter = beanClass.getMethod("get" + StringUtils.capitalize(name));
            if (getter.getReturnType() == Optional.class &&
                getter.getGenericReturnType() instanceof ParameterizedType parameterized) {
                return parameterized.getActualTypeArguments()[0];
            }
        } catch (NoSuchMethodException ignored) {
        }
        return type;
    }

    private static Object convertToPropertyValue(Class<?> beanClass, Object value, Class<?> type, Type genericType)
    throws ConversionException {
        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject object) {
            if (Map.class.isAssignableFrom(type)) {
                var key_class = typeArgumentClass(beanClass, genericType, 0);
                var value_class = typeArgumentClass(beanClass, genericType, 1);
                if (value_class == null || !type.isAssignableFrom(LinkedHashMap.class)) {
                    if (type.isInstance(object)) {
                        return object;
                    }
                    return Convert.toType(object, type);
                }
                var map = new LinkedHashMap<Object, Object>();
                for (var entry : object.entrySet()) {
                    Object key = entry.getKey();
                    if (key_class != null && key_class != String.class && key_class != Object.class) {
                        key = Convert.toType(key, key_class);
                    }
                    map.put(key, convertToPropertyValue(beanClass, entry.getValue(), value_class, value_class));
                }
                return map;
            }
            if (type.isInstance(object)) {
                return object;
            }
            return toBean(object, type);
        }

        if (value instanceof JsonArray array) {
            if (type.isArray()) {
                var component = type.getComponentType();
                var result = Array.newInstance(component, array.size());
                for (var i = 0; i < array.size(); ++i) {
                    Array.set(result, i, convertToPropertyValue(beanClass, array.get(i), component, component));
                }
                return result;
            }
            if (Collection.class.isAssignableFrom(type)) {
                Collection<Object> collection;
                if (type.isAssignableFrom(ArrayList.class)) {
                    collection = new ArrayList<>();
                } else if (type.isAssignableFrom(LinkedHashSet.class)) {
                    collection = new LinkedHashSet<>();
                } else {
                    return Convert.toType(array, type);
                }
                var element_class = typeArgumentClass(beanClass, genericType, 0);
                if (element_class == null) {
                    collection.addAll(array);
                } else {
                    for (var element : array) {
                        collection.add(convertToPropertyValue(beanClass, element, element_class, element_class));
                    }
                }
                return collection;
            }
            if (type.isInstance(array)) {
                return array;
            }
            return Convert.toType(array, type);
        }

        if (type.isInstance(value)) {
            return value;
        }
        return Convert.toType(value, type);
    }

    private static Class<?> typeArgumentClass(Class<?> beanClass, Type genericType, int index) {
        if (genericType instanceof TypeVariable<?> variable) {
            genericType = ClassUtils.resolveTypeVariable(beanClass, variable);
        }
        if (genericType instanceof ParameterizedType parameterized) {
            var arguments = parameterized.getActualTypeArguments();
            if (index < arguments.length) {
                var argument = arguments[index];
                if (argument instanceof WildcardType wildcard) {
                    argument = wildcard.getUpperBounds()[0];
                }
                if (argument instanceof TypeVariable<?> variable) {
                    argument = ClassUtils.resolveTypeVariable(beanClass, variable);
                    if (argument instanceof TypeVariable<?> unresolved) {
                        argument = unresolved.getBounds()[0];
                    }
                }
                if (argument instanceof Class<?> klass) {
                    return klass;
                }
            }
        }
        return null;
    }

    // streams a JSON value to a writer through a chunked buffer, keeping
    // memory bounded without the per-append locking of a buffered writer
    static void print(Object value, Writer writer, int indent)
    throws IOException {
        var chunked = new ChunkedWriter(writer);
        write(value, chunked, indent);
        chunked.flush();
    }

    private static final class ChunkedWriter implements Appendable {
        private static final int CHUNK_SIZE = 1 << 16;

        private final Writer writer_;
        private final StringBuilder buffer_ = new StringBuilder();
        private char[] chunk_ = null;

        ChunkedWriter(Writer writer) {
            writer_ = writer;
        }

        private void flushWhenFull()
        throws IOException {
            if (buffer_.length() >= CHUNK_SIZE) {
                flush();
            }
        }

        void flush()
        throws IOException {
            var length = buffer_.length();
            if (chunk_ == null || chunk_.length < length) {
                chunk_ = new char[Math.max(length, CHUNK_SIZE)];
            }
            buffer_.getChars(0, length, chunk_, 0);
            writer_.write(chunk_, 0, length);
            buffer_.setLength(0);
        }

        public Appendable append(CharSequence characters)
        throws IOException {
            buffer_.append(characters);
            flushWhenFull();
            return this;
        }

        public Appendable append(CharSequence characters, int start, int end)
        throws IOException {
            buffer_.append(characters, start, end);
            flushWhenFull();
            return this;
        }

        public Appendable append(char character)
        throws IOException {
            buffer_.append(character);
            flushWhenFull();
            return this;
        }
    }

    static Object normalize(Object value) {
        if (value instanceof JsonObject || value instanceof JsonArray) {
            return value;
        }
        if (value instanceof Map<?, ?> map) {
            var object = new JsonObject();
            for (var entry : map.entrySet()) {
                object.put(String.valueOf(entry.getKey()), normalize(entry.getValue()));
            }
            return object;
        }
        if (value instanceof Collection<?> collection) {
            var array = new JsonArray();
            for (var element : collection) {
                array.add(normalize(element));
            }
            return array;
        }
        if (value != null && value.getClass().isArray()) {
            var array = new JsonArray();
            var length = Array.getLength(value);
            for (var i = 0; i < length; ++i) {
                array.add(normalize(Array.get(value, i)));
            }
            return array;
        }
        return value;
    }

    static void write(Object value, Appendable out, int indent)
    throws IOException {
        if (value == null) {
            out.append("null");
        } else if (value instanceof CharSequence || value instanceof Character) {
            appendString(out, value.toString());
        } else if (value instanceof Boolean || value instanceof Long || value instanceof Integer ||
                   value instanceof Short || value instanceof Byte) {
            out.append(value.toString());
        } else if (value instanceof Number number) {
            var doubles = number.doubleValue();
            if (Double.isNaN(doubles) || Double.isInfinite(doubles)) {
                throw new IllegalArgumentException("JSON numbers can't be NaN or infinite");
            }
            out.append(value.toString());
        } else if (value instanceof Enum<?> constant) {
            appendString(out, constant.name());
        } else if (value instanceof Date date) {
            out.append('"').append(date.toInstant().toString()).append('"');
        } else if (value instanceof TemporalAccessor temporal) {
            appendString(out, temporal.toString());
        } else if (value instanceof Map<?, ?> map) {
            writeObject(map, out, indent);
        } else if (value instanceof Collection<?> collection) {
            writeArray(collection, out, indent);
        } else if (value.getClass().isArray()) {
            writeNativeArray(value, out, indent);
        } else {
            // beans and records are converted to a JSON object first, so that
            // entire object graphs serialize end to end
            write(fromBean(value, null, new IdentityHashMap<>()), out, indent);
        }
    }

    private static void writeNativeArray(Object array, Appendable out, int indent)
    throws IOException {
        var length = Array.getLength(array);
        if (length == 0) {
            out.append("[]");
            return;
        }

        var child_indent = indent < 0 ? -1 : indent + 1;
        out.append('[');
        // the frequent primitive array types are emitted without boxing
        if (array instanceof int[] ints) {
            for (var i = 0; i < length; ++i) {
                if (i > 0) out.append(',');
                newlineAndIndent(out, child_indent);
                out.append(String.valueOf(ints[i]));
            }
        } else if (array instanceof long[] longs) {
            for (var i = 0; i < length; ++i) {
                if (i > 0) out.append(',');
                newlineAndIndent(out, child_indent);
                out.append(String.valueOf(longs[i]));
            }
        } else if (array instanceof double[] doubles) {
            for (var i = 0; i < length; ++i) {
                if (Double.isNaN(doubles[i]) || Double.isInfinite(doubles[i])) {
                    throw new IllegalArgumentException("JSON numbers can't be NaN or infinite");
                }
                if (i > 0) out.append(',');
                newlineAndIndent(out, child_indent);
                out.append(String.valueOf(doubles[i]));
            }
        } else {
            for (var i = 0; i < length; ++i) {
                if (i > 0) out.append(',');
                newlineAndIndent(out, child_indent);
                write(Array.get(array, i), out, child_indent);
            }
        }
        newlineAndIndent(out, indent);
        out.append(']');
    }

    private static void writeObject(Map<?, ?> map, Appendable out, int indent)
    throws IOException {
        if (map.isEmpty()) {
            out.append("{}");
            return;
        }

        var child_indent = indent < 0 ? -1 : indent + 1;
        out.append('{');
        var first = true;
        for (var entry : map.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            newlineAndIndent(out, child_indent);
            appendString(out, String.valueOf(entry.getKey()));
            out.append(':');
            if (indent >= 0) {
                out.append(' ');
            }
            write(entry.getValue(), out, child_indent);
        }
        newlineAndIndent(out, indent);
        out.append('}');
    }

    private static void writeArray(Collection<?> collection, Appendable out, int indent)
    throws IOException {
        if (collection.isEmpty()) {
            out.append("[]");
            return;
        }

        var child_indent = indent < 0 ? -1 : indent + 1;
        out.append('[');
        var first = true;
        for (var element : collection) {
            if (!first) {
                out.append(',');
            }
            first = false;
            newlineAndIndent(out, child_indent);
            write(element, out, child_indent);
        }
        newlineAndIndent(out, indent);
        out.append(']');
    }

    private static void appendString(Appendable out, String string)
    throws IOException {
        out.append('"');
        if (isPlainString(string)) {
            out.append(string);
        } else {
            out.append(encodeString(string));
        }
        out.append('"');
    }

    // mirrors the characters that the JSON encoding escapes
    private static boolean isPlainString(String string) {
        char previous = 0;
        for (var i = 0; i < string.length(); ++i) {
            var c = string.charAt(i);
            if (c == '"' || c == '\\' || c < ' ' ||
                (c == '/' && previous == '<') ||
                (c >= '\u0080' && c < '\u00a0') ||
                (c >= '\u2000' && c < '\u2100') ||
                Character.isSurrogate(c)) {
                return false;
            }
            previous = c;
        }
        return true;
    }

    private static String encodeString(String string) {
        var encoded = StringUtils.encodeJson(string);
        if (encoded == null || !hasUnpairedSurrogate(encoded)) {
            return encoded;
        }

        // escape unpaired surrogates so that the output is always
        // well-formed, while valid surrogate pairs are preserved as-is
        var out = new StringBuilder(encoded.length() + 5);
        for (var i = 0; i < encoded.length(); ++i) {
            var c = encoded.charAt(i);
            if (Character.isHighSurrogate(c) &&
                i + 1 < encoded.length() && Character.isLowSurrogate(encoded.charAt(i + 1))) {
                out.append(c).append(encoded.charAt(i + 1));
                i += 1;
            } else if (Character.isSurrogate(c)) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean hasUnpairedSurrogate(String string) {
        for (var i = 0; i < string.length(); ++i) {
            var c = string.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= string.length() || !Character.isLowSurrogate(string.charAt(i + 1))) {
                    return true;
                }
                i += 1;
            } else if (Character.isLowSurrogate(c)) {
                return true;
            }
        }
        return false;
    }

    private static void newlineAndIndent(Appendable out, int indent)
    throws IOException {
        if (indent < 0) {
            return;
        }
        out.append('\n');
        for (var i = 0; i < indent; ++i) {
            out.append("    ");
        }
    }
}
