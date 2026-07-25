/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.tools.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract class GenericTypeDetector {
    static Class detectAssociatedClass(Class beanClass, Method readMethod) {
        Class associated_class = null;
        Type generic_return_type = readMethod.getGenericReturnType();
        if (generic_return_type instanceof ParameterizedType) {
            Type[] type_args = ((ParameterizedType) generic_return_type).getActualTypeArguments();
            var erased = ClassUtils.erasedType(beanClass, type_args[0]);
            if (erased != Object.class) {
                associated_class = erased;
            }
        }

        return associated_class;
    }
}
