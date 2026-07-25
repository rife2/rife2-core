/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.Optional;

/**
 * Test model with the idiomatic Optional shape, Optional getters and
 * plain-typed setters, for verifying that such beans persist and restore
 * transparently through the generic query manager.
 */
public class OptionalGqmBean extends MetaData {
    private Integer identifier_;
    private String name_;
    private Integer count_;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name_);
    }

    public void setName(String name) {
        name_ = name;
    }

    public Optional<Integer> getCount() {
        return Optional.ofNullable(count_);
    }

    public void setCount(Integer count) {
        count_ = count;
    }
}
