/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

/**
 * Provides JSON parsing, generation and bean conversion.
 * <p>
 * JSON templates interoperate directly with this package: whole JSON
 * structures, numbers and literals are embedded into template values with
 * {@code setValue}, while string content that appears inside quoted
 * positions should be set with {@code setValueEncoded} so that it's
 * escaped by the template's JSON encoder.
 *
 * @since 1.10
 */
package rife.json;
