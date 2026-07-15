/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestJsonParser {
    @Test
    void testLargeDocumentRoundtrip() {
        var random = new Random(2026);
        var array = new JsonArray();
        for (var i = 0; i < 40_000; ++i) {
            var id = i;
            array.object(o -> o
                .set("id", (long) id)
                .set("name", "user-" + random.nextInt(100_000))
                .set("email", "user" + random.nextInt(100_000) + "@example.com")
                .set("active", random.nextBoolean())
                .set("score", random.nextDouble() * 100)
                .array("tags", t -> t
                    .append("tag" + random.nextInt(50))
                    .append("tag" + random.nextInt(50))));
        }
        var document = array.toString();
        assertTrue(document.length() > 4_000_000, "the large document should be several megabytes");

        var parsed = Json.parseArray(document);
        assertEquals(40_000, parsed.size());
        assertEquals(0, parsed.getObject(0).getInt("id"));
        assertEquals(39_999, parsed.getObject(39_999).getInt("id"));
        assertEquals(array, parsed);
        assertEquals(document, parsed.toString());
    }

    @Test
    void testDeepNestingBoundary() {
        // the maximum nesting depth parses fine, one level more is rejected
        var at_limit = "[".repeat(512) + "]".repeat(512);
        assertInstanceOf(JsonArray.class, Json.parse(at_limit));

        var beyond_limit = "[".repeat(513) + "]".repeat(513);
        assertThrows(JsonParseException.class, () -> Json.parse(beyond_limit));

        var objects_beyond_limit = "{\"n\":".repeat(513) + "1" + "}".repeat(513);
        assertThrows(JsonParseException.class, () -> Json.parse(objects_beyond_limit));
    }

    @Test
    void testLongStringRoundtrip() {
        var random = new Random(2026);
        var builder = new StringBuilder();
        var pool = "abcdefghijklmnopqrstuvwxyz0123456789 \t\n\r\"\\/\b\fé€日";
        while (builder.length() < 1_000_000) {
            builder.append(pool.charAt(random.nextInt(pool.length())));
        }
        builder.append("😀"); // a surrogate pair survives too
        var value = builder.toString();

        var document = new JsonArray().append(value).toString();
        assertEquals(value, Json.parseArray(document).getString(0));
    }

    @Test
    void testRandomizedRoundtrip() {
        var random = new Random(2026);
        for (var i = 0; i < 300; ++i) {
            var value = randomValue(random, 0);
            var document = Json.toString(value);
            assertEquals(value, Json.parse(document), document);
            assertEquals(value, Json.parse(Json.toPrettyString(value)), document);
        }
    }

    private Object randomValue(Random random, int depth) {
        var choice = random.nextInt(depth >= 5 ? 6 : 8);
        return switch (choice) {
            case 0 -> randomString(random);
            case 1 -> random.nextLong();
            case 2 -> random.nextLong(2_000_000) / 128.0;
            case 3 -> random.nextBoolean();
            case 4 -> null;
            case 5 -> (long) random.nextInt(1_000_000);
            case 6 -> {
                var object = new JsonObject();
                for (var i = random.nextInt(6); i > 0; --i) {
                    object.put(randomString(random), randomValue(random, depth + 1));
                }
                yield object;
            }
            default -> {
                var array = new JsonArray();
                for (var i = random.nextInt(6); i > 0; --i) {
                    array.add(randomValue(random, depth + 1));
                }
                yield array;
            }
        };
    }

    private String randomString(Random random) {
        var pool = "abcdef \"\\/\n\té€日<>&{}[]:,";
        var builder = new StringBuilder();
        for (var i = random.nextInt(24); i > 0; --i) {
            builder.append(pool.charAt(random.nextInt(pool.length())));
        }
        return builder.toString();
    }

    @Test
    void testErrorPositionsAfterBulkScanning() {
        // the parser scans strings and numbers in bulk, error positions
        // must stay exact regardless
        var literal = assertThrows(JsonParseException.class, () -> Json.parse("""
            {
              "name": "some long string value",
              "bad": truX
            }"""));
        assertEquals(3, literal.getLine());
        assertEquals(10, literal.getColumn());

        var unterminated = assertThrows(JsonParseException.class, () -> Json.parse("{\"a\": \"xyz"));
        assertEquals(1, unterminated.getLine());
        assertEquals(11, unterminated.getColumn());

        var escape = assertThrows(JsonParseException.class, () -> Json.parse("[\"ab\\q\"]"));
        assertEquals(1, escape.getLine());
        assertEquals(6, escape.getColumn());

        var control = assertThrows(JsonParseException.class, () -> Json.parse("[\"ab\u0001\"]"));
        assertEquals(1, control.getLine());
        assertEquals(5, control.getColumn());
    }

    @Test
    void testScalars() {
        assertEquals(42L, Json.parse("42"));
        assertEquals(-17L, Json.parse(" -17 "));
        assertEquals(0L, Json.parse("0"));
        assertEquals(-0.5, Json.parse("-0.5"));
        assertEquals(-50.0, Json.parse("-0.5e2"));
        assertEquals(3.25E10, Json.parse("3.25E+10"));
        assertEquals(0.001, Json.parse("1e-3"));
        assertEquals(Boolean.TRUE, Json.parse("true"));
        assertEquals(Boolean.FALSE, Json.parse("false"));
        assertNull(Json.parse("null"));
        assertEquals("hello", Json.parse("\"hello\""));
        assertEquals("", Json.parse("\"\""));
    }

    @Test
    void testLongOverflowFallsBackToDouble() {
        assertEquals(9223372036854775807L, Json.parse("9223372036854775807"));
        assertEquals(9.223372036854776E18, Json.parse("9223372036854775808"));
    }

    @Test
    void testStringEscapes() {
        assertEquals("a\"b\\c/d", Json.parse("\"a\\\"b\\\\c\\/d\""));
        assertEquals("\b\f\n\r\t", Json.parse("\"\\b\\f\\n\\r\\t\""));
        assertEquals("aé", Json.parse("\"a\\u00e9\""));
        assertEquals("aé", Json.parse("\"a\\u00E9\""));
        assertEquals("\0", Json.parse("\"\\u0000\""));
        assertEquals("😀", Json.parse("\"\\uD83D\\uDE00\""));
        assertEquals("é😀", Json.parse("\"é😀\""));
    }

    @Test
    void testUnicodeContent() {
        // characters from various planes and scripts pass through unaltered
        var text = "διακριτικός 中文 العربية ελληνικά 🚀🌍 𝔘𝔫𝔦𝔠𝔬𝔡𝔢 é\u00A0\u2028";
        var parsed = Json.parseObject("{\"text\": \"" + text + "\"}");
        assertEquals(text, parsed.getString("text"));

        // supplementary characters work in member names too
        var keyed = Json.parseObject("{\"🔑\": \"value\"}");
        assertEquals("value", keyed.getString("🔑"));
    }

    @Test
    void testLoneSurrogateEscapeIsAccepted() {
        // RFC 8259 allows any \\uXXXX escape, unpaired surrogates included
        assertEquals("\uD800", Json.parse("\"\\uD800\""));
    }

    @Test
    void testNumbersOutOfRangeAreRejected() {
        assertThrows(JsonParseException.class, () -> Json.parse("1e999"));
        assertThrows(JsonParseException.class, () -> Json.parse("[-1e999]"));
        // underflow flushes to zero instead of losing finiteness
        assertEquals(0.0, Json.parse("1e-999"));
    }

    @Test
    void testInvalidUnicodeEscapes() {
        assertThrows(JsonParseException.class, () -> Json.parse("\"\\u+123\""));
        assertThrows(JsonParseException.class, () -> Json.parse("\"\\u-123\""));
        assertThrows(JsonParseException.class, () -> Json.parse("\"\\u12G4\""));
        assertThrows(JsonParseException.class, () -> Json.parse("\"\\u 123\""));
        assertThrows(JsonParseException.class, () -> Json.parse("\"\\u12\""));
    }

    @Test
    void testLeadingByteOrderMarkIsTolerated() {
        var object = Json.parseObject("\uFEFF{\"name\": \"my-app\"}");
        assertEquals("my-app", object.getString("name"));

        // anywhere else the byte order mark is invalid content
        assertThrows(JsonParseException.class, () -> Json.parse("{\uFEFF\"name\": 1}"));
        assertThrows(JsonParseException.class, () -> Json.parse("\uFEFF\uFEFF{}"));
    }

    @Test
    void testObjects() {
        var object = Json.parseObject("""
            {"name": "my-app", "port": 8080, "debug": false, "extra": null}""");
        assertEquals(4, object.size());
        assertEquals("my-app", object.getString("name"));
        assertEquals(8080, object.getInt("port"));
        assertFalse(object.getBoolean("debug"));
        assertTrue(object.containsKey("extra"));
        assertNull(object.get("extra"));

        assertEquals(0, Json.parseObject("{}").size());
        assertEquals(0, Json.parseObject(" { } ").size());
    }

    @Test
    void testObjectMemberOrderIsPreserved() {
        var object = Json.parseObject("""
            {"one": 1, "two": 2, "three": 3}""");
        assertEquals("[one, two, three]", object.keySet().toString());
    }

    @Test
    void testDuplicateMembersLastOneWins() {
        var object = Json.parseObject("""
            {"name": "first", "name": "second"}""");
        assertEquals(1, object.size());
        assertEquals("second", object.getString("name"));
    }

    @Test
    void testArrays() {
        var array = Json.parseArray("""
            [1, "two", true, null, {"nested": []}]""");
        assertEquals(5, array.size());
        assertEquals(1L, array.get(0));
        assertEquals("two", array.getString(1));
        assertEquals(Boolean.TRUE, array.get(2));
        assertNull(array.get(3));
        assertEquals(0, array.getObject(4).getArray("nested").size());

        assertEquals(0, Json.parseArray("[]").size());
    }

    @Test
    void testNesting() {
        var object = Json.parseObject("""
            {
                "jsonrpc": "2.0",
                "id": 1,
                "result": {
                    "capabilities": {"tools": {"listChanged": true}},
                    "serverInfo": {"name": "bld", "version": "2.4.0"}
                }
            }""");
        assertEquals("2.0", object.getString("jsonrpc"));
        assertEquals(1L, object.getLong("id"));
        assertTrue(object.getObject("result").getObject("capabilities").getObject("tools").getBoolean("listChanged"));
        assertEquals("bld", object.getObject("result").getObject("serverInfo").getString("name"));
    }

    @Test
    void testParseErrors() {
        assertThrows(JsonParseException.class, () -> Json.parse(""));
        assertThrows(JsonParseException.class, () -> Json.parse("   "));
        assertThrows(JsonParseException.class, () -> Json.parse("{\"a\":1} extra"));
        assertThrows(JsonParseException.class, () -> Json.parse("\"unterminated"));
        assertThrows(JsonParseException.class, () -> Json.parse("\"control\tchar\""));
        assertThrows(JsonParseException.class, () -> Json.parse("[1, 2,]"));
        assertThrows(JsonParseException.class, () -> Json.parse("{\"a\":1,}"));
        assertThrows(JsonParseException.class, () -> Json.parse("{'a':1}"));
        assertThrows(JsonParseException.class, () -> Json.parse("{\"a\" 1}"));
        assertThrows(JsonParseException.class, () -> Json.parse("NaN"));
        assertThrows(JsonParseException.class, () -> Json.parse("01"));
        assertThrows(JsonParseException.class, () -> Json.parse("-"));
        assertThrows(JsonParseException.class, () -> Json.parse("1."));
        assertThrows(JsonParseException.class, () -> Json.parse("1e"));
        assertThrows(JsonParseException.class, () -> Json.parse("truthy"));
        assertThrows(JsonParseException.class, () -> Json.parse("\"bad\\escape\""));
        assertThrows(JsonParseException.class, () -> Json.parse("\"bad\\uZZZZ\""));
        assertThrows(JsonParseException.class, () -> Json.parse((String) null));
    }

    @Test
    void testParseErrorLocation() {
        var exception = assertThrows(JsonParseException.class, () -> Json.parse("""
            {
                "name": "my-app",
                "port": &
            }"""));
        assertEquals(3, exception.getLine());
        assertEquals(13, exception.getColumn());
        assertTrue(exception.getMessage().contains("line 3"));
        assertTrue(exception.getMessage().contains("column 13"));
    }

    @Test
    void testParseWrongRootType() {
        assertThrows(JsonParseException.class, () -> Json.parseObject("[1, 2]"));
        assertThrows(JsonParseException.class, () -> Json.parseArray("{\"a\": 1}"));
    }

    @Test
    void testMaximumNestingDepth() {
        var deep = "[".repeat(600) + "]".repeat(600);
        var exception = assertThrows(JsonParseException.class, () -> Json.parse(deep));
        assertTrue(exception.getMessage().contains("nesting depth"));

        // a depth that stays within the limit parses fine
        var acceptable = "[".repeat(500) + "]".repeat(500);
        assertNotNull(Json.parse(acceptable));
    }

    @Test
    void testParseFromReader()
    throws Exception {
        try (var reader = new java.io.StringReader("{\"name\": \"my-app\"}")) {
            var object = (JsonObject) Json.parse(reader);
            assertEquals("my-app", object.getString("name"));
        }

        try (var reader = new java.io.StringReader("{\"name\": \"my-app\"}")) {
            assertEquals("my-app", Json.parseObject(reader).getString("name"));
        }

        try (var reader = new java.io.StringReader("[1, 2]")) {
            assertEquals(2, Json.parseArray(reader).size());
        }

        try (var reader = new java.io.StringReader("[1, 2]")) {
            assertThrows(JsonParseException.class, () -> Json.parseObject(reader));
        }
    }
}
