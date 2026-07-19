/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.migrations.exceptions;

import java.io.Serial;

public class IrreversibleMigrationException extends MigrationException {
    @Serial private static final long serialVersionUID = 1187475271204290622L;

    private final int version_;

    public IrreversibleMigrationException(int version) {
        super("The migration with version " + version + " doesn't implement down and can't be rolled back.");

        version_ = version;
    }

    public int getVersion() {
        return version_;
    }
}
