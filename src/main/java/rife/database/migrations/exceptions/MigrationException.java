/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class MigrationException extends DatabaseException {
    @Serial private static final long serialVersionUID = 7135752505527269843L;

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
