/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class ViewDefinitionRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 3168575639969948742L;

    private final String queryName_;

    public ViewDefinitionRequiredException(String queryName) {
        super(queryName + " queries require a view definition.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
