/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The ClassUtils class provides useful utility methods for working with Java classes.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class ClassUtils {
    private ClassUtils() {
        // no-op
    }

    /**
     * Follows a type variable through the generic hierarchy of a class,
     * for instance resolving {@code T} of a supertype to the type argument
     * that the class provides for it.
     *
     * @param context  the class whose generic hierarchy provides the type
     *                 arguments
     * @param variable the type variable to resolve
     * @return the resolved type; or the type variable itself when the
     * hierarchy doesn't provide an argument for it
     * @since 1.10
     */
    public static Type resolveTypeVariable(Class<?> context, TypeVariable<?> variable) {
        var arguments = new HashMap<TypeVariable<?>, Type>();
        collectTypeArguments(context, arguments);

        Type resolved = variable;
        while (resolved instanceof TypeVariable<?> current) {
            var next = arguments.get(current);
            if (next == null || next == resolved) {
                break;
            }
            resolved = next;
        }
        return resolved;
    }

    /**
     * Erases a generic type to the class it will be at runtime, resolving
     * type variables through the generic hierarchy of the provided class.
     * An unresolved variable erases to its first bound and a wildcard to
     * its upper bound.
     *
     * @param context the class whose generic hierarchy provides the type
     *                arguments
     * @param type    the generic type to erase
     * @return the erased class; or {@code Object} when the type can't be
     * erased to anything more specific
     * @since 1.10
     */
    public static Class<?> erasedType(Class<?> context, Type type) {
        if (type instanceof TypeVariable<?> variable) {
            type = resolveTypeVariable(context, variable);
            if (type instanceof TypeVariable<?> unresolved) {
                return erasedType(context, unresolved.getBounds()[0]);
            }
        }
        if (type instanceof Class<?> klass) {
            return klass;
        }
        if (type instanceof ParameterizedType parameterized &&
            parameterized.getRawType() instanceof Class<?> raw) {
            return raw;
        }
        if (type instanceof GenericArrayType array) {
            return erasedType(context, array.getGenericComponentType()).arrayType();
        }
        if (type instanceof WildcardType wildcard) {
            return erasedType(context, wildcard.getUpperBounds()[0]);
        }
        return Object.class;
    }

    private static void collectTypeArguments(Type generic, Map<TypeVariable<?>, Type> arguments) {
        Class<?> raw = null;
        if (generic instanceof ParameterizedType parameterized &&
            parameterized.getRawType() instanceof Class<?> parameterized_raw) {
            raw = parameterized_raw;
            var variables = parameterized_raw.getTypeParameters();
            var actual = parameterized.getActualTypeArguments();
            for (var i = 0; i < variables.length; i++) {
                arguments.putIfAbsent(variables[i], actual[i]);
            }
        } else if (generic instanceof Class<?> klass) {
            raw = klass;
        }

        if (raw != null && raw != Object.class) {
            collectTypeArguments(raw.getGenericSuperclass(), arguments);
            for (var generic_interface : raw.getGenericInterfaces()) {
                collectTypeArguments(generic_interface, arguments);
            }
        }
    }

    /**
     * Returns true if the specified class is numeric.
     *
     * @param klass the class to check
     * @return true if the specified class is numeric, false otherwise
     * @since 1.0
     */
    public static boolean isNumeric(Class klass) {
        return Number.class.isAssignableFrom(klass) ||
            byte.class == klass ||
            short.class == klass ||
            int.class == klass ||
            long.class == klass ||
            float.class == klass ||
            double.class == klass;
    }

    /**
     * Returns true if the specified class is text.
     *
     * @param klass the class to check
     * @return true if the specified class is text, false otherwise
     * @since 1.0
     */
    public static boolean isText(Class klass) {
        return CharSequence.class.isAssignableFrom(klass) ||
            Character.class == klass ||
            char.class == klass;
    }

    /**
     * Returns true if the specified class is a basic type.
     *
     * @param klass the class to check
     * @return true if the specified class is a basic type, false otherwise
     * @since 1.0
     */
    public static boolean isBasic(Class klass) {
        if (null == klass) {
            return false;
        }

        return isNumeric(klass) ||
            boolean.class == klass ||
            Boolean.class == klass ||
            Date.class.isAssignableFrom(klass) ||
            klass.isEnum() ||
            isText(klass);
    }

    /**
     * Returns true if the specified class is from the JDK.
     *
     * @param klass the class to check
     * @return true if the specified class is from the JDK, false otherwise
     * @since 1.0
     */
    public static boolean isFromJdk(Class klass) {
        if (null == klass) {
            return false;
        }

        return isBasic(klass) || klass.getClassLoader() == Object.class.getClassLoader();
    }

    /**
     * Returns the simple name of the specified class without the package name.
     *
     * @param klass the class whose simple name to return
     * @return the simple name of the specified class
     * @since 1.0
     */
    public static String simpleClassName(Class klass) {
        var class_name = klass.getName();
        if (klass.getPackage() != null) {
            class_name = class_name.substring(klass.getPackage().getName().length() + 1);
        }

        return class_name;
    }

    /**
     * Returns a shortened version of the specified class name, with "$" characters replaced by underscores.
     *
     * @param klass the class whose name to shorten
     * @return a shortened version of the specified class name
     * @since 1.0
     */
    public static String shortenClassName(Class klass) {
        return simpleClassName(klass).replace('$', '_');
    }

    /**
     * Returns an array of the values of the enum constants of the specified class, or null if the class is not an enum.
     *
     * @param klass the class whose enum constant values to return
     * @return an array of the values of the enum constants of the specified class, or null if the class is not an enum
     * @since 1.0
     */
    public static String[] getEnumClassValues(Class klass) {
        if (klass.isEnum()) {
            var values = klass.getEnumConstants();
            return ArrayUtils.createStringArray(values);
        }

        return null;
    }
}