/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestClassUtils {
    public static class Base<T> {
        public Optional<T> getValue() { return Optional.empty(); }
        public List<T> getList() { return null; }
        public T[] getArray() { return null; }
    }

    public static class Middle<S> extends Base<S> {}

    public static class Concrete extends Middle<Integer> {}

    public static class Bounded<T extends Number> {
        public T getValue() { return null; }
    }

    @Test
    void testResolveTypeVariable()
    throws Exception {
        var variable = (java.lang.reflect.TypeVariable<?>) ((java.lang.reflect.ParameterizedType) Base.class.getMethod("getValue").getGenericReturnType()).getActualTypeArguments()[0];
        // resolves through multiple levels of the hierarchy
        assertEquals(Integer.class, ClassUtils.resolveTypeVariable(Concrete.class, variable));
        // stays the variable itself when the hierarchy doesn't provide an argument
        assertSame(variable, ClassUtils.resolveTypeVariable(Base.class, variable));
    }

    @Test
    void testErasedType()
    throws Exception {
        var optional_argument = ((java.lang.reflect.ParameterizedType) Base.class.getMethod("getValue").getGenericReturnType()).getActualTypeArguments()[0];
        var list_type = Base.class.getMethod("getList").getGenericReturnType();
        var array_type = Base.class.getMethod("getArray").getGenericReturnType();
        var bounded_type = Bounded.class.getMethod("getValue").getGenericReturnType();

        // a variable resolves against the context class
        assertSame(Integer.class, ClassUtils.erasedType(Concrete.class, optional_argument));
        // an unresolved variable erases to its first bound
        assertSame(Object.class, ClassUtils.erasedType(Base.class, optional_argument));
        assertSame(Number.class, ClassUtils.erasedType(Bounded.class, bounded_type));
        // a parameterized type erases to its raw class
        assertSame(List.class, ClassUtils.erasedType(Concrete.class, list_type));
        // a generic array reconstitutes the resolved array class
        assertSame(Integer[].class, ClassUtils.erasedType(Concrete.class, array_type));
        // plain classes pass through
        assertSame(String.class, ClassUtils.erasedType(Concrete.class, String.class));
    }

    @Test
    void testIsNumeric() {
        assertTrue(ClassUtils.isNumeric(Byte.class));
        assertTrue(ClassUtils.isNumeric(Short.class));
        assertTrue(ClassUtils.isNumeric(Integer.class));
        assertTrue(ClassUtils.isNumeric(Long.class));
        assertTrue(ClassUtils.isNumeric(Float.class));
        assertTrue(ClassUtils.isNumeric(Double.class));
        assertTrue(ClassUtils.isNumeric(BigDecimal.class));
        assertTrue(ClassUtils.isNumeric(BigInteger.class));
        assertTrue(ClassUtils.isNumeric(byte.class));
        assertTrue(ClassUtils.isNumeric(short.class));
        assertTrue(ClassUtils.isNumeric(int.class));
        assertTrue(ClassUtils.isNumeric(long.class));
        assertTrue(ClassUtils.isNumeric(float.class));
        assertTrue(ClassUtils.isNumeric(double.class));
        assertFalse(ClassUtils.isNumeric(Character.class));
        assertFalse(ClassUtils.isNumeric(String.class));
        assertFalse(ClassUtils.isNumeric(StringBuffer.class));
        assertFalse(ClassUtils.isNumeric(char.class));
        assertFalse(ClassUtils.isNumeric(Date.class));
    }

    @Test
    void testIsText() {
        assertFalse(ClassUtils.isText(Byte.class));
        assertFalse(ClassUtils.isText(Short.class));
        assertFalse(ClassUtils.isText(Integer.class));
        assertFalse(ClassUtils.isText(Long.class));
        assertFalse(ClassUtils.isText(Float.class));
        assertFalse(ClassUtils.isText(Double.class));
        assertFalse(ClassUtils.isText(BigDecimal.class));
        assertFalse(ClassUtils.isText(BigInteger.class));
        assertFalse(ClassUtils.isText(byte.class));
        assertFalse(ClassUtils.isText(short.class));
        assertFalse(ClassUtils.isText(int.class));
        assertFalse(ClassUtils.isText(long.class));
        assertFalse(ClassUtils.isText(float.class));
        assertFalse(ClassUtils.isText(double.class));
        assertTrue(ClassUtils.isText(Character.class));
        assertTrue(ClassUtils.isText(String.class));
        assertTrue(ClassUtils.isText(StringBuffer.class));
        assertTrue(ClassUtils.isText(char.class));
        assertFalse(ClassUtils.isText(Date.class));
    }

    @Test
    void testIsFromJdk() {
        assertTrue(ClassUtils.isFromJdk(Byte.class));
        assertTrue(ClassUtils.isFromJdk(Short.class));
        assertTrue(ClassUtils.isFromJdk(Integer.class));
        assertTrue(ClassUtils.isFromJdk(Long.class));
        assertTrue(ClassUtils.isFromJdk(Float.class));
        assertTrue(ClassUtils.isFromJdk(Double.class));
        assertTrue(ClassUtils.isFromJdk(BigDecimal.class));
        assertTrue(ClassUtils.isFromJdk(BigInteger.class));
        assertTrue(ClassUtils.isFromJdk(byte.class));
        assertTrue(ClassUtils.isFromJdk(short.class));
        assertTrue(ClassUtils.isFromJdk(int.class));
        assertTrue(ClassUtils.isFromJdk(long.class));
        assertTrue(ClassUtils.isFromJdk(float.class));
        assertTrue(ClassUtils.isFromJdk(double.class));
        assertTrue(ClassUtils.isFromJdk(Character.class));
        assertTrue(ClassUtils.isFromJdk(String.class));
        assertTrue(ClassUtils.isFromJdk(StringBuffer.class));
        assertTrue(ClassUtils.isFromJdk(char.class));
        assertTrue(ClassUtils.isFromJdk(Date.class));
        assertFalse(ClassUtils.isFromJdk(Interface1.class));
        assertFalse(ClassUtils.isFromJdk(getClass()));
    }

    @Test
    public void testSimpleClassName() {
        String simpleName = ClassUtils.simpleClassName(String.class);
        assertEquals("String", simpleName);
    }

    @Test
    public void testSimpleClassName2() {
        String simpleName = ClassUtils.simpleClassName(Outer.Inner.class);
        assertEquals("TestClassUtils$Outer$Inner", simpleName);
    }

    @Test
    public void testShortenClassName() {
        String shortenedName = ClassUtils.shortenClassName(Outer.Inner.class);
        assertEquals("TestClassUtils_Outer_Inner", shortenedName);
    }

    private static class Outer {
        private static class Inner {
        }
    }

    @Test
    public void testGetEnumClassValues() {
        String[] values = ClassUtils.getEnumClassValues(TestEnum.class);
        assertArrayEquals(new String[]{"ONE", "TWO", "THREE"}, values);
    }

    private enum TestEnum {
        ONE,
        TWO,
        THREE
    }

    @Test
    public void testGetEnumClassValuesNonEnum() {
        String[] values = ClassUtils.getEnumClassValues(String.class);
        assertNull(values);
    }
}

interface Interface1 {
}

interface Interface2 extends Interface1 {
}

class Parent implements Interface1 {
}

class Child1 extends Parent {
}

class Child2 extends Child1 implements Interface2, Interface1 {
}

class Child3 extends Parent {
}

class Child4 extends Object {
}
