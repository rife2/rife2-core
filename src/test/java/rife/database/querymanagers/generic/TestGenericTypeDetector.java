/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.api.Test;
import rifetestmodels.MOSecondBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericTypeDetector {
    public static class RelationBase<T> {
        public List<T> getEntries() { return null; }
    }

    public static class SecondBeanRelations extends RelationBase<MOSecondBean> {}

    public static class WildcardRelations {
        public List<? extends MOSecondBean> getEntries() { return null; }
    }

    @Test
    void testDetectAssociatedClass()
    throws Exception {
        var inherited = SecondBeanRelations.class.getMethod("getEntries");
        // the element type of an inherited generic collection resolves
        // against the concrete bean class
        assertSame(MOSecondBean.class, GenericTypeDetector.detectAssociatedClass(SecondBeanRelations.class, inherited));

        // a wildcard resolves to its upper bound
        var wildcard = WildcardRelations.class.getMethod("getEntries");
        assertSame(MOSecondBean.class, GenericTypeDetector.detectAssociatedClass(WildcardRelations.class, wildcard));

        // an unresolvable element type is null instead of a ClassCastException,
        // which the relationship analysis reports as missing type information
        var unresolved = RelationBase.class.getMethod("getEntries");
        assertNull(GenericTypeDetector.detectAssociatedClass(RelationBase.class, unresolved));
    }
}
