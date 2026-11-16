/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestLocalizationUtils {
    @Test
    void testExtractLocalizedUrl() {
        assertEquals("/root", Localization.extractLocalizedUrl("en:/root,nl:/wortel"));
        assertEquals("/logout", Localization.extractLocalizedUrl("nl:/afmelden,/logout,fr:/deconnection"));
        assertNull(Localization.extractLocalizedUrl(null));
        assertEquals("/root", Localization.extractLocalizedUrl("/root"));
        assertNull(Localization.extractLocalizedUrl("nl:/afmelden,fr:/deconnection"));
        assertEquals("http://www.uwyn.com", Localization.extractLocalizedUrl("http://www.uwyn.com"));
        assertEquals("http://www.uwyn.com", Localization.extractLocalizedUrl("fr:/racine,en:http://www.uwyn.com,nl:/wortel"));
    }

    @Test
    void testRegionalBundleFallsBack() {
        var bundle = Localization.getResourceBundle("localization.fallback", "fr", "BE");
        assertEquals("be c", bundle.getString("c"));
        assertEquals("fr a", bundle.getString("a"));
        assertEquals("base b", bundle.getString("b"));
        assertEquals(Set.of("a", "b", "c"), new HashSet<>(Collections.list(bundle.getKeys())));
        assertEquals("base b", Localization.getString("localization.fallback", "b", "fr", "BE"));
    }

    @Test
    void testDefaultLocaleOnlyWithoutRequestedBundle() {
        var default_locale = Locale.getDefault();
        Locale.setDefault(new Locale("nl"));
        try {
            var french = Localization.getResourceBundle("localization.defaulted", "fr", null);
            assertEquals("fr a", french.getString("a"));
            assertEquals("base b", french.getString("b"));

            var german = Localization.getResourceBundle("localization.defaulted", "de", null);
            assertEquals("nl a", german.getString("a"));
            assertEquals("nl b", german.getString("b"));
        } finally {
            Locale.setDefault(default_locale);
        }
    }
}
