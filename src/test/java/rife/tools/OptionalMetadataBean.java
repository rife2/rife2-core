/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.Optional;

/**
 * Test support bean whose adjacent {@code OptionalMetadataBeanBeanInfo}
 * provides a customized property descriptor, to verify that descriptor
 * metadata survives the Optional enhancement.
 */
public class OptionalMetadataBean {
    private String label_;

    public Optional<String> getLabel() {
        return Optional.ofNullable(label_);
    }

    public void setLabel(String label) {
        label_ = label;
    }
}
