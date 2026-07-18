/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class IndexNameRequiredException extends DbQueryException {
    @Serial private static final long serialVersionUID = 5911661261564050138L;

    private final String queryName_;

    public IndexNameRequiredException(String queryName) {
        super(queryName + " queries require an index name.");

        queryName_ = queryName;
    }

    public String getQueryName() {
        return queryName_;
    }
}
