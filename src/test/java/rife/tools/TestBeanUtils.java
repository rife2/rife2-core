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
import rife.engine.UploadedFile;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.beans.PropertyDescriptor;
import java.io.File;
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
        assertEquals(bean.getPropertyString(), "one");
    }

    @Test
    void testSetUppercaseBeanPropertyNoSetter()
    throws BeanUtilsException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        assertEquals(bean.getPropertyReadonly(), 23L);
        BeanUtils.setUppercasedBeanProperty("propertyReadonly", new String[]{"42131"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyReadonly(), 23L);
    }

    public static class UploadGuardBean extends Validation {
        private String locked_ = "untouched";
        private String open_ = null;

        protected void activateValidation() {
            addConstraint(new ConstrainedProperty("locked").editable(false));
            addConstraint(new ConstrainedProperty("open"));
        }

        public void setLocked(String locked) {
            locked_ = locked;
        }

        public String getLocked() {
            return locked_;
        }

        public void setOpen(String open) {
            open_ = open;
        }

        public String getOpen() {
            return open_;
        }
    }

    @Test
    void testSetUppercasedBeanPropertyFileHonorsEditable()
    throws Exception {
        var file = File.createTempFile("rifetest", ".txt");
        file.deleteOnExit();
        FileUtils.writeString("uploaded content", file);
        try (var uploaded = new UploadedFile("upload.txt", "text/plain")) {
            uploaded.setTempFile(file);

            var bean_properties = BeanUtils.getUppercasedBeanProperties(UploadGuardBean.class);
            var bean = new UploadGuardBean();

            BeanUtils.setUppercasedBeanProperty("locked", uploaded, null, bean_properties, bean);
            assertEquals("untouched", bean.getLocked());

            BeanUtils.setUppercasedBeanProperty("open", uploaded, null, bean_properties, bean);
            assertEquals("uploaded content", bean.getOpen());

            // the parameter name only has to match the property
            // case-insensitively, the guard has to hold up for those too
            BeanUtils.setUppercasedBeanProperty("Locked", uploaded, null, bean_properties, bean);
            assertEquals("untouched", bean.getLocked());
        }
    }

    @Test
    void testSetUppercaseBeanProperty()
    throws BeanUtilsException, ParseException, SerializationUtilsErrorException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyString(), "one");

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyInt", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyInt(), 438);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyChar", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyChar(), 'E');

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBoolean", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertTrue(bean.isPropertyBoolean());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyByte(), 27);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDouble(), 80756.6287d);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyFloat(), 435.557f);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLong(), 122875);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyShort(), 3285);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"983743.343", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyBigDecimal(), new BigDecimal("983743.343"));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyIntegerObject(), 438);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObject", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyCharacterObject(), 'E');

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObject", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyBooleanObject(), Boolean.TRUE);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyByteObject(), (byte) 27);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDoubleObject(), 80756.6287d);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyFloatObject(), 435.557f);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLongObject(), 122875);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyShortObject(), (short) 3285);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuffer", new String[]{"one1", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyStringBuffer().toString(), "one1");

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilder", new String[]{"one2", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyStringBuilder().toString(), "one2");

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
        assertArrayEquals(bean.getPropertyStringArray(), new String[]{"one", "two"});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntArray", new String[]{"438", "98455", "711"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyIntArray(), new int[]{438, 98455, 711});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharArray", new String[]{"E", "a", "x"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyCharArray(), new char[]{'E', 'a', 'x'});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanArray", new String[]{"true", "0", "t", "1"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBooleanArray(), new boolean[]{true, false, true, true});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyByteArray(), new byte[]{27, 78});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80756.6287", "3214.75", "85796.6237"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDoubleArray(), new double[]{80756.6287d, 3214.75d, 85796.6237d});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435.557", "589.5"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyFloatArray(), new float[]{435.557f, 589.5f});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"122875", "8526780", "3826589"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLongArray(), new long[]{122875, 8526780, 3826589});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"3285", "58"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyShortArray(), new short[]{3285, 58});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"438", "7865", "475"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyIntegerObjectArray(), new Integer[]{438, 7865, 475});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObjectArray", new String[]{"E", "z"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyCharacterObjectArray(), new Character[]{'E', 'z'});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObjectArray", new String[]{"fslse", "1", "true"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBooleanObjectArray(), new Boolean[]{false, true, true});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyByteObjectArray(), new Byte[]{(byte) 27, (byte) 78});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80756.6287", "5876.14", "3268.57"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDoubleObjectArray(), new Double[]{80756.6287d, 5876.14d, 3268.57d});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435.557", "7865.66"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyFloatObjectArray(), new Float[]{435.557f, 7865.66f});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"122875", "5687621", "66578"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLongObjectArray(), new Long[]{122875L, 5687621L, 66578L});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"3285", "6588"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyShortObjectArray(), new Short[]{(short) 3285, (short) 6588});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"32859837434343983.83749837498373434", "65884343.343"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBigDecimalArray(), new BigDecimal[]{new BigDecimal("32859837434343983.83749837498373434"), new BigDecimal("65884343343E-3")});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBufferArray", new String[]{"one1", "two2"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(ArrayUtils.createStringArray(bean.getPropertyStringBufferArray()), new String[]{"one1", "two2"});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilderArray", new String[]{"three3", "four4"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(ArrayUtils.createStringArray(bean.getPropertyStringBuilderArray()), new String[]{"three3", "four4"});

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
        assertEquals(bean.getPropertyInt(), 438);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyByte(), 27);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDouble(), 80756.6287d);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyFloat(), 435.557f);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLong(), 122875);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyShort(), 3285);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"4353344987349830948394893,55709384093", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyBigDecimal(), new BigDecimal("435334498734983094839489355709384093E-11"));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"$438", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyIntegerObject(), 438);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyByteObject(), (byte) 27);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDoubleObject(), 80756.6287d);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyFloatObject(), 435.557f);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLongObject(), 122875);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyShortObject(), (short) 3285);

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
        assertArrayEquals(bean.getPropertyIntArray(), new int[]{438, 98455, 711});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyByteArray(), new byte[]{27, 78});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80,756.6287", "3,214.75", "85,796.6237"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDoubleArray(), new double[]{80756.6287d, 3214.75d, 85796.6237d});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435,557", "589,5"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyFloatArray(), new float[]{435.557f, 589.5f});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"$122,875.00", "$8,526,780.00", "$3,826,589.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLongArray(), new long[]{122875, 8526780, 3826589});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"¤3285", "¤58"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyShortArray(), new short[]{3285, 58});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"$438", "$7865", "$475"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyIntegerObjectArray(), new Integer[]{438, 7865, 475});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyByteObjectArray(), new Byte[]{(byte) 27, (byte) 78});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80,756.6287", "5,876.14", "3,268.57"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDoubleObjectArray(), new Double[]{80756.6287d, 5876.14d, 3268.57d});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435,557", "7865,66"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyFloatObjectArray(), new Float[]{435.557f, 7865.66f});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"$122,875.00", "$5,687,621.00", "$66,578.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLongObjectArray(), new Long[]{122875L, 5687621L, 66578L});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"¤3285", "¤6588"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyShortObjectArray(), new Short[]{(short) 3285, (short) 6588});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"97687687998978673545669789,0000000000001", "34353"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyBigDecimalArray(), new BigDecimal[]{new BigDecimal("976876879989786735456697890000000000001E-13"), new BigDecimal("3.4353E4")});

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
            assertEquals(property_names.size(), 20);
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
            assertEquals(property_names.size(), 21);
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
            assertEquals(property_names.size(), 21);
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
            assertEquals(property_names.size(), 20);
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
            assertEquals(property_names.size(), 21);
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
            assertEquals(property_names.size(), 21);
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
            assertEquals(property_names.size(), 9);
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
            assertEquals(property_names.size(), 10);
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
            assertEquals(property_names.size(), 10);
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
            assertEquals(property_names.size(), 9);
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
            assertEquals(property_names.size(), 10);
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
            assertEquals(property_names.size(), 10);
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
            assertEquals(property_names.size(), 12);
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
            assertEquals(property_names.size(), 13);
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
            assertEquals(property_names.size(), 13);
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
            assertEquals(property_names.size(), 12);
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
            assertEquals(property_names.size(), 13);
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
            assertEquals(property_names.size(), 13);
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
            assertEquals(property_names.size(), 5);
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
            assertEquals(property_names.size(), 6);
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
            assertEquals(property_names.size(), 6);
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
            assertEquals(property_names.size(), 5);
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
            assertEquals(property_names.size(), 6);
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
            assertEquals(property_names.size(), 6);
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
            assertEquals(count, 20);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(count, 21);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(count, 21);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefix() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 20);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefixGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 21);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountPropertiesPrefixSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 21);
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
            assertEquals(count, 12);
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
            assertEquals(count, 13);
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
            assertEquals(count, 13);
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
            assertEquals(count, 12);
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
            assertEquals(count, 13);
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
            assertEquals(count, 13);
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
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyString"), String.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyStringBuffer"), StringBuffer.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyDate"), java.util.Date.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyCalendar"), java.util.Calendar.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertySqlDate"), java.sql.Date.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyTime"), java.sql.Time.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyInstant"), Instant.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalDateTime"), LocalDateTime.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalDate"), LocalDate.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyLocalTime"), LocalTime.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyChar"), char.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyBoolean"), boolean.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyByte"), byte.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyDouble"), double.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyFloat"), float.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyInt"), int.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyLong"), long.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyShort"), short.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "unknown"), String.class);
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), BeanImpl.class);
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
            assertEquals(property_types.size(), 20);
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
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_types.size(), 21);
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
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_types.size(), 21);
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
            assertSame(property_types.get("propertyWriteOnly"), long.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 20);
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
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 21);
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
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPropertyTypesPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 21);
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
            assertSame(property_types.get("PREFIX:propertyWriteOnly"), long.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
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
            assertEquals(property_types.size(), 9);
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyLocalDate"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 10);
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
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 10);
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
            assertSame(property_types.get("propertyWriteOnly"), long.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 9);
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyLocalDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 10);
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
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 10);
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
            assertSame(property_types.get("PREFIX:propertyWriteOnly"), long.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDate"), LocalDate.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 13);
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
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 14);
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
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 14);
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
            assertSame(property_types.get("propertyWriteOnly"), long.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 13);
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
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 14);
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
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 14);
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
            assertSame(property_types.get("PREFIX:propertyWriteOnly"), long.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyLocalDateTime"), LocalDateTime.class);
            assertSame(property_types.get("PREFIX:propertyLocalTime"), LocalTime.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 6);
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 6);
            assertTrue(property_types.containsKey("propertyWriteOnly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyInstant"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyWriteOnly"), long.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyInstant"), Instant.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
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
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 6);
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(property_types.size(), 6);
            assertTrue(property_types.containsKey("PREFIX:propertyWriteOnly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyWriteOnly"), long.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyInstant"), Instant.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
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
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyString"), "thisisastring");
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyDate"), Convert.toDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyCalendar"), cal);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTime"), Convert.toSqlTime(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyInstant"), Convert.toInstant(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyChar"), 'g');
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyBoolean"), Boolean.FALSE);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyByte"), (byte) 53);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyDouble"), 84578.42d);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyFloat"), 35523.967f);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyInt"), 978);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLong"), 87346L);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyShort"), (short) 31);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
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
            assertEquals(property_values.size(), 20);
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
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, null);
            assertEquals(property_values.size(), 21);
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
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, null);
            assertEquals(property_values.size(), 20);
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
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 20);
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
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 21);
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
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetPropertyValuesPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 20);
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
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
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
            assertEquals(property_values.size(), 9);
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
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 10);
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
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 9);
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
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 9);
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
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 10);
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
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 9);
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
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertySqlDate"), Convert.toSqlDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDate"), Convert.toLocalDate(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 13);
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
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 14);
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
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 13);
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
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 13);
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
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 14);
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
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 13);
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
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), Convert.toSqlTimestamp(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalDateTime"), Convert.toLocalDateTime(cal));
            assertEquals(property_values.get("PREFIX:propertyLocalTime"), Convert.toLocalTime(cal));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 6);
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyInstant"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 6);
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyInstant"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = RifeConfig.tools().getCalendarInstance(2002, Calendar.DECEMBER, 26, 22, 52, 31, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), Convert.toDate(cal));
            assertEquals(property_values.get("PREFIX:propertyTime"), Convert.toSqlTime(cal));
            assertEquals(property_values.get("PREFIX:propertyInstant"), Convert.toInstant(cal));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
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
    class TestOptionalProperties {
        // the idiomatic shape, Optional getters with plain-typed setters, the
        // exact bean shape the standard introspector refuses to pair
        public static class OptionalIdiomaticBean {
            private String name_;
            private Integer count_;
            private String plain_;

            public Optional<String> getName() { return Optional.ofNullable(name_); }
            public void setName(String name) { name_ = name; }

            public Optional<Integer> getCount() { return Optional.ofNullable(count_); }
            public void setCount(Integer count) { count_ = count; }

            public String getPlain() { return plain_; }
            public void setPlain(String plain) { plain_ = plain; }
        }

        // the symmetric shape, the introspector pairs it and the setter
        // values are wrapped
        public static class OptionalSymmetricBean {
            private Optional<String> value_ = Optional.empty();

            public Optional<String> getValue() { return value_; }
            public void setValue(Optional<String> value) { value_ = value; }
        }

        public static class OptionalReadOnlyBean {
            public Optional<String> getOnly() { return Optional.of("only"); }
            public Optional<String> getNullRef() { return null; }
        }

        public static class WriteOnlyBean {
            public void setOnly(String only) {}
        }

        @Test
        void testIntrospectorDropsMismatchedSetterCanary()
        throws Exception {
            // the supplemental pairing exists because the standard introspector
            // silently drops a setter whose type doesn't match the getter; when
            // this canary fails, a JDK changed that behavior and the pairing
            // should be revisited
            for (var descriptor : java.beans.Introspector.getBeanInfo(OptionalIdiomaticBean.class, Object.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("name")) {
                    assertNotNull(descriptor.getReadMethod());
                    assertNull(descriptor.getWriteMethod(), "the introspector is expected to drop the mismatched setter");
                    return;
                }
            }
            fail("the name property wasn't found");
        }

        @Test
        void testIdiomaticEnumeration()
        throws BeanUtilsException {
            // the recovered setter makes the property a full read-write member
            var default_names = BeanUtils.getPropertyNames(OptionalIdiomaticBean.class, null, null, null);
            assertEquals(Set.of("name", "count", "plain"), default_names);
            assertEquals(Set.of("name", "count", "plain"), BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, OptionalIdiomaticBean.class, null, null, null));
            assertEquals(Set.of("name", "count", "plain"), BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, OptionalIdiomaticBean.class, null, null, null));
            assertEquals(3, BeanUtils.countProperties(OptionalIdiomaticBean.class, null, null, null));
        }

        @Test
        void testReadOnlyEnumeration()
        throws BeanUtilsException {
            // without a setter to recover, an Optional getter stays read-only
            assertEquals(Collections.emptySet(), BeanUtils.getPropertyNames(OptionalReadOnlyBean.class, null, null, null));
            assertEquals(Set.of("only", "nullRef"), BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, OptionalReadOnlyBean.class, null, null, null));
        }

        @Test
        void testEffectiveTypes()
        throws BeanUtilsException {
            assertSame(String.class, BeanUtils.getPropertyType(OptionalIdiomaticBean.class, "name"));
            assertSame(Integer.class, BeanUtils.getPropertyType(OptionalIdiomaticBean.class, "count"));
            assertSame(String.class, BeanUtils.getPropertyType(OptionalIdiomaticBean.class, "plain"));
            assertSame(String.class, BeanUtils.getPropertyType(OptionalSymmetricBean.class, "value"));
            // a write-only property resolves through its setter instead of
            // raising a NullPointerException
            assertSame(String.class, BeanUtils.getPropertyType(WriteOnlyBean.class, "only"));

            var types = BeanUtils.getPropertyTypes(OptionalIdiomaticBean.class, null, null, null);
            assertSame(String.class, types.get("name"));
            assertSame(Integer.class, types.get("count"));
            assertSame(String.class, types.get("plain"));
        }

        @Test
        void testValuesAreUnwrapped()
        throws BeanUtilsException {
            var bean = new OptionalIdiomaticBean();
            bean.setName("hello");

            var values = BeanUtils.getPropertyValues(bean, null, null, null);
            assertEquals("hello", values.get("name"));
            assertFalse(values.get("name") instanceof Optional);
            assertNull(values.get("count"), "an empty Optional reads as null");

            assertEquals("hello", BeanUtils.getPropertyValue(bean, "name"));
            assertNull(BeanUtils.getPropertyValue(bean, "count"));
        }

        @Test
        void testNullOptionalReferenceIsLenient()
        throws BeanUtilsException {
            // a getter that returns a null Optional reference reads as null
            assertNull(BeanUtils.getPropertyValue(new OptionalReadOnlyBean(), "nullRef"));
        }

        @Test
        void testFormatPropertyValueNull() {
            // Optional values arrive at formatting already unwrapped by the
            // property value methods, and a null never consults the format
            assertNull(BeanUtils.formatPropertyValue(null, null));
            assertNull(BeanUtils.formatPropertyValue(null, new ConstrainedProperty("property").format(RifeConfig.tools().getConcisePreciseDateFormat())));
        }

        @Test
        void testRoundTripIdiomatic()
        throws BeanUtilsException {
            var properties = BeanUtils.getUppercasedBeanProperties(OptionalIdiomaticBean.class);
            var bean = new OptionalIdiomaticBean();

            // string values convert to the effective type and reach the
            // recovered plain-typed setters
            BeanUtils.setUppercasedBeanProperty("NAME", new String[]{"hello"}, null, properties, bean, null);
            BeanUtils.setUppercasedBeanProperty("COUNT", new String[]{"438"}, null, properties, bean, null);
            assertEquals(Optional.of("hello"), bean.getName());
            assertEquals(Optional.of(438), bean.getCount());
        }

        @Test
        void testRoundTripSymmetric()
        throws BeanUtilsException {
            var properties = BeanUtils.getUppercasedBeanProperties(OptionalSymmetricBean.class);
            var bean = new OptionalSymmetricBean();

            // the converted value is wrapped for the Optional-typed setter
            BeanUtils.setUppercasedBeanProperty("VALUE", new String[]{"world"}, null, properties, bean, null);
            assertEquals(Optional.of("world"), bean.getValue());
        }

        @Test
        void testEmptyBeanPath()
        throws BeanUtilsException {
            var properties = BeanUtils.getUppercasedBeanProperties(OptionalIdiomaticBean.class);
            var empty_bean = new OptionalIdiomaticBean();
            empty_bean.setName("default");

            // an empty submission takes the value from the empty bean, through
            // the Optional getter and into the recovered setter
            var bean = new OptionalIdiomaticBean();
            bean.setName("previous");
            BeanUtils.setUppercasedBeanProperty("NAME", new String[0], null, properties, bean, empty_bean);
            assertEquals(Optional.of("default"), bean.getName());
        }

        @Test
        void testSetPropertyValueOptional()
        throws BeanUtilsException {
            var idiomatic = new OptionalIdiomaticBean();
            BeanUtils.setPropertyValue(idiomatic, "name", "direct");
            assertEquals(Optional.of("direct"), idiomatic.getName());

            var symmetric = new OptionalSymmetricBean();
            BeanUtils.setPropertyValue(symmetric, "value", "wrapped");
            assertEquals(Optional.of("wrapped"), symmetric.getValue());
        }

        @Test
        void testPrefixAndFilters()
        throws BeanUtilsException {
            // prefixes and inclusion filters apply to recovered properties
            // like to any other
            assertEquals(Set.of("PRE:name"),
                BeanUtils.getPropertyNames(OptionalIdiomaticBean.class, new String[]{"PRE:name"}, null, "PRE:"));
            assertEquals(Set.of("name", "plain"),
                BeanUtils.getPropertyNames(OptionalIdiomaticBean.class, null, new String[]{"count"}, null));

            var properties = BeanUtils.getUppercasedBeanProperties(OptionalIdiomaticBean.class);
            var bean = new OptionalIdiomaticBean();
            BeanUtils.setUppercasedBeanProperty("PRE:NAME", new String[]{"prefixed"}, "PRE:", properties, bean, null);
            assertEquals(Optional.of("prefixed"), bean.getName());
        }

        public static class OptionalBaseBean {
            protected String label_;

            public Optional<String> getLabel() { return Optional.ofNullable(label_); }
        }

        public static class OptionalDerivedBean extends OptionalBaseBean {
            public void setLabel(String label) { label_ = label; }
        }

        @Test
        void testInheritedPairing()
        throws BeanUtilsException {
            // the Optional getter lives in the superclass and the plain-typed
            // setter in the subclass, the recovery pairs across the hierarchy
            assertEquals(Set.of("label"), BeanUtils.getPropertyNames(OptionalDerivedBean.class, null, null, null));

            var properties = BeanUtils.getUppercasedBeanProperties(OptionalDerivedBean.class);
            var bean = new OptionalDerivedBean();
            BeanUtils.setUppercasedBeanProperty("LABEL", new String[]{"inherited"}, null, properties, bean, null);
            assertEquals(Optional.of("inherited"), bean.getLabel());
        }

        @SuppressWarnings("rawtypes")
        public static class RawOptionalBean {
            private String value_;

            public Optional getRaw() { return Optional.ofNullable(value_); }
            public void setRaw(String value) { value_ = value; }
        }

        @Test
        void testRawOptionalBehavesAsOptionalObject()
        throws BeanUtilsException {
            // a raw Optional getter has no resolvable value type and behaves
            // like Optional<Object>, following the introspector's assignability
            // rule the most specific same-named setter is paired
            assertEquals(Set.of("raw"), BeanUtils.getPropertyNames(RawOptionalBean.class, null, null, null));
            assertSame(Object.class, BeanUtils.getPropertyType(RawOptionalBean.class, "raw"));

            var bean = new RawOptionalBean();
            BeanUtils.setPropertyValue(bean, "raw", "rawvalue");
            assertEquals("rawvalue", BeanUtils.getPropertyValue(bean, "raw"));
        }

        public static class OverloadedOptionalBean {
            private String text_;
            private StringBuilder builder_;

            public Optional<String> getText() { return Optional.ofNullable(text_); }
            public void setText(String text) { text_ = text; }
            public void setText(StringBuilder text) { builder_ = text; }
        }

        @Test
        void testOverloadedSettersPickExactMatch()
        throws BeanUtilsException {
            var properties = BeanUtils.getUppercasedBeanProperties(OverloadedOptionalBean.class);
            var bean = new OverloadedOptionalBean();
            BeanUtils.setUppercasedBeanProperty("TEXT", new String[]{"picked"}, null, properties, bean, null);
            assertEquals(Optional.of("picked"), bean.getText());
            assertNull(bean.builder_, "the overload that matches the Optional's value type is used");
        }

        public static class OptionalArrayBean {
            private String[] tags_;

            public Optional<String[]> getTags() { return Optional.ofNullable(tags_); }
            public void setTags(String[] tags) { tags_ = tags; }
        }

        @Test
        void testOptionalArrayProperty()
        throws BeanUtilsException {
            assertSame(String[].class, BeanUtils.getPropertyType(OptionalArrayBean.class, "tags"));

            var properties = BeanUtils.getUppercasedBeanProperties(OptionalArrayBean.class);
            var bean = new OptionalArrayBean();
            BeanUtils.setUppercasedBeanProperty("TAGS", new String[]{"one", "two"}, null, properties, bean, null);
            assertArrayEquals(new String[]{"one", "two"}, bean.getTags().orElseThrow());

            // reading unwraps to the array itself
            assertArrayEquals(new String[]{"one", "two"}, (String[]) BeanUtils.getPropertyValue(bean, "tags"));
        }

        public static class OptionalDateBean {
            private LocalDate date_;

            public Optional<LocalDate> getDate() { return Optional.ofNullable(date_); }
            public void setDate(LocalDate date) { date_ = date; }
        }

        @Test
        void testDateConversion()
        throws BeanUtilsException {
            // the date conversion machinery targets the effective type
            assertSame(LocalDate.class, BeanUtils.getPropertyType(OptionalDateBean.class, "date"));

            var properties = BeanUtils.getUppercasedBeanProperties(OptionalDateBean.class);
            var bean = new OptionalDateBean();
            BeanUtils.setUppercasedBeanProperty("DATE", new String[]{"2023-01-23 00:00"}, null, properties, bean, null);
            assertEquals(Optional.of(LocalDate.of(2023, Month.JANUARY, 23)), bean.getDate());
        }

        public static class ValidatedOptionalBean extends rife.validation.MetaData {
            private Integer count_;

            public Optional<Integer> getCount() { return Optional.ofNullable(count_); }
            public void setCount(Integer count) { count_ = count; }
        }

        @Test
        void testValidatedConversionError()
        throws BeanUtilsException {
            // a value that can't convert to the effective type registers a
            // validation error instead of corrupting the property
            var properties = BeanUtils.getUppercasedBeanProperties(ValidatedOptionalBean.class);
            var bean = new ValidatedOptionalBean();
            BeanUtils.setUppercasedBeanProperty("count", new String[]{"notanumber"}, null, properties, bean, null);
            assertEquals(Optional.empty(), bean.getCount(), "the property stays untouched");
            assertEquals(1, bean.getValidationErrors().size());
            assertEquals("count", bean.getValidationErrors().iterator().next().getSubject());
        }

        public static class PrimitiveOptionalBean {
            public OptionalInt getScore() { return OptionalInt.of(42); }
            public void setScore(OptionalInt score) {}
        }

        @Test
        void testEmptyParameterWithPrimitiveSetter()
        throws BeanUtilsException {
            // a blank submission can't hand an absent value to a primitive
            // setter, the property is left untouched instead of failing
            var properties = BeanUtils.getUppercasedBeanProperties(PrimitiveParamSetterBean.class);
            var bean = new PrimitiveParamSetterBean();
            BeanUtils.setUppercasedBeanProperty("COUNT", new String[]{"42"}, null, properties, bean, null);
            assertEquals(Optional.of(42), bean.getCount());

            BeanUtils.setUppercasedBeanProperty("COUNT", new String[0], null, properties, bean, new PrimitiveParamSetterBean());
            assertEquals(Optional.of(42), bean.getCount(), "the existing value survives a blank submission");
        }

        @Test
        void testDescriptorsAreSelfDescribing()
        throws BeanUtilsException {
            // the descriptors that getBeanInfo hands out describe Optional
            // properties directly, plain descriptor-based code that uses
            // getPropertyType() and getWriteMethod() is correct without any
            // Optional-specific handling
            for (var descriptor : BeanUtils.getBeanInfo(OptionalIdiomaticBean.class).getPropertyDescriptors()) {
                switch (descriptor.getName()) {
                    case "name" -> {
                        assertSame(String.class, descriptor.getPropertyType());
                        assertNotNull(descriptor.getWriteMethod(), "the recovered setter is the write method");
                        assertSame(String.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                    }
                    case "count" -> {
                        assertSame(Integer.class, descriptor.getPropertyType());
                        assertNotNull(descriptor.getWriteMethod());
                    }
                    case "plain" -> assertSame(String.class, descriptor.getPropertyType());
                }
            }

            // the symmetric shape reports the value type too, while keeping
            // its own Optional-typed setter as the write method
            for (var descriptor : BeanUtils.getBeanInfo(OptionalSymmetricBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("value")) {
                    assertSame(String.class, descriptor.getPropertyType());
                    assertNotNull(descriptor.getWriteMethod());
                    assertSame(Optional.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                }
            }
        }

        public static class DualSetterOptionalBean {
            private Optional<String> value_ = Optional.empty();

            public Optional<String> getValue() { return value_; }
            public void setValue(Optional<String> value) { value_ = value; }
            public void setValue(String value) { value_ = Optional.ofNullable(value); }
        }

        @Test
        void testPlainSetterPreferredOverOptionalSetter()
        throws Exception {
            // when the bean offers both setter shapes, the plain-typed one
            // becomes the write method, so even naive reflection code can
            // write the effective type directly, without any wrapping
            for (var descriptor : BeanUtils.getBeanInfo(DualSetterOptionalBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("value")) {
                    assertSame(String.class, descriptor.getPropertyType());
                    assertSame(String.class, descriptor.getWriteMethod().getParameterTypes()[0]);

                    var bean = new DualSetterOptionalBean();
                    descriptor.getWriteMethod().invoke(bean, "naive");
                    assertEquals(Optional.of("naive"), bean.getValue());
                    return;
                }
            }
            fail("the value property wasn't found");
        }

        public static class MixedIsGetterBean {
            private boolean enabled_;
            private String note_;

            public boolean isEnabled() { return enabled_; }
            public void setEnabled(boolean enabled) { enabled_ = enabled; }

            public Optional<String> getNote() { return Optional.ofNullable(note_); }
            public void setNote(String note) { note_ = note; }
        }

        @Test
        void testIsGetterBooleansAlongsideOptionals()
        throws BeanUtilsException {
            // a primitive boolean is-getter is a regular property next to
            // Optional ones, the enhancement leaves its descriptor untouched
            assertEquals(Set.of("enabled", "note"), BeanUtils.getPropertyNames(MixedIsGetterBean.class, null, null, null));
            assertSame(boolean.class, BeanUtils.getPropertyType(MixedIsGetterBean.class, "enabled"));
            for (var descriptor : BeanUtils.getBeanInfo(MixedIsGetterBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("enabled")) {
                    assertEquals("isEnabled", descriptor.getReadMethod().getName());
                }
            }

            var properties = BeanUtils.getUppercasedBeanProperties(MixedIsGetterBean.class);
            var bean = new MixedIsGetterBean();
            BeanUtils.setUppercasedBeanProperty("ENABLED", new String[]{"true"}, null, properties, bean, null);
            BeanUtils.setUppercasedBeanProperty("NOTE", new String[]{"hi"}, null, properties, bean, null);
            assertTrue(bean.isEnabled());
            assertEquals(Optional.of("hi"), bean.getNote());
        }

        public static class IsPrefixOptionalBean {
            private Boolean active_;

            public Optional<Boolean> isActive() { return Optional.ofNullable(active_); }
            public void setActive(Boolean active) { active_ = active; }
        }

        @Test
        void testIsPrefixIsNotAnOptionalGetter()
        throws BeanUtilsException {
            // the is prefix is only a getter for primitive booleans, an
            // Optional-returning is-method is not a getter at all, so the
            // property is write-only
            assertEquals(Collections.emptySet(), BeanUtils.getPropertyNames(IsPrefixOptionalBean.class, null, null, null));
            assertEquals(Collections.emptySet(), BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, IsPrefixOptionalBean.class, null, null, null));
            assertEquals(Set.of("active"), BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, IsPrefixOptionalBean.class, null, null, null));
            assertSame(Boolean.class, BeanUtils.getPropertyType(IsPrefixOptionalBean.class, "active"));
        }

        public static class OptionalBooleanBean {
            private Boolean active_;

            public Optional<Boolean> getActive() { return Optional.ofNullable(active_); }
            public void setActive(Boolean active) { active_ = active; }
        }

        @Test
        void testOptionalBooleanRoundTrip()
        throws BeanUtilsException {
            assertSame(Boolean.class, BeanUtils.getPropertyType(OptionalBooleanBean.class, "active"));

            var properties = BeanUtils.getUppercasedBeanProperties(OptionalBooleanBean.class);
            var bean = new OptionalBooleanBean();
            BeanUtils.setUppercasedBeanProperty("ACTIVE", new String[]{"true"}, null, properties, bean, null);
            assertEquals(Optional.of(Boolean.TRUE), bean.getActive());
        }

        public static class FluentSetterOptionalBean {
            public Optional<String> getName() { return Optional.empty(); }
            public FluentSetterOptionalBean setName(String name) { return this; }
        }

        @Test
        void testFluentSetterIsNotRecovered()
        throws BeanUtilsException {
            // the introspector only pairs void setters, the recovery follows
            // the same rule so Optional properties aren't treated differently
            assertEquals(Collections.emptySet(), BeanUtils.getPropertyNames(FluentSetterOptionalBean.class, null, null, null));
            for (var descriptor : BeanUtils.getBeanInfo(FluentSetterOptionalBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("name")) {
                    assertNull(descriptor.getWriteMethod());
                }
            }
        }

        public static class StaticSetterOptionalBean {
            public Optional<String> getCode() { return Optional.empty(); }
            public static void setCode(String code) {}
        }

        @Test
        void testStaticSetterIsNotRecovered()
        throws BeanUtilsException {
            // the introspector ignores static methods, the recovery does too
            assertEquals(Collections.emptySet(), BeanUtils.getPropertyNames(StaticSetterOptionalBean.class, null, null, null));
            assertEquals(Set.of("code"), BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, StaticSetterOptionalBean.class, null, null, null));
        }

        public static class SubtypeSetterBean {
            private CharSequence text_;

            public Optional<CharSequence> getText() { return Optional.ofNullable(text_); }
            public void setText(String text) { text_ = text; }
        }

        @Test
        void testSubtypeSetterIsRecovered()
        throws BeanUtilsException {
            // the introspector pairs a setter whose parameter is a subtype of
            // the getter type, the recovery applies the same rule
            assertEquals(Set.of("text"), BeanUtils.getPropertyNames(SubtypeSetterBean.class, null, null, null));
            assertSame(CharSequence.class, BeanUtils.getPropertyType(SubtypeSetterBean.class, "text"));

            var bean = new SubtypeSetterBean();
            BeanUtils.setPropertyValue(bean, "text", "subtyped");
            assertEquals(Optional.of("subtyped"), bean.getText());
        }

        public static class PrimitiveParamSetterBean {
            private int count_ = -1;

            public Optional<Integer> getCount() { return count_ < 0 ? Optional.empty() : Optional.of(count_); }
            public void setCount(int count) { count_ = count; }
        }

        @Test
        void testPrimitiveSetterCounterpartIsRecovered()
        throws BeanUtilsException {
            // a primitive parameter pairs with its boxed value type,
            // reflection unboxes the converted value at invocation time
            assertEquals(Set.of("count"), BeanUtils.getPropertyNames(PrimitiveParamSetterBean.class, null, null, null));
            assertSame(Integer.class, BeanUtils.getPropertyType(PrimitiveParamSetterBean.class, "count"));

            var properties = BeanUtils.getUppercasedBeanProperties(PrimitiveParamSetterBean.class);
            var bean = new PrimitiveParamSetterBean();
            BeanUtils.setUppercasedBeanProperty("COUNT", new String[]{"42"}, null, properties, bean, null);
            assertEquals(Optional.of(42), bean.getCount());
        }

        public static class IndexedPropertyBean {
            private String[] items_ = {"a", "b"};

            public String[] getItems() { return items_; }
            public void setItems(String[] items) { items_ = items; }
            public String getItems(int index) { return items_[index]; }
            public void setItems(int index, String item) { items_[index] = item; }
        }

        @Test
        void testIndexedPropertiesUntouched()
        throws BeanUtilsException {
            // indexed properties keep their own descriptor type and behavior
            for (var descriptor : BeanUtils.getBeanInfo(IndexedPropertyBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("items")) {
                    assertInstanceOf(java.beans.IndexedPropertyDescriptor.class, descriptor);
                }
            }
            assertSame(String[].class, BeanUtils.getPropertyType(IndexedPropertyBean.class, "items"));

            var properties = BeanUtils.getUppercasedBeanProperties(IndexedPropertyBean.class);
            var bean = new IndexedPropertyBean();
            BeanUtils.setUppercasedBeanProperty("ITEMS", new String[]{"x", "y"}, null, properties, bean, null);
            assertArrayEquals(new String[]{"x", "y"}, bean.getItems());
        }

        public static class SetterOnlyOptionalBean {
            private String token_;

            public void setToken(Optional<String> token) { token_ = token.orElse(null); }
            public String token() { return token_; }
        }

        @Test
        void testSetterOnlyOptionalProperty()
        throws BeanUtilsException {
            // a setter-only Optional property describes itself with the value
            // type too, and receives its values wrapped
            assertEquals(Set.of("token"), BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, SetterOnlyOptionalBean.class, null, null, null));
            assertSame(String.class, BeanUtils.getPropertyType(SetterOnlyOptionalBean.class, "token"));
            for (var descriptor : BeanUtils.getBeanInfo(SetterOnlyOptionalBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("token")) {
                    assertSame(String.class, descriptor.getPropertyType());
                }
            }

            var properties = BeanUtils.getUppercasedBeanProperties(SetterOnlyOptionalBean.class);
            var bean = new SetterOnlyOptionalBean();
            BeanUtils.setUppercasedBeanProperty("TOKEN", new String[]{"secret"}, null, properties, bean, null);
            assertEquals("secret", bean.token());

            var other = new SetterOnlyOptionalBean();
            BeanUtils.setPropertyValue(other, "token", "direct");
            assertEquals("direct", other.token());

            // a blank update passes an empty Optional through the setter
            // instead of failing on the absent getter
            BeanUtils.setUppercasedBeanProperty("TOKEN", new String[0], null, properties, bean, new SetterOnlyOptionalBean());
            assertNull(bean.token());
        }

        public static class GenericOptionalBase<T> {
            protected T value_;

            public Optional<T> getValue() { return Optional.ofNullable(value_); }
            public void setValue(T value) { value_ = value; }
        }

        public static class GenericOptionalSub extends GenericOptionalBase<String> {}

        public static class GenericIntegerSub extends GenericOptionalBase<Integer> {}

        public static class GenericMiddle<S> extends GenericOptionalBase<S> {}

        public static class GenericChainedSub extends GenericMiddle<Long> {}

        @SuppressWarnings("rawtypes")
        public static class GenericRawSub extends GenericOptionalBase {}

        @Test
        void testInheritedGenericOptionalResolves()
        throws BeanUtilsException {
            // type variables resolve against the bean class's generic
            // hierarchy, so Optional<T> from a supertype behaves like the
            // concrete type the bean provides
            assertEquals(Set.of("value"), BeanUtils.getPropertyNames(GenericOptionalSub.class, null, null, null));
            assertSame(String.class, BeanUtils.getPropertyType(GenericOptionalSub.class, "value"));
            assertSame(Integer.class, BeanUtils.getPropertyType(GenericIntegerSub.class, "value"));
            assertSame(Long.class, BeanUtils.getPropertyType(GenericChainedSub.class, "value"));

            // request population converts to the resolved type
            var text_bean = new GenericOptionalSub();
            BeanUtils.setUppercasedBeanProperty("VALUE", new String[]{"resolved"}, null,
                BeanUtils.getUppercasedBeanProperties(GenericOptionalSub.class), text_bean, null);
            assertEquals(Optional.of("resolved"), text_bean.getValue());

            var int_bean = new GenericIntegerSub();
            BeanUtils.setUppercasedBeanProperty("VALUE", new String[]{"42"}, null,
                BeanUtils.getUppercasedBeanProperties(GenericIntegerSub.class), int_bean, null);
            assertEquals(Optional.of(42), int_bean.getValue());
        }

        public static class GenericArrayBase<T> {
            protected T[] values_;

            public Optional<T[]> getValues() { return Optional.ofNullable(values_); }
            public void setValues(T[] values) { values_ = values; }
        }

        public static class GenericArrayStrings extends GenericArrayBase<String> {}

        @Test
        void testInheritedGenericArrayResolves()
        throws BeanUtilsException {
            // a generic array component resolves through the hierarchy and
            // reconstitutes the concrete array type
            assertEquals(Set.of("values"), BeanUtils.getPropertyNames(GenericArrayStrings.class, null, null, null));
            assertSame(String[].class, BeanUtils.getPropertyType(GenericArrayStrings.class, "values"));

            // request population treats it as an array property
            var bean = new GenericArrayStrings();
            BeanUtils.setUppercasedBeanProperty("VALUES", new String[]{"a", "b"}, null,
                BeanUtils.getUppercasedBeanProperties(GenericArrayStrings.class), bean, null);
            assertArrayEquals(new String[]{"a", "b"}, bean.getValues().orElseThrow());
        }

        @Test
        void testRawGenericSubclassDegradesToObject()
        throws BeanUtilsException {
            // a raw extension provides no type argument to resolve, the
            // property degrades to Object but stays usable
            assertSame(Object.class, BeanUtils.getPropertyType(GenericRawSub.class, "value"));

            var bean = new GenericRawSub();
            BeanUtils.setPropertyValue(bean, "value", "raw");
            assertEquals("raw", BeanUtils.getPropertyValue(bean, "value"));
        }

        public static class UndeclaredOptionalBean {
            private Object data_ = Optional.of("x");

            public Object getData() { return data_; }
            public void setData(Object data) { data_ = data; }
        }

        @Test
        void testOnlyDeclaredOptionalGettersUnwrap()
        throws BeanUtilsException {
            // an Object property that happens to hold an Optional isn't
            // unwrapped, only getters declared to return Optional are
            var value = BeanUtils.getPropertyValue(new UndeclaredOptionalBean(), "data");
            assertInstanceOf(Optional.class, value);
            assertEquals(Optional.of("x"), value);
            assertEquals(Optional.of("x"), BeanUtils.getPropertyValues(new UndeclaredOptionalBean(), null, null, null).get("data"));
        }

        @Test
        void testDescriptorMetadataPreserved()
        throws BeanUtilsException {
            // the enhancement copies the metadata of the original descriptor,
            // so the flags and attributes of a custom BeanInfo survive
            for (var descriptor : BeanUtils.getBeanInfo(OptionalMetadataBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("label")) {
                    assertSame(String.class, descriptor.getPropertyType());
                    assertNotNull(descriptor.getWriteMethod(), "the setter is still recovered");
                    assertTrue(descriptor.isBound());
                    assertTrue(descriptor.isExpert());
                    assertTrue(descriptor.isPreferred());
                    assertFalse(descriptor.isHidden());
                    assertEquals("Label Display", descriptor.getDisplayName());
                    assertEquals("A labelled optional", descriptor.getShortDescription());
                    assertEquals("attribute", descriptor.getValue("custom"));
                    return;
                }
            }
            fail("the label property wasn't found");
        }

        public static class WildcardOptionalBean {
            private Number num_;

            public Optional<? extends Number> getNum() { return Optional.ofNullable(num_); }
            public void setNum(Number num) { num_ = num; }
        }

        @Test
        void testWildcardResolvesToUpperBound()
        throws BeanUtilsException {
            // a wildcard value type stands for its upper bound instead of
            // degrading to Object
            assertEquals(Set.of("num"), BeanUtils.getPropertyNames(WildcardOptionalBean.class, null, null, null));
            assertSame(Number.class, BeanUtils.getPropertyType(WildcardOptionalBean.class, "num"));

            var bean = new WildcardOptionalBean();
            BeanUtils.setPropertyValue(bean, "num", 42);
            assertEquals(42, BeanUtils.getPropertyValue(bean, "num"));
        }

        public static class BoundedOptionalBean<T extends Number> {
            private T num_;

            public Optional<T> getNum() { return Optional.ofNullable(num_); }
            public void setNum(T num) { num_ = num; }
        }

        @Test
        void testBoundedVariableErasesToBound()
        throws BeanUtilsException {
            // an unresolved type variable erases to its first bound, like the
            // compiled class file does
            assertEquals(Set.of("num"), BeanUtils.getPropertyNames(BoundedOptionalBean.class, null, null, null));
            assertSame(Number.class, BeanUtils.getPropertyType(BoundedOptionalBean.class, "num"));

            var bean = new BoundedOptionalBean<Integer>();
            BeanUtils.setPropertyValue(bean, "num", 42);
            assertEquals(42, BeanUtils.getPropertyValue(bean, "num"));
        }

        public static class AmbiguousOverloadBean {
            private CharSequence text_;

            public Optional<CharSequence> getText() { return Optional.ofNullable(text_); }
            public void setText(String text) { text_ = text; }
            public void setText(StringBuilder text) { text_ = text; }
        }

        public static class ExactOverloadBean {
            private CharSequence text_;

            public Optional<CharSequence> getText() { return Optional.ofNullable(text_); }
            public void setText(CharSequence text) { text_ = text; }
            public void setText(String text) { text_ = text; }
        }

        @Test
        void testOverloadSelectionIsDeterministic()
        throws BeanUtilsException {
            // incomparable subtype overloads resolve over a stable order
            for (var descriptor : BeanUtils.getBeanInfo(AmbiguousOverloadBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("text")) {
                    assertSame(String.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                }
            }

            // an exact match on the value type wins outright
            for (var descriptor : BeanUtils.getBeanInfo(ExactOverloadBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("text")) {
                    assertSame(CharSequence.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                }
            }
        }

        public static class DualPrimitiveBoxedBean {
            private Integer count_;

            public Optional<Integer> getCount() { return Optional.ofNullable(count_); }
            public void setCount(int count) { count_ = count; }
            public void setCount(Integer count) { count_ = count; }
        }

        @Test
        void testBoxedOverloadPreferredOverPrimitive()
        throws BeanUtilsException {
            // when both counterparts exist, the boxed one wins regardless of
            // reflection order, since it can carry the null of an empty Optional
            for (var descriptor : BeanUtils.getBeanInfo(DualPrimitiveBoxedBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("count")) {
                    assertSame(Integer.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                }
            }

            var properties = BeanUtils.getUppercasedBeanProperties(DualPrimitiveBoxedBean.class);
            var bean = new DualPrimitiveBoxedBean();
            BeanUtils.setUppercasedBeanProperty("COUNT", new String[]{"42"}, null, properties, bean, null);
            assertEquals(Optional.of(42), bean.getCount());

            // through the boxed setter, a blank update restores absence,
            // unlike the primitive-only bean where the value is left untouched
            BeanUtils.setUppercasedBeanProperty("COUNT", new String[0], null, properties, bean, new DualPrimitiveBoxedBean());
            assertEquals(Optional.empty(), bean.getCount());
        }

        public static class SubtypeDualBean {
            private Number value_;

            public Optional<Number> getValue() { return Optional.ofNullable(value_); }
            public void setValue(short value) { value_ = value; }
            public void setValue(Short value) { value_ = value; }
        }

        @Test
        void testBoxedPreferredAtEverySpecificityLevel()
        throws BeanUtilsException {
            // among subtype candidates that resolve to the same type, the
            // boxed declaration wins over its primitive counterpart too
            for (var descriptor : BeanUtils.getBeanInfo(SubtypeDualBean.class).getPropertyDescriptors()) {
                if (descriptor.getName().equals("value")) {
                    assertSame(Short.class, descriptor.getWriteMethod().getParameterTypes()[0]);
                }
            }

            var bean = new SubtypeDualBean();
            BeanUtils.setPropertyValue(bean, "value", (short) 7);
            assertEquals((short) 7, BeanUtils.getPropertyValue(bean, "value"));

            // null flows through the nullable overload instead of failing
            BeanUtils.setPropertyValue(bean, "value", null);
            assertNull(BeanUtils.getPropertyValue(bean, "value"));
        }

        @Test
        void testPrimitiveOptionalsStayOpaque()
        throws BeanUtilsException {
            // OptionalInt, OptionalLong and OptionalDouble are deliberately
            // not unwrapped, they behave like any other opaque property type
            assertSame(OptionalInt.class, BeanUtils.getPropertyType(PrimitiveOptionalBean.class, "score"));
            assertEquals(OptionalInt.of(42), BeanUtils.getPropertyValue(new PrimitiveOptionalBean(), "score"));
        }
    }
}
