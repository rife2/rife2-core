/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import java.util.*;

import rife.asm.*;

import static rife.asm.Opcodes.ASM9;

class MetaDataMethodCollector extends ClassVisitor {
    private Map<String, List<String>> methods_ = null;
    private boolean merged_ = false;

    MetaDataMethodCollector() {
        super(ASM9);
    }

    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (MetaDataClassAdapter.DELEGATE_VAR_NAME.equals(name)) {
            merged_ = true;
        }
        return null;
    }

    boolean isMerged() {
        return merged_;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (null == methods_) {
            methods_ = new HashMap<>();
        }
        List<String> descriptions = methods_.computeIfAbsent(name, k -> new ArrayList<>());
        descriptions.add(desc);

        return null;
    }

    public Map<String, List<String>> getMethods() {
        if (null == methods_) {
            return Collections.emptyMap();
        }

        return methods_;
    }
}
