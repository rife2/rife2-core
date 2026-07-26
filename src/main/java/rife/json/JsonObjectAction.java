/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

/**
 * Captures an action that has to be executed on a {@link JsonObject}
 * instance.
 * <p>There's no need to implement this interface directly since it's
 * intended to be provided as an inline lambda to
 * {@link JsonObject#object(String, JsonObjectAction)} and
 * {@link JsonArray#object(JsonObjectAction)}. Those methods will construct
 * a nested JSON object, hand it to the action so that you can populate it,
 * and attach it to the enclosing structure afterwards.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see JsonArrayAction
 * @since 1.10
 */
@FunctionalInterface
public interface JsonObjectAction {
    /**
     * Executes the action on the specified {@code JsonObject} instance.
     * <p>The JSON object that is handed to you has already been created,
     * so that you only have to populate it.
     *
     * @param o the {@code JsonObject} instance on which to execute the action
     * @since 1.10
     */
    void use(JsonObject o);
}
