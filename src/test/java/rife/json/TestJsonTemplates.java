/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import org.junit.jupiter.api.Test;
import rife.template.TemplateFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TestJsonTemplates {
    @Test
    void testJsonTemplateInterop() {
        var template = TemplateFactory.JSON.get("templates.json_interop");

        // string content is escaped by the template's JSON encoder
        template.setValueEncoded("name", "line1\nline2 \"quoted\" €😀");
        // scalars and whole JSON structures are embedded as-is
        template.setValue("count", 42);
        template.setValue("data", new JsonObject()
            .set("nested", true)
            .array("tags", t -> t.append("a").append("b")));

        // the template output is valid JSON and the values survive intact
        var parsed = Json.parseObject(template.getContent());
        assertEquals("line1\nline2 \"quoted\" €😀", parsed.getString("name"));
        assertEquals(42, parsed.getInt("count"));
        assertTrue(parsed.getObject("data").getBoolean("nested"));
        assertEquals("a", parsed.getObject("data").getArray("tags").getString(0));
        assertEquals("b", parsed.getObject("data").getArray("tags").getString(1));
    }

    @Test
    void testSetBeanOnJsonTemplate() {
        var template = TemplateFactory.JSON.get("templates.json_interop_bean");

        var user = new User();
        user.setUsername("john \"the builder\"\nnewline");
        user.setId(7);
        template.setBean(user);

        var parsed = Json.parseObject(template.getContent());
        assertEquals("john \"the builder\"\nnewline", parsed.getString("username"));
        assertEquals(7, parsed.getInt("id"));
    }

    @Test
    void testSetBeanHonorsSerializedConstraint() {
        var template = TemplateFactory.JSON.get("templates.json_interop_constrained");

        var account = new TestJson.Account();
        account.setUsername("john");
        account.setPasswordHash("hash");
        template.setBean(account);

        assertTrue(template.isValueSet("username"));
        assertFalse(template.isValueSet("passwordHash"));
    }

    @Test
    void testJsonBeanHandlerMimeType() {
        assertSame(rife.cmf.MimeType.APPLICATION_JSON, rife.template.BeanHandlerJson.instance().getMimeType());
    }

    public static class User {
        private String username_;
        private int id_;

        public String getUsername() { return username_; }
        public void setUsername(String username) { username_ = username; }
        public int getId() { return id_; }
        public void setId(int id) { id_ = id; }
    }
}
