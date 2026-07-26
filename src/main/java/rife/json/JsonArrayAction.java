/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

/**
 * Captures an action that has to be executed on a {@link JsonArray}
 * instance.
 * <p>There's no need to implement this interface directly since it's
 * intended to be provided as an inline lambda to
 * {@link JsonObject#array(String, JsonArrayAction)} and
 * {@link JsonArray#array(JsonArrayAction)}. Those methods will construct a
 * nested JSON array, hand it to the action so that you can populate it,
 * and attach it to the enclosing structure afterwards.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see JsonObjectAction
 * @since 1.10
 */
@FunctionalInterface
public interface JsonArrayAction {
    /**
     * Executes the action on the specified {@code JsonArray} instance.
     * <p>The JSON array that is handed to you has already been created,
     * so that you only have to populate it.
     *
     * @param a the {@code JsonArray} instance on which to execute the action
     * @since 1.10
     */
    void use(JsonArray a);
}
