/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestJson {
    @Test
    void testFluentConstruction() {
        var json = new JsonObject()
            .set("name", "my-app")
            .set("port", 8080)
            .set("active", true)
            .set("extra", null)
            .object("server", s -> s
                .set("host", "localhost")
                .array("protocols", p -> p
                    .append("http")
                    .append("https")))
            .array("tags", t -> t
                .append("java")
                .object(o -> o.set("nested", 1)));

        assertEquals("""
            {"name":"my-app","port":8080,"active":true,"extra":null,"server":{"host":"localhost","protocols":["http","https"]},"tags":["java",{"nested":1}]}""",
            json.toString());
    }

    @Test
    void testPrettyPrinting() {
        var json = new JsonObject()
            .set("name", "my-app")
            .object("server", s -> s.set("host", "localhost"))
            .array("tags", t -> t.append("java"))
            .object("empty", o -> {})
            .array("none", a -> {});

        assertEquals("""
            {
                "name": "my-app",
                "server": {
                    "host": "localhost"
                },
                "tags": [
                    "java"
                ],
                "empty": {},
                "none": []
            }""",
            json.toPrettyString());
    }

    @Test
    void testPrintToWriter()
    throws Exception {
        var json = new JsonObject()
            .set("name", "my-app")
            .array("tags", t -> t.append("java"));

        var compact = new java.io.StringWriter();
        json.print(compact);
        assertEquals(json.toString(), compact.toString());

        var pretty = new java.io.StringWriter();
        json.prettyPrint(pretty);
        assertEquals(json.toPrettyString(), pretty.toString());

        var array = new java.io.StringWriter();
        json.getArray("tags").print(array);
        assertEquals("[\"java\"]", array.toString());

        // an unbuffered writer goes through the streaming buffered path
        var streamed = new java.io.CharArrayWriter();
        json.print(streamed);
        assertEquals(json.toString(), streamed.toString());
    }

    @Test
    void testArrayTypedRetrieval() {
        var array = Json.parseArray("""
            [42, 9999999999, 0.5, true, "8080", null]""");
        assertEquals(42, array.getInt(0));
        assertEquals(42L, array.getLong(0));
        assertEquals(9999999999L, array.getLong(1));
        assertEquals(0.5, array.getDouble(2));
        assertTrue(array.getBoolean(3));
        assertEquals(8080, array.getInt(4));
        assertEquals(0, array.getInt(5));
        assertFalse(array.getBoolean(5));
    }

    @Test
    void testTypedRetrieval() {
        var json = Json.parseObject("""
            {"name": "my-app", "port": 8080, "ratio": 0.5, "active": true, "id": 9999999999}""");

        assertEquals("my-app", json.getString("name"));
        assertEquals("fallback", json.getString("missing", "fallback"));
        assertNull(json.getString("missing"));

        assertEquals(8080, json.getInt("port"));
        assertEquals(80, json.getInt("missing", 80));
        assertEquals(9999999999L, json.getLong("id"));
        assertEquals(0.5, json.getDouble("ratio"));
        assertEquals(8080.0, json.getDouble("port"));
        assertTrue(json.getBoolean("active"));
        assertFalse(json.getBoolean("missing"));
        assertTrue(json.getBoolean("missing", true));

        // string values coerce to numbers and booleans
        var strings = Json.parseObject("""
            {"port": "8080", "active": "true"}""");
        assertEquals(8080, strings.getInt("port"));
        assertTrue(strings.getBoolean("active"));
    }

    @Test
    void testSerializationEscaping() {
        var json = new JsonObject()
            .set("text", "line1\nline2\t\"quoted\" \\ €é");

        assertEquals("""
            {"text":"line1\\nline2\\t\\"quoted\\" \\\\ \\u20acé"}""", json.toString());

        // roundtrip preserves the original value
        assertEquals("line1\nline2\t\"quoted\" \\ €é",
            Json.parseObject(json.toString()).getString("text"));
    }

    @Test
    void testUnicodeRoundtrip() {
        // characters from various planes and scripts survive
        // serialization and parsing in both member names and values
        var text = "διακριτικός 中文 العربية 🚀🌍 𝔘𝔫𝔦𝔠𝔬𝔡𝔢 é";
        var json = new JsonObject().set("🔑 name", text);
        var roundtrip = Json.parseObject(json.toString());
        assertEquals(text, roundtrip.getString("🔑 name"));

        // supplementary characters serialize as literal surrogate pairs
        assertTrue(json.toString().contains("🚀"));
    }

    @Test
    void testUnpairedSurrogatesAreEscapedInOutput() {
        var json = new JsonObject()
            .set("lone-high", "a\uD800b")
            .set("lone-low", "a\uDC00b")
            .set("paired", "a😀b");

        var output = json.toString();
        assertTrue(output.contains("a\\ud800b"));
        assertTrue(output.contains("a\\udc00b"));
        assertTrue(output.contains("a😀b"));

        // the output is well-formed and preserves the original values
        var roundtrip = Json.parseObject(output);
        assertEquals("a\uD800b", roundtrip.getString("lone-high"));
        assertEquals("a\uDC00b", roundtrip.getString("lone-low"));
        assertEquals("a😀b", roundtrip.getString("paired"));
    }

    @Test
    void testSerializationRoundtrip() {
        var original = """
            {"name":"my-app","values":[1,-2.5,true,null,"text"],"nested":{"deep":[[1],[2]]}}""";
        assertEquals(original, Json.parseObject(original).toString());
    }

    @Test
    void testSerializationOfCommonTypes() {
        var json = new JsonObject()
            .set("integer", 42)
            .set("floats", 1.5f)
            .set("character", 'c')
            .set("map", new LinkedHashMap<>(java.util.Map.of("key", "value")))
            .set("list", List.of(1, 2));

        var parsed = Json.parseObject(json.toString());
        assertEquals(42, parsed.getInt("integer"));
        assertEquals(1.5, parsed.getDouble("floats"));
        assertEquals("c", parsed.getString("character"));
        assertEquals("value", parsed.getObject("map").getString("key"));
        assertEquals(2, parsed.getArray("list").size());
    }

    @Test
    void testSerializationOfArrays() {
        var json = new JsonObject()
            .set("strings", new String[]{"a", "b"})
            .set("ints", new int[]{1, 2, 3})
            .set("nested", new Object[]{new int[]{4}, List.of(5)});

        assertEquals("""
            {"strings":["a","b"],"ints":[1,2,3],"nested":[[4],[5]]}""", json.toString());
    }

    @Test
    void testFluentValuesAreNormalized() {
        var json = new JsonObject()
            .set("map", java.util.Map.of("a", 1))
            .set("list", List.of(java.util.Map.of("b", 2)))
            .set("array", new String[]{"c"});

        assertEquals(1, json.getObject("map").getInt("a"));
        assertEquals(2, json.getArray("list").getObject(0).getInt("b"));
        assertEquals("c", json.getArray("array").getString(0));

        var array = new JsonArray(List.of(java.util.Map.of("d", 3)))
            .append(java.util.Map.of("e", 4));
        assertEquals(3, array.getObject(0).getInt("d"));
        assertEquals(4, array.getObject(1).getInt("e"));

        var object = new JsonObject(java.util.Map.of("f", List.of(5)));
        assertEquals(5, object.getArray("f").getInt(0));
    }

    @Test
    void testRawMutatorsNormalize() {
        var json = new JsonObject();
        json.put("put", java.util.Map.of("a", 1));
        json.putAll(java.util.Map.of("putAll", List.of(2)));
        json.putIfAbsent("putIfAbsent", java.util.Map.of("b", 3));
        json.merge("merge", java.util.Map.of("c", 4), (existing, provided) -> provided);
        json.computeIfAbsent("compute", key -> List.of(5));
        json.replace("put", java.util.Map.of("a", 6));

        assertEquals(6, json.getObject("put").getInt("a"));
        assertEquals(2, json.getArray("putAll").getInt(0));
        assertEquals(3, json.getObject("putIfAbsent").getInt("b"));
        assertEquals(4, json.getObject("merge").getInt("c"));
        assertEquals(5, json.getArray("compute").getInt(0));

        var array = new JsonArray();
        array.add(java.util.Map.of("d", 7));
        array.add(0, List.of(8));
        array.addAll(List.of(java.util.Map.of("e", 9)));
        array.set(0, new int[]{10});

        assertEquals(10, array.getArray(0).getInt(0));
        assertEquals(7, array.getObject(1).getInt("d"));
        assertEquals(9, array.getObject(2).getInt("e"));
    }

    @Test
    void testConversionBridge()
    throws Exception {
        assertEquals(1, JsonObject.parse("{\"a\":1}").getInt("a"));
        assertEquals(2, JsonArray.parse("[2]").getInt(0));
        assertThrows(JsonParseException.class, () -> JsonObject.parse("[1]"));
        assertThrows(JsonParseException.class, () -> JsonArray.parse("{}"));

        // RIFE2's standard conversions pick up the static parse methods
        var object = (JsonObject) rife.tools.Convert.toType("{\"a\":1}", JsonObject.class);
        assertEquals(1, object.getInt("a"));
        var array = (JsonArray) rife.tools.Convert.toType("[1,2]", JsonArray.class);
        assertEquals(2, array.size());
    }

    @Test
    void testTemporalGetters() {
        var json = Json.parseObject("""
            {
                "created": "2026-07-15T10:15:30Z",
                "day": "2026-07-15",
                "timestamp": "2026-07-15T10:15:30",
                "time": "10:15:30",
                "epoch": 0,
                "invalid": "not a moment"
            }""");

        assertEquals(Instant.parse("2026-07-15T10:15:30Z"), json.getInstant("created"));
        assertEquals(Date.from(Instant.parse("2026-07-15T10:15:30Z")), json.getDate("created"));
        assertEquals(LocalDate.of(2026, 7, 15), json.getLocalDate("day"));
        assertEquals(LocalDateTime.of(2026, 7, 15, 10, 15, 30), json.getLocalDateTime("timestamp"));
        assertEquals(java.time.LocalTime.of(10, 15, 30), json.getLocalTime("time"));
        // epoch millisecond numbers convert too
        assertEquals(new Date(0), json.getDate("epoch"));
        // absent members are null
        assertNull(json.getInstant("missing"));
        // failed conversions point at the member
        var failure = assertThrows(IllegalArgumentException.class, () -> json.getInstant("invalid"));
        assertTrue(failure.getMessage().contains("'invalid'"));

        var array = Json.parseArray("[\"2026-07-15T10:15:30Z\", null]");
        assertEquals(Instant.parse("2026-07-15T10:15:30Z"), array.getInstant(0));
        assertNull(array.getInstant(1));
    }

    @Test
    void testCollectionConversion() {
        var address1 = new Address();
        address1.setStreet("Main 1");
        address1.setCity("Ghent");
        var address2 = new Address();
        address2.setStreet("Side 2");
        address2.setCity("Bruges");

        // a collection of beans converts to a JSON array
        var json = Json.from(List.of(address1, address2));
        assertEquals("""
            [{"city":"Ghent","street":"Main 1"},{"city":"Bruges","street":"Side 2"}]""", json.toString());

        // and converts back to a list of beans
        var restored = Json.parseArray(json.toString()).toBeanList(Address.class);
        assertEquals(2, restored.size());
        assertEquals("Main 1", restored.get(0).getStreet());
        assertEquals("Bruges", restored.get(1).getCity());

        // null elements stay null, non-objects fail with a clear message
        var with_null = new JsonArray().append(null);
        assertNull(with_null.toBeanList(Address.class).get(0));
        var not_object = new JsonArray().append("text");
        var failure = assertThrows(IllegalArgumentException.class, () -> not_object.toBeanList(Address.class));
        assertTrue(failure.getMessage().contains("element 0"));

        // records work through the same path
        var ranges = Json.from(List.of(new Range(1.0, 2.0), new Range(3.0, 4.0)));
        assertEquals(List.of(new Range(1.0, 2.0), new Range(3.0, 4.0)),
            Json.toBeanList(ranges, Range.class));
    }

    @Test
    void testRecordRoundtrip() {
        var report = new Report("uptime", 99, List.of("daily", "weekly"),
            new Range(new java.math.BigDecimal("1.5").doubleValue(), 10.0));

        var json = Json.from(report);
        assertEquals("""
            {"name":"uptime","score":99,"tags":["daily","weekly"],"range":{"min":1.5,"max":10.0}}""", json.toString());

        assertEquals(report, Json.toBean(Json.parseObject(json.toString()), Report.class));
    }

    @Test
    void testRecordExclusionsAndDefaults() {
        var report = new Report("uptime", 99, List.of("daily"), new Range(1.0, 2.0));
        var json = Json.fromExcluded(report, "tags", "range");
        assertEquals("""
            {"name":"uptime","score":99}""", json.toString());

        // absent members become null or primitive defaults
        var bean = Json.toBean(Json.parseObject("{\"name\":\"partial\"}"), Report.class);
        assertEquals("partial", bean.name());
        assertEquals(0, bean.score());
        assertNull(bean.tags());
        assertNull(bean.range());
    }

    public record Range(double min, double max) {}

    public record Report(String name, int score, List<String> tags, Range range) {}

    @Test
    void testTemporalBeanRoundtrip() {
        var event = new Event();
        event.setCreation(new Date(0));
        event.setInstant(Instant.parse("2026-07-15T10:15:30Z"));
        event.setDay(LocalDate.of(2026, 7, 15));
        event.setTimestamp(LocalDateTime.of(2026, 7, 15, 10, 15, 30));
        event.setMoment(OffsetDateTime.of(2026, 7, 15, 10, 15, 30, 0, ZoneOffset.ofHours(2)));
        event.setStatus(Event.Status.ACTIVE);

        var bean = Json.toBean(Json.parseObject(Json.from(event).toString()), Event.class);
        assertEquals(event.getCreation(), bean.getCreation());
        assertEquals(event.getInstant(), bean.getInstant());
        assertEquals(event.getDay(), bean.getDay());
        assertEquals(event.getTimestamp(), bean.getTimestamp());
        assertEquals(event.getMoment(), bean.getMoment());
        assertEquals(Event.Status.ACTIVE, bean.getStatus());
    }

    @Test
    void testConversionConstructors() {
        var object = new JsonObject(new LinkedHashMap<>(java.util.Map.of("key", "value")))
            .set("added", 1);
        assertEquals("value", object.getString("key"));
        assertEquals(1, object.getInt("added"));

        var array = new JsonArray(List.of(1, 2))
            .append(3);
        assertEquals("[1,2,3]", array.toString());
    }

    @Test
    void testFacadeSerialization() {
        assertEquals("\"line1\\nline2\"", Json.toString("line1\nline2"));
        assertEquals("42", Json.toString(42));
        assertEquals("true", Json.toString(true));
        assertEquals("null", Json.toString(null));
        assertEquals("[1,2,3]", Json.toString(new int[]{1, 2, 3}));
        assertEquals("""
            {"key":[1,2]}""", Json.toString(java.util.Map.of("key", List.of(1, 2))));
        assertEquals("""
            {
                "key": "value"
            }""", Json.toPrettyString(java.util.Map.of("key", "value")));
    }

    @Test
    void testNativeArraySerialization() {
        assertEquals("[1,2,3]", Json.toString(new int[]{1, 2, 3}));
        assertEquals("[9223372036854775807]", Json.toString(new long[]{Long.MAX_VALUE}));
        assertEquals("[1.5,-0.25]", Json.toString(new double[]{1.5, -0.25}));
        assertEquals("[\"a\",\"b\"]", Json.toString(new String[]{"a", "b"}));
        assertEquals("[]", Json.toString(new int[0]));
        assertEquals("""
            [
                1,
                2
            ]""", Json.toPrettyString(new int[]{1, 2}));
    }

    @Test
    void testSerializationRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class,
            () -> new JsonObject().set("bad", Double.NaN).toString());
        assertThrows(IllegalArgumentException.class,
            () -> Json.toString(new double[]{Double.NaN}));
        assertThrows(IllegalArgumentException.class,
            () -> Json.toString(new double[]{Double.NEGATIVE_INFINITY}));
        assertThrows(IllegalArgumentException.class,
            () -> new JsonObject().set("bad", Double.POSITIVE_INFINITY).toString());
        assertThrows(IllegalArgumentException.class,
            () -> new JsonObject().set("bad", new Object()).toString());
    }

    @Test
    void testBeanToJson() {
        var customer = new Customer();
        customer.setName("John");
        customer.setCount(3);
        customer.setActive(true);
        customer.setTags(List.of("new", "verified"));
        var address = new Address();
        address.setStreet("Main Street 1");
        address.setCity("Springfield");
        customer.setAddress(address);

        var json = Json.from(customer);
        assertEquals("John", json.getString("name"));
        assertEquals(3, json.getInt("count"));
        assertTrue(json.getBoolean("active"));
        assertEquals(List.of("new", "verified"), json.getArray("tags"));
        assertEquals("Springfield", json.getObject("address").getString("city"));

        var excluded = Json.fromExcluded(customer, "tags", "address");
        assertEquals("John", excluded.getString("name"));
        assertFalse(excluded.containsKey("tags"));
        assertFalse(excluded.containsKey("address"));
    }

    @Test
    void testBeanDatesSerializeAsIso8601() {
        var customer = new Customer();
        customer.setCreation(new Date(0));

        var json = Json.from(customer);
        assertEquals("1970-01-01T00:00:00Z", json.getString("creation"));
    }

    @Test
    void testJsonToBean() {
        var customer = Json.parseObject("""
            {
                "name": "John",
                "count": 3,
                "active": true,
                "tags": ["new", "verified"],
                "address": {"street": "Main Street 1", "city": "Springfield"},
                "unknown": "is ignored"
            }""").toBean(Customer.class);

        assertEquals("John", customer.getName());
        assertEquals(3, customer.getCount());
        assertTrue(customer.isActive());
        assertEquals(List.of("new", "verified"), customer.getTags());
        assertEquals("Main Street 1", customer.getAddress().getStreet());
        assertEquals("Springfield", customer.getAddress().getCity());
    }

    @Test
    void testBeanRoundtrip() {
        var customer = new Customer();
        customer.setName("Jane");
        customer.setCount(7);
        var address = new Address();
        address.setCity("Shelbyville");
        customer.setAddress(address);

        var roundtrip = Json.from(customer).toBean(Customer.class);
        assertEquals("Jane", roundtrip.getName());
        assertEquals(7, roundtrip.getCount());
        assertEquals("Shelbyville", roundtrip.getAddress().getCity());
    }

    @Test
    void testSharedBeanReferencesAreAllowed() {
        var address = new Address();
        address.setCity("Springfield");
        var shipment = new Shipment();
        shipment.setOrigin(address);
        shipment.setDestination(address);

        var json = Json.from(shipment);
        assertEquals("Springfield", json.getObject("origin").getString("city"));
        assertEquals("Springfield", json.getObject("destination").getString("city"));
    }

    @Test
    void testCyclicalBeanReferencesAreDetected() {
        var node = new Node();
        node.setNext(node);

        var exception = assertThrows(IllegalArgumentException.class, () -> Json.from(node));
        assertTrue(exception.getMessage().contains("Cyclical"));
    }

    @Test
    void testTypedCollectionBinding() {
        var order = Json.parseObject("""
            {
                "addresses": [{"city": "Springfield"}, {"city": "Shelbyville"}],
                "byCity": {"home": {"city": "Springfield"}},
                "labels": ["a", "b", "a"],
                "quantities": [1, 2, 3],
                "history": [{"city": "Ogdenville"}],
                "counts": [4, 5]
            }""").toBean(Order.class);

        // list elements are converted to the generic element type
        assertEquals(2, order.getAddresses().size());
        assertEquals("Springfield", order.getAddresses().get(0).getCity());
        assertEquals("Shelbyville", order.getAddresses().get(1).getCity());
        // map values are converted to the generic value type
        assertEquals("Springfield", order.getByCity().get("home").getCity());
        // sets preserve iteration order and drop duplicates
        assertEquals(List.of("a", "b"), List.copyOf(order.getLabels()));
        // numeric elements are converted to the generic element type
        assertEquals(List.of(1, 2, 3), order.getQuantities());
        // arrays are converted to their component types
        assertEquals("Ogdenville", order.getHistory()[0].getCity());
        assertArrayEquals(new int[]{4, 5}, order.getCounts());
    }

    @Test
    void testTypedCollectionRoundtrip() {
        var address1 = new Address();
        address1.setCity("Springfield");
        var address2 = new Address();
        address2.setCity("Shelbyville");

        var order = new Order();
        order.setAddresses(List.of(address1, address2));
        order.setByCity(java.util.Map.of("home", address1));
        order.setLabels(new java.util.LinkedHashSet<>(List.of("a", "b")));
        order.setQuantities(List.of(1, 2));
        order.setHistory(new Address[]{address2});
        order.setCounts(new int[]{4, 5});

        var roundtrip = Json.from(order).toBean(Order.class);
        assertEquals("Springfield", roundtrip.getAddresses().get(0).getCity());
        assertEquals("Shelbyville", roundtrip.getAddresses().get(1).getCity());
        assertEquals("Springfield", roundtrip.getByCity().get("home").getCity());
        assertEquals(List.of("a", "b"), List.copyOf(roundtrip.getLabels()));
        assertEquals(List.of(1, 2), roundtrip.getQuantities());
        assertEquals("Shelbyville", roundtrip.getHistory()[0].getCity());
        assertArrayEquals(new int[]{4, 5}, roundtrip.getCounts());
    }

    public static class Order {
        private List<Address> addresses_;
        private java.util.Map<String, Address> byCity_;
        private java.util.Set<String> labels_;
        private List<Integer> quantities_;
        private Address[] history_;
        private int[] counts_;

        public List<Address> getAddresses() { return addresses_; }
        public void setAddresses(List<Address> addresses) { addresses_ = addresses; }
        public java.util.Map<String, Address> getByCity() { return byCity_; }
        public void setByCity(java.util.Map<String, Address> byCity) { byCity_ = byCity; }
        public java.util.Set<String> getLabels() { return labels_; }
        public void setLabels(java.util.Set<String> labels) { labels_ = labels; }
        public List<Integer> getQuantities() { return quantities_; }
        public void setQuantities(List<Integer> quantities) { quantities_ = quantities; }
        public Address[] getHistory() { return history_; }
        public void setHistory(Address[] history) { history_ = history; }
        public int[] getCounts() { return counts_; }
        public void setCounts(int[] counts) { counts_ = counts; }
    }

    @Test
    void testSerializedConstraintExcludesInBothDirections() {
        var account = new Account();
        account.setUsername("john");
        account.setPasswordHash("hash-value");
        account.setId(7);

        // not serialized : excluded from generation,
        // not editable : still generated
        var json = Json.from(account);
        assertEquals("john", json.getString("username"));
        assertFalse(json.containsKey("passwordHash"));
        assertEquals(7, json.getInt("id"));
        // nothing else leaks into the JSON, metadata internals included
        assertEquals(2, json.size());

        // not serialized and not editable : both ignored when binding
        var bound = Json.parseObject("""
            {"username": "jane", "passwordHash": "injected", "id": 99}""").toBean(Account.class);
        assertEquals("jane", bound.getUsername());
        assertNull(bound.getPasswordHash());
        assertEquals(0, bound.getId());
    }

    public static class Account extends rife.validation.MetaData {
        private String username_;
        private String passwordHash_;
        private int id_;

        public void activateMetaData() {
            addConstraint(new rife.validation.ConstrainedProperty("passwordHash")
                .serialized(false));
            addConstraint(new rife.validation.ConstrainedProperty("id")
                .editable(false));
        }

        public String getUsername() { return username_; }
        public void setUsername(String username) { username_ = username; }
        public String getPasswordHash() { return passwordHash_; }
        public void setPasswordHash(String passwordHash) { passwordHash_ = passwordHash; }
        public int getId() { return id_; }
        public void setId(int id) { id_ = id; }
    }

    public static class Customer {
        private String name_;
        private int count_;
        private boolean active_;
        private List<String> tags_;
        private Address address_;
        private Date creation_;

        public String getName() { return name_; }
        public void setName(String name) { name_ = name; }
        public int getCount() { return count_; }
        public void setCount(int count) { count_ = count; }
        public boolean isActive() { return active_; }
        public void setActive(boolean active) { active_ = active; }
        public List<String> getTags() { return tags_; }
        public void setTags(List<String> tags) { tags_ = tags; }
        public Address getAddress() { return address_; }
        public void setAddress(Address address) { address_ = address; }
        public Date getCreation() { return creation_; }
        public void setCreation(Date creation) { creation_ = creation; }
    }

    public static class Event {
        public enum Status {ACTIVE, CLOSED}

        private Date creation_;
        private Instant instant_;
        private LocalDate day_;
        private LocalDateTime timestamp_;
        private OffsetDateTime moment_;
        private Status status_;

        public Date getCreation() { return creation_; }
        public void setCreation(Date creation) { creation_ = creation; }
        public Instant getInstant() { return instant_; }
        public void setInstant(Instant instant) { instant_ = instant; }
        public LocalDate getDay() { return day_; }
        public void setDay(LocalDate day) { day_ = day; }
        public LocalDateTime getTimestamp() { return timestamp_; }
        public void setTimestamp(LocalDateTime timestamp) { timestamp_ = timestamp; }
        public OffsetDateTime getMoment() { return moment_; }
        public void setMoment(OffsetDateTime moment) { moment_ = moment; }
        public Status getStatus() { return status_; }
        public void setStatus(Status status) { status_ = status; }
    }

    public static class Address {
        private String street_;
        private String city_;

        public String getStreet() { return street_; }
        public void setStreet(String street) { street_ = street; }
        public String getCity() { return city_; }
        public void setCity(String city) { city_ = city; }
    }

    public static class Shipment {
        private Address origin_;
        private Address destination_;

        public Address getOrigin() { return origin_; }
        public void setOrigin(Address origin) { origin_ = origin; }
        public Address getDestination() { return destination_; }
        public void setDestination(Address destination) { destination_ = destination; }
    }

    public static class Node {
        private Node next_;

        public Node getNext() { return next_; }
        public void setNext(Node next) { next_ = next; }
    }
}
