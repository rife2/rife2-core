/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.Optional;

/**
 * Test model with an Optional-wrapped many-to-one relationship property,
 * which isn't supported and has to fail loudly instead of silently
 * misbehaving.
 */
public class OptionalManyToOneBean extends MetaData {
    private Integer identifier_;
    private MOSecondBean second_;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("identifier").identifier(true));
        addConstraint(new ConstrainedProperty("second").manyToOne());
    }

    public void setIdentifier(Integer identifier) {
        identifier_ = identifier;
    }

    public Integer getIdentifier() {
        return identifier_;
    }

    public Optional<MOSecondBean> getSecond() {
        return Optional.ofNullable(second_);
    }

    public void setSecond(MOSecondBean second) {
        second_ = second;
    }
}
