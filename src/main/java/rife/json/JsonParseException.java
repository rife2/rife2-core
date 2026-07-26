/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

import java.io.Serial;

/**
 * A {@code JsonParseException} is thrown when a JSON document couldn't be
 * parsed.
 * <p>The line and the column where the parsing went wrong are part of the
 * message of the exception and can also be retrieved individually, so that
 * you can point at the exact location of the problem.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Json
 * @since 1.10
 */
public class JsonParseException extends RuntimeException {
    @Serial private static final long serialVersionUID = 894192516471197179L;

    private final int line_;
    private final int column_;

    /**
     * Creates a new parse exception.
     * <p>The line and column numbers that you provide are appended to the
     * message so that the location of the problem is always part of the
     * description of the exception.
     *
     * @param message the description of the parse problem
     * @param line    the line number where the problem occurred
     * @param column  the column number where the problem occurred
     * @see #getLine()
     * @see #getColumn()
     * @since 1.10
     */
    public JsonParseException(String message, int line, int column) {
        super(message + " at line " + line + ", column " + column);

        line_ = line;
        column_ = column;
    }

    /**
     * Retrieves the line number where the parse problem occurred.
     *
     * @return the line number, starting at {@code 1}
     * @see #getColumn()
     * @since 1.10
     */
    public int getLine() {
        return line_;
    }

    /**
     * Retrieves the column number where the parse problem occurred.
     *
     * @return the column number, starting at {@code 1}
     * @see #getLine()
     * @since 1.10
     */
    public int getColumn() {
        return column_;
    }
}
