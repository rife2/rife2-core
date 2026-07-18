/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class ViewNameRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 7962789542265652844L;

    private final String queryName_;

    public ViewNameRequiredException(String queryName) {
        super(queryName + " queries require a view name.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
