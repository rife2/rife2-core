/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Custom BeanInfo for {@code OptionalMetadataBean} that decorates the
 * {@code label} property descriptor with metadata, so tests can verify it
 * survives the Optional enhancement.
 */
public class OptionalMetadataBeanBeanInfo extends SimpleBeanInfo {
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            // the standard introspector refuses the mismatched setter, so the
            // descriptor is provided with the getter alone, like the
            // introspector itself would
            var label = new PropertyDescriptor("label",
                OptionalMetadataBean.class.getMethod("getLabel"), null);
            label.setBound(true);
            label.setExpert(true);
            label.setPreferred(true);
            label.setDisplayName("Label Display");
            label.setShortDescription("A labelled optional");
            label.setValue("custom", "attribute");
            return new PropertyDescriptor[]{label};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
