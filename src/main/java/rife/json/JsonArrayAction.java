/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

/**
 * Functional interface that captures an action to execute
 * on a {@link JsonArray} instance.
 * <p>
 * There's no need to implement this interface directly, it's intended
 * to be provided as an inline lambda to
 * {@link JsonObject#array(String, JsonArrayAction)} and
 * {@link JsonArray#array(JsonArrayAction)}, which construct a nested
 * JSON array, hand it to the action to populate, and attach it to
 * the enclosing structure.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
@FunctionalInterface
public interface JsonArrayAction {
    /**
     * Executes the action on the specified {@code JsonArray} instance.
     *
     * @param a the {@code JsonArray} instance on which to execute the action
     * @since 1.10
     */
    void use(JsonArray a);
}
