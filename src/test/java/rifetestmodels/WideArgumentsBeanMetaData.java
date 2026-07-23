/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.MetaDataBeanAware;
import rife.validation.MetaDataMerged;

public class WideArgumentsBeanMetaData implements MetaDataMerged, MetaDataBeanAware, WideArguments {
    private Object bean_;

    public void setMetaDataBean(Object bean) {
        bean_ = bean;
    }

    public Object retrieveMetaDataBean() {
        return bean_;
    }

    public Object associatedBean() {
        return bean_;
    }

    public String combine(long first, String second, double third, String fourth) {
        return first + ":" + second + ":" + third + ":" + fourth;
    }
}
