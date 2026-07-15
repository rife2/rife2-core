/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the JSON parser against the JSONTestSuite conformance corpus.
 * <p>
 * The corpus resides in the {@code json/test_parsing} test resources and
 * originates from <a href="https://github.com/nst/JSONTestSuite">JSONTestSuite</a>,
 * MIT licensed, vendored at commit {@code 1ef36fa} of November 22, 2024.
 * The two generated deep-nesting documents,
 * {@code n_structure_100000_opening_arrays.json} and
 * {@code n_structure_open_array_object.json}, aren't vendored, they are
 * recreated by this test instead. Files prefixed {@code y_} must parse, files prefixed
 * {@code n_} must be rejected, and files prefixed {@code i_} are
 * implementation-defined: any outcome is valid as long as parsing
 * terminates with a regular result or a {@code JsonParseException}.
 * <p>
 * The corpus files are decoded as strict UTF-8 before parsing, a document
 * whose bytes aren't valid UTF-8 counts as rejected at the transport level.
 */
public class TestJsonConformance {
    @TestFactory
    Stream<DynamicTest> testJsonTestSuite()
    throws IOException, URISyntaxException {
        // the class loader is used instead of the class so that the corpus
        // is also found when the tests run as part of the rife.core module
        var marker = getClass().getClassLoader().getResource("json/test_parsing/LICENSE");
        assertNotNull(marker, "the JSONTestSuite corpus should be present in the test resources");

        var dir = Path.of(marker.toURI()).getParent();
        List<Path> files;
        try (var stream = Files.list(dir)) {
            files = stream.filter(f -> f.getFileName().toString().endsWith(".json")).sorted().toList();
        }
        assertTrue(files.size() > 300, "the JSONTestSuite corpus should be complete");

        return files.stream().map(file -> DynamicTest.dynamicTest(file.getFileName().toString(), () -> {
            var name = file.getFileName().toString();
            var bytes = Files.readAllBytes(file);

            String content = null;
            try {
                content = decodeStrictUtf8(bytes);
            } catch (CharacterCodingException e) {
                // rejected at the transport level
                if (name.startsWith("y_")) {
                    fail("must-accept document isn't valid UTF-8: " + name);
                }
                return;
            }

            final var document = content;
            if (name.startsWith("y_")) {
                assertDoesNotThrow(() -> Json.parse(document), name);
            } else if (name.startsWith("n_")) {
                assertThrows(JsonParseException.class, () -> Json.parse(document), name);
            } else {
                // implementation-defined, both outcomes are valid but
                // parsing has to terminate without any other failure
                try {
                    Json.parse(document);
                } catch (JsonParseException e) {
                    // rejection is a valid outcome
                }
            }
        }));
    }

    @Test
    void testDeeplyNestedStructuresAreRejected() {
        // in-test replacements for the two large structural documents of
        // the corpus that aren't included in the test resources :
        // n_structure_100000_opening_arrays and n_structure_open_array_object
        var arrays = "[".repeat(100000);
        assertThrows(JsonParseException.class, () -> Json.parse(arrays));

        var alternating = "[{\"\":".repeat(50000);
        assertThrows(JsonParseException.class, () -> Json.parse(alternating));
    }

    private static String decodeStrictUtf8(byte[] bytes)
    throws CharacterCodingException {
        var decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
