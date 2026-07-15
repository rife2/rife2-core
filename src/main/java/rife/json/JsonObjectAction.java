/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

/**
 * Functional interface that captures an action to execute
 * on a {@link JsonObject} instance.
 * <p>
 * There's no need to implement this interface directly, it's intended
 * to be provided as an inline lambda to
 * {@link JsonObject#object(String, JsonObjectAction)} and
 * {@link JsonArray#object(JsonObjectAction)}, which construct a nested
 * JSON object, hand it to the action to populate, and attach it to
 * the enclosing structure.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
@FunctionalInterface
public interface JsonObjectAction {
    /**
     * Executes the action on the specified {@code JsonObject} instance.
     *
     * @param o the {@code JsonObject} instance on which to execute the action
     * @since 1.10
     */
    void use(JsonObject o);
}
