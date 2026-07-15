/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.cmf.MimeType;
import rife.forms.FormBuilder;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ConstrainedUtils;

import java.util.Map;

public class BeanHandlerJson extends AbstractBeanHandler {
    BeanHandlerJson() {
    }

    public static BeanHandlerJson instance() {
        return BeanHandlerJsonSingleton.INSTANCE;
    }

    public MimeType getMimeType() {
        return MimeType.APPLICATION_JSON;
    }

    public FormBuilder getFormBuilder() {
        return null;
    }

    protected Map<String, Object> getPropertyValues(Template template, Object bean, String prefix)
    throws BeanUtilsException {
        var values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, bean, template.getAvailableValueIds(), null, prefix);
        var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        if (constrained != null) {
            values.keySet().removeIf(name -> {
                var property_name = prefix == null ? name : name.substring(prefix.length());
                var property = constrained.getConstrainedProperty(property_name);
                return property != null && !property.isSerialized();
            });
        }
        return values;
    }
}
