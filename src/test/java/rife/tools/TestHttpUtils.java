/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHttpUtils {
    @Test
    void testBasicAuthorizationHeaderUsesUtf8() {
        assertEquals("Basic " + Base64.getEncoder().encodeToString("jörg:päss".getBytes(StandardCharsets.UTF_8)),
            HttpUtils.basicAuthorizationHeader("jörg", "päss"));
    }
}
