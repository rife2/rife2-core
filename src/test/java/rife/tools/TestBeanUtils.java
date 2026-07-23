/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.tools.exceptions.BeanUtilsException;
import rife.tools.exceptions.SerializationUtilsErrorException;
import rife.validation.ConstrainedProperty;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestBeanUtils {
    @BeforeEach
    public void setup() {
        RifeConfig.tools().setDefaultTimeZone(TimeZone.getTimeZone("EST"));
    }

    @AfterEach
    public void tearDown() {
        RifeConfig.tools().setDefaultTimeZone(null);
    }

    private BeanImpl getPopulatedBean() {
        BeanImpl bean = new BeanImpl();
        var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
        bean.setPropertyString("thisisastring");
        bean.setPropertyStringBuffer(new StringBuffer("butthisisastringbuffer"));
        bean.setPropertyDate(Convert.toDate(cal));
        bean.setPropertyCalendar(cal);
        bean.setPropertySqlDate(Convert.toSqlDate(cal));
        bean.setPropertyTime(Convert.toSqlTime(cal));
        bean.setPropertyTimestamp(Convert.toSqlTimestamp(cal));
        bean.setPropertyInstant(Convert.toInstant(cal));
        bean.setPropertyLocalDateTime(Convert.toLocalDateTime(cal));
        bean.setPropertyLocalDate(Convert.toLocalDate(cal));
        bean.setPropertyLocalTime(Convert.toLocalTime(cal));
        bean.setPropertyChar('g');
        bean.setPropertyBoolean(false);
        bean.setPropertyByte((byte) 53);
        bean.setPropertyDouble(84578.42d);
        bean.setPropertyFloat(35523.967f);
        bean.setPropertyInt(978);
        bean.setPropertyLong(87346L);
        bean.setPropertyShort((short) 31);
        bean.setPropertyBigDecimal(new BigDecimal("8347365990.387437894678"));

        return bean;
    }

    @Test
    void testSetUppercaseBeanPropertyIllegalArguments()
    throws BeanUtilsException {
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        try {
            BeanUtils.setUppercasedBeanProperty(null, null, null, bean_properties, new BeanImpl2(), null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, null, new BeanImpl2(), null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, new BeanImpl2(), null);
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException not expected.");
        }
    }

    @Test
    void testSetUppercaseBeanPropertyNoOpArguments()
    throws BeanUtilsException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, bean, null);
        assertNull(bean.getPropertyString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[0], null, bean_properties, bean, null);
        assertNull(bean.getPropertyString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals("one", bean.getPropertyString());
    }

    @Test
    void testSetUppercaseBeanPropertyNoSetter()
    throws BeanUtilsException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        assertEquals(23L, bean.getPropertyReadonly());
        BeanUtils.setUppercasedBeanProperty("propertyReadonly", new String[]{"42131"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(23L, bean.getPropertyReadonly());
    }

    @Test
    void testSetUppercaseBeanProperty()
    throws BeanUtilsException, ParseException, SerializationUtilsErrorException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals("one", bean.getPropertyString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyInt", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(438, bean.getPropertyInt());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyChar", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals('E', bean.getPropertyChar());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBoolean", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertTrue(bean.isPropertyBoolean());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(27, bean.getPropertyByte());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(80756.6287d, bean.getPropertyDouble());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(435.557f, bean.getPropertyFloat());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(122875, bean.getPropertyLong());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(3285, bean.getPropertyShort());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"983743.343", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(new BigDecimal("983743.343"), bean.getPropertyBigDecimal());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(438, bean.getPropertyIntegerObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObject", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals('E', bean.getPropertyCharacterObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObject", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(Boolean.TRUE, bean.getPropertyBooleanObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals((byte) 27, bean.getPropertyByteObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(80756.6287d, bean.getPropertyDoubleObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(435.557f, bean.getPropertyFloatObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(122875, bean.getPropertyLongObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals((short) 3285, bean.getPropertyShortObject());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuffer", new String[]{"one1", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals("one1", bean.getPropertyStringBuffer().toString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilder", new String[]{"one2", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals("one2", bean.getPropertyStringBuilder().toString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDate", new String[]{"2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDate(), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyInstant", new String[]{"2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyInstant(), Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateTime", new String[]{"2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLocalDateTime(), Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDate", new String[]{"2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLocalDate(), Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 00:00")));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalTime", new String[]{"10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLocalTime(), Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("10:45")));


        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringArray", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new String[]{"one", "two"}, bean.getPropertyStringArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntArray", new String[]{"438", "98455", "711"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new int[]{438, 98455, 711}, bean.getPropertyIntArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharArray", new String[]{"E", "a", "x"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new char[]{'E', 'a', 'x'}, bean.getPropertyCharArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanArray", new String[]{"true", "0", "t", "1"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new boolean[]{true, false, true, true}, bean.getPropertyBooleanArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new byte[]{27, 78}, bean.getPropertyByteArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80756.6287", "3214.75", "85796.6237"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new double[]{80756.6287d, 3214.75d, 85796.6237d}, bean.getPropertyDoubleArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435.557", "589.5"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new float[]{435.557f, 589.5f}, bean.getPropertyFloatArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"122875", "8526780", "3826589"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new long[]{122875, 8526780, 3826589}, bean.getPropertyLongArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"3285", "58"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new short[]{3285, 58}, bean.getPropertyShortArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"438", "7865", "475"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Integer[]{438, 7865, 475}, bean.getPropertyIntegerObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObjectArray", new String[]{"E", "z"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Character[]{'E', 'z'}, bean.getPropertyCharacterObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObjectArray", new String[]{"fslse", "1", "true"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Boolean[]{false, true, true}, bean.getPropertyBooleanObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Byte[]{(byte) 27, (byte) 78}, bean.getPropertyByteObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80756.6287", "5876.14", "3268.57"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Double[]{80756.6287d, 5876.14d, 3268.57d}, bean.getPropertyDoubleObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435.557", "7865.66"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Float[]{435.557f, 7865.66f}, bean.getPropertyFloatObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"122875", "5687621", "66578"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Long[]{122875L, 5687621L, 66578L}, bean.getPropertyLongObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"3285", "6588"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new Short[]{(short) 3285, (short) 6588}, bean.getPropertyShortObjectArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"32859837434343983.83749837498373434", "65884343.343"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new BigDecimal[]{new BigDecimal("32859837434343983.83749837498373434"), new BigDecimal("65884343343E-3")}, bean.getPropertyBigDecimalArray());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBufferArray", new String[]{"one1", "two2"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new String[]{"one1", "two2"}, ArrayUtils.createStringArray(bean.getPropertyStringBufferArray()));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilderArray", new String[]{"three3", "four4"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(new String[]{"three3", "four4"}, ArrayUtils.createStringArray(bean.getPropertyStringBuilderArray()));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDateArray", new String[]{"2006-08-04 10:45", "2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDateArray(), new Date[]{RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05")});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyInstantArray", new String[]{"2006-08-04 10:45", "2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyInstantArray(), new Instant[]{Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")), Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05"))});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateTimeArray", new String[]{"2006-08-04 10:45", "2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLocalDateTimeArray(), new LocalDateTime[]{Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")), Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05"))});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateArray", new String[]{"2006-08-04 10:45", "2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLocalDateArray(), new LocalDate[]{Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 00:00")), Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 00:00"))});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLocalTimeArray", new String[]{"10:45", "11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLocalTimeArray(), new LocalTime[]{Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("10:45")), Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("11:05"))});
    }

    @Test
    void testSetUppercaseBeanPropertyConstrained()
    throws BeanUtilsException, ParseException, SerializationUtilsErrorException {
        BeanImpl3 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl3.class);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDate", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDate(), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyInstant", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyInstant(), Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateTime", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLocalDateTime(), Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDate", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLocalDate(), Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 00:00")));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalTime", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLocalTime(), Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("10:45")));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyInt", new String[]{"$438", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(438, bean.getPropertyInt());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(27, bean.getPropertyByte());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(80756.6287d, bean.getPropertyDouble());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(435.557f, bean.getPropertyFloat());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(122875, bean.getPropertyLong());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(3285, bean.getPropertyShort());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"4353344987349830948394893,55709384093", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(new BigDecimal("435334498734983094839489355709384093E-11"), bean.getPropertyBigDecimal());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"$438", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(438, bean.getPropertyIntegerObject());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals((byte) 27, bean.getPropertyByteObject());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(80756.6287d, bean.getPropertyDoubleObject());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(435.557f, bean.getPropertyFloatObject());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(122875, bean.getPropertyLongObject());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals((short) 3285, bean.getPropertyShortObject());

        bean = new BeanImpl3();
        BeanImpl3.SerializableType serializable = new BeanImpl3.SerializableType(5686, "Testing");
        BeanUtils.setUppercasedBeanProperty("propertySerializableType", new String[]{SerializationUtils.serializeToString(serializable), "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertySerializableType(), serializable);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDateArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDateArray(), new Date[]{RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05")});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyInstantArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyInstantArray(), new Instant[]{Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")), Convert.toInstant(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05"))});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateTimeArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLocalDateTimeArray(), new LocalDateTime[]{Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45")), Convert.toLocalDateTime(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05"))});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalDateArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLocalDateArray(), new LocalDate[]{Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 00:00")), Convert.toLocalDate(RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 00:00"))});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLocalTimeArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLocalTimeArray(), new LocalTime[]{Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("10:45")), Convert.toLocalTime(RifeConfig.tools().getDefaultInputTimeFormat().parse("11:05"))});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntArray", new String[]{"$438", "$98455", "$711"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new int[]{438, 98455, 711}, bean.getPropertyIntArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new byte[]{27, 78}, bean.getPropertyByteArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80,756.6287", "3,214.75", "85,796.6237"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new double[]{80756.6287d, 3214.75d, 85796.6237d}, bean.getPropertyDoubleArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435,557", "589,5"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new float[]{435.557f, 589.5f}, bean.getPropertyFloatArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"$122,875.00", "$8,526,780.00", "$3,826,589.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new long[]{122875, 8526780, 3826589}, bean.getPropertyLongArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"¤3285", "¤58"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new short[]{3285, 58}, bean.getPropertyShortArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"$438", "$7865", "$475"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Integer[]{438, 7865, 475}, bean.getPropertyIntegerObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Byte[]{(byte) 27, (byte) 78}, bean.getPropertyByteObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80,756.6287", "5,876.14", "3,268.57"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Double[]{80756.6287d, 5876.14d, 3268.57d}, bean.getPropertyDoubleObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435,557", "7865,66"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Float[]{435.557f, 7865.66f}, bean.getPropertyFloatObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"$122,875.00", "$5,687,621.00", "$66,578.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Long[]{122875L, 5687621L, 66578L}, bean.getPropertyLongObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"¤3285", "¤6588"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new Short[]{(short) 3285, (short) 6588}, bean.getPropertyShortObjectArray());

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"97687687998978673545669789,0000000000001", "34353"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(new BigDecimal[]{new BigDecimal("976876879989786735456697890000000000001E-13"), new BigDecimal("3.4353E4")}, bean.getPropertyBigDecimalArray());

        bean = new BeanImpl3();
        BeanImpl3.SerializableType serializable1 = new BeanImpl3.SerializableType(5682, "AnotherTest");
        BeanImpl3.SerializableType serializable2 = new BeanImpl3.SerializableType(850, "WhatTest");
        BeanUtils.setUppercasedBeanProperty("propertySerializableTypeArray", new String[]{SerializationUtils.serializeToString(serializable1), SerializationUtils.serializeToString(serializable2)}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertySerializableTypeArray(), new BeanImpl3.SerializableType[]{serializable1, serializable2});
    }

    @Test
    void testPropertyNamesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyNames(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesEmpty() {
        try {
            assertEquals(0, BeanUtils.getPropertyNames(Object.class, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNames() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class, null, null, null);
            assertEquals(20, property_names.size());
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, property_names.size());
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, property_names.size());
            assertTrue(property_names.contains("propertyWriteOnly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(20, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyWriteOnly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncluded() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(9, property_names.size());
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncludedGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(10, property_names.size());
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncludedSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(10, property_names.size());
            assertTrue(property_names.contains("propertyWriteOnly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncludedPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(9, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncludedPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(10, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesIncludedPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(10, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyWriteOnly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcluded() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(12, property_names.size());
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcludedGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(13, property_names.size());
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcludedSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(13, property_names.size());
            assertTrue(property_names.contains("propertyWriteOnly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyLocalDateTime"));
            assertTrue(property_names.contains("propertyLocalTime"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcludedPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(12, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcludedPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(13, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesExcludedPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(13, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyWriteOnly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyLocalDateTime"));
            assertTrue(property_names.contains("PREFIX:propertyLocalTime"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFiltered() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyLocalDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(5, property_names.size());
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFilteredGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyLocalDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(6, property_names.size());
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFilteredSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyLocalDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(6, property_names.size());
            assertTrue(property_names.contains("propertyWriteOnly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyInstant"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFilteredPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(5, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFilteredPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(6, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyNamesFilteredPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(6, property_names.size());
            assertTrue(property_names.contains("PREFIX:propertyWriteOnly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyInstant"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIllegal() {
        try {
            assertEquals(0, BeanUtils.countProperties(null, null, null, null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountProperties() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class, null, null, null);
            assertEquals(20, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefix() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(20, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefixGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefixSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncluded() {
        try {
            assertEquals(9, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncludedGetters() {
        try {
            assertEquals(10, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncludedSetters() {
        try {
            assertEquals(10, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncludedPrefix() {
        try {
            assertEquals(9, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncludedPrefixGetters() {
        try {
            assertEquals(10, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesIncludedPrefixSetters() {
        try {
            assertEquals(10, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcluded() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(12, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcludedGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(13, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcludedSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(13, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcludedPrefix() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(12, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcludedPrefixGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(13, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesExcludedPrefixSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(13, count);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFiltered() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyLocalDate", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFilteredGetters() {
        try {
            assertEquals(5, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyLocalDate", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFilteredSetters() {
        try {
            assertEquals(5, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyLocalDate", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFilteredPrefix() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyLocalDate", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFilteredPrefixGetters() {
        try {
            assertEquals(5, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyLocalDate", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesFilteredPrefixSetters() {
        try {
            assertEquals(5, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyLocalDate", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypeIllegal() {
        try {
            BeanUtils.getPropertyType(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyType(Object.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyType(Object.class, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyType() {
        try {
            assertSame(String.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyString"));
            assertSame(StringBuffer.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyStringBuffer"));
            assertSame(Date.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyDate"));
            assertSame(Calendar.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyCalendar"));
            assertSame(java.sql.Date.class, BeanUtils.getPropertyType(BeanImpl.class, "propertySqlDate"));
            assertSame(java.sql.Time.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyTime"));
            assertSame(java.sql.Timestamp.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyTimestamp"));
            assertSame(Instant.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyInstant"));
            assertSame(LocalDateTime.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalDateTime"));
            assertSame(LocalDate.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalDate"));
            assertSame(LocalTime.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalTime"));
            assertSame(char.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyChar"));
            assertSame(boolean.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyBoolean"));
            assertSame(byte.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyByte"));
            assertSame(double.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyDouble"));
            assertSame(float.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyFloat"));
            assertSame(int.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyInt"));
            assertSame(long.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyLong"));
            assertSame(short.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyShort"));
            assertSame(BigDecimal.class, BeanUtils.getPropertyType(BeanImpl.class, "propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            assertSame(String.class, BeanUtils.getPropertyType(BeanImpl.class, "unknown"));
            fail();
        } catch (BeanUtilsException e) {
            assertSame(BeanImpl.class, e.getBeanClass());
        }
    }

    @Test
    void testPropertyTypesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyTypes(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypes() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class, null, null, null);
            assertEquals(20, property_types.size());
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(Calendar.class, property_types.get("propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(boolean.class, property_types.get("propertyBoolean"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
            assertSame(BigDecimal.class, property_types.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, property_types.size());
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(int.class, property_types.get("propertyReadonly"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(Calendar.class, property_types.get("propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(boolean.class, property_types.get("propertyBoolean"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
            assertSame(BigDecimal.class, property_types.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(21, property_types.size());
            assertTrue(property_types.containsKey("propertyWriteOnly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(long.class, property_types.get("propertyWriteOnly"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(Calendar.class, property_types.get("propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(boolean.class, property_types.get("propertyBoolean"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
            assertSame(BigDecimal.class, property_types.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(20, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(Calendar.class, property_types.get("PREFIX:propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(boolean.class, property_types.get("PREFIX:propertyBoolean"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
            assertSame(BigDecimal.class, property_types.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(int.class, property_types.get("PREFIX:propertyReadonly"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(Calendar.class, property_types.get("PREFIX:propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(boolean.class, property_types.get("PREFIX:propertyBoolean"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
            assertSame(BigDecimal.class, property_types.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(21, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyWriteOnly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(long.class, property_types.get("PREFIX:propertyWriteOnly"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(Calendar.class, property_types.get("PREFIX:propertyCalendar"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(boolean.class, property_types.get("PREFIX:propertyBoolean"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
            assertSame(BigDecimal.class, property_types.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncluded() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(9, property_types.size());
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncludedGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(10, property_types.size());
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(int.class, property_types.get("propertyReadonly"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncludedSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(10, property_types.size());
            assertTrue(property_types.containsKey("propertyWriteOnly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(long.class, property_types.get("propertyWriteOnly"));
            assertSame(String.class, property_types.get("propertyString"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDate.class, property_types.get("propertyLocalDate"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(float.class, property_types.get("propertyFloat"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncludedPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(9, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncludedPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(10, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(int.class, property_types.get("PREFIX:propertyReadonly"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesIncludedPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(10, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyWriteOnly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(long.class, property_types.get("PREFIX:propertyWriteOnly"));
            assertSame(String.class, property_types.get("PREFIX:propertyString"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Date.class, property_types.get("PREFIX:propertySqlDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDate.class, property_types.get("PREFIX:propertyLocalDate"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(float.class, property_types.get("PREFIX:propertyFloat"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcluded() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(13, property_types.size());
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcludedGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(14, property_types.size());
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(int.class, property_types.get("propertyReadonly"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcludedSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(14, property_types.size());
            assertTrue(property_types.containsKey("propertyWriteOnly"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDateTime"));
            assertTrue(property_types.containsKey("propertyLocalTime"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(long.class, property_types.get("propertyWriteOnly"));
            assertSame(StringBuffer.class, property_types.get("propertyStringBuffer"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("propertyTimestamp"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("propertyLocalTime"));
            assertSame(char.class, property_types.get("propertyChar"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(double.class, property_types.get("propertyDouble"));
            assertSame(int.class, property_types.get("propertyInt"));
            assertSame(long.class, property_types.get("propertyLong"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcludedPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(13, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcludedPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(14, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(int.class, property_types.get("PREFIX:propertyReadonly"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesExcludedPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(14, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyWriteOnly"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(long.class, property_types.get("PREFIX:propertyWriteOnly"));
            assertSame(StringBuffer.class, property_types.get("PREFIX:propertyStringBuffer"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(java.sql.Timestamp.class, property_types.get("PREFIX:propertyTimestamp"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(LocalDateTime.class, property_types.get("PREFIX:propertyLocalDateTime"));
            assertSame(LocalTime.class, property_types.get("PREFIX:propertyLocalTime"));
            assertSame(char.class, property_types.get("PREFIX:propertyChar"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(double.class, property_types.get("PREFIX:propertyDouble"));
            assertSame(int.class, property_types.get("PREFIX:propertyInt"));
            assertSame(long.class, property_types.get("PREFIX:propertyLong"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFiltered() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(5, property_types.size());
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFilteredGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(6, property_types.size());
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(int.class, property_types.get("propertyReadonly"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFilteredSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyInstant", "propertyLocalDate", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(6, property_types.size());
            assertTrue(property_types.containsKey("propertyWriteOnly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(long.class, property_types.get("propertyWriteOnly"));
            assertSame(Date.class, property_types.get("propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("propertyTime"));
            assertSame(Instant.class, property_types.get("propertyInstant"));
            assertSame(byte.class, property_types.get("propertyByte"));
            assertSame(short.class, property_types.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFilteredPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(5, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFilteredPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(6, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(int.class, property_types.get("PREFIX:propertyReadonly"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesFilteredPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(6, property_types.size());
            assertTrue(property_types.containsKey("PREFIX:propertyWriteOnly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(long.class, property_types.get("PREFIX:propertyWriteOnly"));
            assertSame(Date.class, property_types.get("PREFIX:propertyDate"));
            assertSame(java.sql.Time.class, property_types.get("PREFIX:propertyTime"));
            assertSame(Instant.class, property_types.get("PREFIX:propertyInstant"));
            assertSame(byte.class, property_types.get("PREFIX:propertyByte"));
            assertSame(short.class, property_types.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValueIllegal() {
        try {
            BeanUtils.getPropertyValue(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(Object.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(new Object(), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(new Object(), "");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValue() {
        Object bean = getPopulatedBean();
        try {
            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", BeanUtils.getPropertyValue(bean, "propertyString"));
            assertEquals("butthisisastringbuffer", BeanUtils.getPropertyValue(bean, "propertyStringBuffer").toString());
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyDate"), Convert.toDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyCalendar"), cal);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTime"), Convert.toSqlTime(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyInstant"), Convert.toInstant(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', BeanUtils.getPropertyValue(bean, "propertyChar"));
            assertEquals(Boolean.FALSE, BeanUtils.getPropertyValue(bean, "propertyBoolean"));
            assertEquals((byte) 53, BeanUtils.getPropertyValue(bean, "propertyByte"));
            assertEquals(84578.42d, BeanUtils.getPropertyValue(bean, "propertyDouble"));
            assertEquals(35523.967f, BeanUtils.getPropertyValue(bean, "propertyFloat"));
            assertEquals(978, BeanUtils.getPropertyValue(bean, "propertyInt"));
            assertEquals(87346L, BeanUtils.getPropertyValue(bean, "propertyLong"));
            assertEquals((short) 31, BeanUtils.getPropertyValue(bean, "propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), BeanUtils.getPropertyValue(bean, "propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(bean, "unknown");
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), bean.getClass());
        }
    }

    @Test
    void testSetPropertyValue() {
        BeanImpl bean = new BeanImpl();
        try {
            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            BeanUtils.setPropertyValue(bean, "propertyString", "thisisastring");
            BeanUtils.setPropertyValue(bean, "propertyStringBuffer", new StringBuffer("butthisisastringbuffer"));
            BeanUtils.setPropertyValue(bean, "propertyDate", Convert.toDate(cal));
            BeanUtils.setPropertyValue(bean, "propertyCalendar", cal);
            BeanUtils.setPropertyValue(bean, "propertySqlDate", Convert.toSqlDate(cal));
            BeanUtils.setPropertyValue(bean, "propertyTime", Convert.toSqlTime(cal));
            BeanUtils.setPropertyValue(bean, "propertyTimestamp", Convert.toSqlTimestamp(cal));
            BeanUtils.setPropertyValue(bean, "propertyInstant", Convert.toInstant(cal));
            BeanUtils.setPropertyValue(bean, "propertyLocalDateTime", Convert.toLocalDateTime(cal));
            BeanUtils.setPropertyValue(bean, "propertyLocalDate", Convert.toLocalDate(cal));
            BeanUtils.setPropertyValue(bean, "propertyLocalTime", Convert.toLocalTime(cal));
            BeanUtils.setPropertyValue(bean, "propertyChar", 'g');
            BeanUtils.setPropertyValue(bean, "propertyBoolean", Boolean.FALSE);
            BeanUtils.setPropertyValue(bean, "propertyByte", (byte) 53);
            BeanUtils.setPropertyValue(bean, "propertyDouble", 84578.42d);
            BeanUtils.setPropertyValue(bean, "propertyFloat", 35523.967f);
            BeanUtils.setPropertyValue(bean, "propertyInt", 978);
            BeanUtils.setPropertyValue(bean, "propertyLong", 87346L);
            BeanUtils.setPropertyValue(bean, "propertyShort", (short) 31);
            BeanUtils.setPropertyValue(bean, "propertyBigDecimal", new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        BeanImpl populated = getPopulatedBean();
        assertEquals(bean.getPropertyString(), populated.getPropertyString());
        assertEquals(bean.getPropertyStringBuffer().toString(), populated.getPropertyStringBuffer().toString());
        assertEquals(bean.getPropertyDate(), populated.getPropertyDate());
        assertEquals(bean.getPropertyCalendar(), populated.getPropertyCalendar());
        assertEquals(bean.getPropertySqlDate(), populated.getPropertySqlDate());
        assertEquals(bean.getPropertyTime(), populated.getPropertyTime());
        assertEquals(bean.getPropertyTimestamp(), populated.getPropertyTimestamp());
        assertEquals(bean.getPropertyChar(), populated.getPropertyChar());
        assertEquals(bean.isPropertyBoolean(), populated.isPropertyBoolean());
        assertEquals(bean.getPropertyByte(), populated.getPropertyByte());
        assertEquals(bean.getPropertyDouble(), populated.getPropertyDouble());
        assertEquals(bean.getPropertyFloat(), populated.getPropertyFloat());
        assertEquals(bean.getPropertyInt(), populated.getPropertyInt());
        assertEquals(bean.getPropertyLong(), populated.getPropertyLong());
        assertEquals(bean.getPropertyShort(), populated.getPropertyShort());
        assertEquals(bean.getPropertyBigDecimal(), populated.getPropertyBigDecimal());

        try {
            BeanUtils.setPropertyValue(bean, "unknown", "ok");
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), bean.getClass());
        }
    }

    @Test
    void testGetPropertyValuesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyValues(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValues(Object.class, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValues() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(), null, null, null);
            assertEquals(20, property_values.size());
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("propertyBoolean"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, null);
            assertEquals(21, property_values.size());
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("propertyReadonly"));
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("propertyBoolean"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, null);
            assertEquals(20, property_values.size());
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("propertyBoolean"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(20, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("PREFIX:propertyBoolean"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(21, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("PREFIX:propertyReadonly"));
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("PREFIX:propertyBoolean"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(20, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals(Boolean.FALSE, property_values.get("PREFIX:propertyBoolean"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
            assertEquals(new BigDecimal("8347365990.387437894678"), property_values.get("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncluded() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(9, property_values.size());
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncludedGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(10, property_values.size());
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("propertyReadonly"));
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncludedSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(9, property_values.size());
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDate"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("propertyString"));
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(35523.967f, property_values.get("propertyFloat"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncludedPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(9, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncludedPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(10, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("PREFIX:propertyReadonly"));
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesIncludedPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(9, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("thisisastring", property_values.get("PREFIX:propertyString"));
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(35523.967f, property_values.get("PREFIX:propertyFloat"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcluded() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(13, property_values.size());
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcludedGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(14, property_values.size());
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("propertyReadonly"));
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcludedSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(13, property_values.size());
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyLocalDateTime"));
            assertTrue(property_values.containsKey("propertyLocalTime"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("butthisisastringbuffer", property_values.get("propertyStringBuffer").toString());
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("propertyChar"));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals(84578.42d, property_values.get("propertyDouble"));
            assertEquals(978, property_values.get("propertyInt"));
            assertEquals(87346L, property_values.get("propertyLong"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcludedPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(13, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcludedPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(14, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("PREFIX:propertyReadonly"));
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesExcludedPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(13, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalDateTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyLocalTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals("butthisisastringbuffer", property_values.get("PREFIX:propertyStringBuffer").toString());
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals('g', property_values.get("PREFIX:propertyChar"));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals(84578.42d, property_values.get("PREFIX:propertyDouble"));
            assertEquals(978, property_values.get("PREFIX:propertyInt"));
            assertEquals(87346L, property_values.get("PREFIX:propertyLong"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFiltered() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(5, property_values.size());
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFilteredGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(6, property_values.size());
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("propertyReadonly"));
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFilteredSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteOnly", "propertyString", "propertyDate", "propertySqlDate",
                    "propertyInstant", "propertyLocalDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyLocalDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(5, property_values.size());
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("propertyByte"));
            assertEquals((short) 31, property_values.get("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFilteredPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(5, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFilteredPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(6, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(23, property_values.get("PREFIX:propertyReadonly"));
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesFilteredPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteOnly", "PREFIX:propertyString", "PREFIX:propertyDate",
                    "PREFIX:propertyInstant", "PREFIX:propertyLocalDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyLocalDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(5, property_values.size());
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals((byte) 53, property_values.get("PREFIX:propertyByte"));
            assertEquals((short) 31, property_values.get("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @SuppressWarnings("deprecated")
    @Test
    void testFormatPropertyValues() {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        assertEquals("20230123134523142-0500", BeanUtils.formatPropertyValue(Convert.toDate(cal), null));
        assertEquals("20230123134523142-0500", BeanUtils.formatPropertyValue(Convert.toInstant(cal), null));
        assertEquals("20230123134523142-0500", BeanUtils.formatPropertyValue(Convert.toLocalDateTime(cal), null));
        assertEquals("20230123000000000-0500", BeanUtils.formatPropertyValue(Convert.toLocalDate(cal), null));
        assertEquals("134523142-0500", BeanUtils.formatPropertyValue(Convert.toLocalTime(cal), null));
    }

    @Test
    void testFormatPropertyValuesConstrained() {
        var cal = RifeConfig.tools().getCalendarInstance(2023, Calendar.JANUARY, 23, 13, 45, 23, 142);
        var format = RifeConfig.tools().getSimpleDateFormat("d MMM yyyy HH:mm:ss");
        assertEquals("23 Jan 2023 13:45:23", BeanUtils.formatPropertyValue(Convert.toDate(cal), new ConstrainedProperty("property").format(format)));
        assertEquals("23 Jan 2023 13:45:23", BeanUtils.formatPropertyValue(Instant.parse("2023-01-23T18:45:23.00Z"), new ConstrainedProperty("property").format(format)));
        assertEquals("23 Jan 2023 13:45:23", BeanUtils.formatPropertyValue(LocalDateTime.of(2023, Month.JANUARY, 23, 13, 45, 23, 142000000), new ConstrainedProperty("property").format(format)));
        assertEquals("23 Jan 2023 00:00:00", BeanUtils.formatPropertyValue(LocalDate.of(2023, Month.JANUARY, 23), new ConstrainedProperty("property").format(format)));
        assertEquals("1 Jan 1970 13:45:23", BeanUtils.formatPropertyValue(LocalTime.of(13, 45, 23, 142000000), new ConstrainedProperty("property").format(format)));
    }

    @Nested
    class TestOptionalGetters {
        public static class BeanOptional {
            private Optional<String> present = Optional.of("hello");

            public Optional<String> getPropertyOptionalPresent() { return present; }
            public void setPropertyOptionalPresent(Optional<String> v) { this.present = v; }

            public Optional<String> getPropertyOptionalEmpty() { return Optional.empty(); }
            public void setPropertyOptionalEmpty(Optional<String> v) {}

            public Optional<Integer> getPropertyOptionalInt() { return Optional.of(42); }
            public void setPropertyOptionalInt(Optional<Integer> v) {}

            public String getPropertyRegular() { return "regular"; }
            public void setPropertyRegular(String v) {}
        }

        public static class BeanOptionalOnlyGetter {
            public Optional<String> getPropertyOnlyGetter() { return Optional.of("only"); }
        }

        @Test
        void testGetPropertyValueOptionalPresent() {
            try {
                var bean = new BeanOptional();
                Object value = BeanUtils.getPropertyValue(bean, "propertyOptionalPresent");
                assertEquals("hello", value);
                assertFalse(value instanceof Optional);
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testGetPropertyValueOptionalEmpty() {
            try {
                var bean = new BeanOptional();
                Object value = BeanUtils.getPropertyValue(bean, "propertyOptionalEmpty");
                assertNull(value);
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testGetPropertyTypeOptional() {
            try {
                assertSame(String.class, BeanUtils.getPropertyType(BeanOptional.class, "propertyOptionalPresent"));
                assertSame(String.class, BeanUtils.getPropertyType(BeanOptional.class, "propertyOptionalEmpty"));
                assertSame(Integer.class, BeanUtils.getPropertyType(BeanOptional.class, "propertyOptionalInt"));
                assertSame(String.class, BeanUtils.getPropertyType(BeanOptional.class, "propertyRegular"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testGetPropertyTypesOptional() {
            try {
                var types = BeanUtils.getPropertyTypes(BeanOptional.class, null, null, null);
                assertSame(String.class, types.get("propertyOptionalPresent"));
                assertSame(String.class, types.get("propertyOptionalEmpty"));
                assertSame(Integer.class, types.get("propertyOptionalInt"));
                assertSame(String.class, types.get("propertyRegular"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testGetPropertyValuesOptional() {
            try {
                var bean = new BeanOptional();
                var values = BeanUtils.getPropertyValues(bean, null, null, null);
                assertEquals("hello", values.get("propertyOptionalPresent"));
                assertNull(values.get("propertyOptionalEmpty"));
                assertEquals(42, values.get("propertyOptionalInt"));
                assertEquals("regular", values.get("propertyRegular"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testProcessPropertyValuesOptional() {
            try {
                var bean = new BeanOptional();
                var seen = new HashMap<String, Object>();
                BeanUtils.processPropertyValues(bean, null, null, null, (name, descriptor, value, constrainedProperty) -> seen.put(name, value));
                assertEquals("hello", seen.get("propertyOptionalPresent"));
                assertNull(seen.get("propertyOptionalEmpty"));
                assertEquals(42, seen.get("propertyOptionalInt"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testFormatPropertyValueOptional() {
            assertEquals("hello", BeanUtils.formatPropertyValue(Optional.of("hello"), null));
            assertNull(BeanUtils.formatPropertyValue(Optional.empty(), null));
            assertEquals("regular", BeanUtils.formatPropertyValue("regular", null));
        }

        @Test
        void testPropertyNamesOptional() {
            try {
                Set<String> names = BeanUtils.getPropertyNames(BeanOptional.class, null, null, null);
                assertTrue(names.contains("propertyOptionalPresent"));
                assertTrue(names.contains("propertyOptionalEmpty"));
                assertTrue(names.contains("propertyOptionalInt"));
                assertTrue(names.contains("propertyRegular"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        @Test
        void testPropertyTypesGettersOptionalOnly() {
            try {
                var types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanOptionalOnlyGetter.class, null, null, null);
                assertEquals(1, types.size());
                assertSame(String.class, types.get("propertyOnlyGetter"));
                var values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, new BeanOptionalOnlyGetter(), null, null, null);
                assertEquals("only", values.get("propertyOnlyGetter"));
            } catch (BeanUtilsException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
