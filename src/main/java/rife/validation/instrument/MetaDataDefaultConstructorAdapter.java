/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.*;

class MetaDataDefaultConstructorAdapter extends MethodVisitor implements Opcodes {
    private String baseInternalName_ = null;
    private String metaDataInternalName_ = null;
    private boolean metaDataBeanAware_ = false;

    MetaDataDefaultConstructorAdapter(String baseInternalName, String metaDataInternalName, boolean metaDataBeanAware, MethodVisitor visitor) {
        super(ASM9, visitor);

        baseInternalName_ = baseInternalName;
        metaDataInternalName_ = metaDataInternalName;
        metaDataBeanAware_ = metaDataBeanAware;
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, final boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);

        if (INVOKESPECIAL == opcode &&
            "<init>".equals(name) &&
            "()V".equals(desc)) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, metaDataInternalName_);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, metaDataInternalName_, "<init>", "()V", false);
            mv.visitFieldInsn(PUTFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");

            if (metaDataBeanAware_) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, "setMetaDataBean", "(Ljava/lang/Object;)V", false);
            }
        }
    }
}
