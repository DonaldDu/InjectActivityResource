package com.dhy.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

public class ActivityInjector {
    public static byte[] weave(InputStream inputStream, String SELF_PATH_NAME, String L_SELF_PATH_NAME, String SELF_SUPER_CLASS_PATH_NAME) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        inputStream.close();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ActivityClassVisitor(cw, SELF_PATH_NAME, L_SELF_PATH_NAME, SELF_SUPER_CLASS_PATH_NAME);
        cr.accept(cv, Opcodes.ASM5);
        return cw.toByteArray();
    }

    private static class ActivityClassVisitor extends ClassVisitor implements Opcodes {
        private final String SELF_PATH_NAME;
        private final String L_SELF_PATH_NAME;
        private final String SELF_SUPER_CLASS_PATH_NAME;
        boolean needInsert = true;
        private final ClassWriter classWriter;

        public ActivityClassVisitor(ClassWriter cv, String SELF_PATH_NAME, String L_SELF_PATH_NAME, String SELF_SUPER_CLASS_PATH_NAME) {
            super(Opcodes.ASM5, cv);
            classWriter = cv;
            this.SELF_PATH_NAME = SELF_PATH_NAME;
            this.L_SELF_PATH_NAME = L_SELF_PATH_NAME;
            this.SELF_SUPER_CLASS_PATH_NAME = SELF_SUPER_CLASS_PATH_NAME;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("getResources")) {
                needInsert = false;
                return new ChangeMethodVisitor_getResources(cv.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("onCreate") && desc.equals("(Landroid/os/Bundle;)V")) {
                return new ChangeMethodVisitor_onCreate(cv.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            addField();
            addMethod_initInjectActivityResource();
            if (needInsert) addMethod_getResources();
            super.visitEnd();
        }

        private void addField() {
            FieldVisitor fieldVisitor;

            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "injectActivityResource", "Landroid/os/Handler;", null, null);
            fieldVisitor.visitEnd();

            fieldVisitor = classWriter.visitField(ACC_PRIVATE, "msg", "Landroid/os/Message;", null, null);
            fieldVisitor.visitEnd();
        }

        void addMethod_getResources() {
            MethodVisitor methodVisitor;
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitFieldInsn(GETSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
            Label label1 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label1);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_SUPER_CLASS_PATH_NAME, "getResources", "()Landroid/content/res/Resources;", false);
            methodVisitor.visitVarInsn(ASTORE, 1);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
            Label label4 = new Label();
            methodVisitor.visitJumpInsn(IFNONNULL, label4);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitTypeInsn(NEW, "android/os/Message");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "android/os/Message", "<init>", "()V", false);
            methodVisitor.visitFieldInsn(PUTFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
            methodVisitor.visitLabel(label4);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"android/content/res/Resources"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "android/os/Message", "obj", "Ljava/lang/Object;");
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitFieldInsn(GETSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "android/os/Handler", "handleMessage", "(Landroid/os/Message;)V", false);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_SUPER_CLASS_PATH_NAME, "getResources", "()Landroid/content/res/Resources;", false);
            methodVisitor.visitInsn(ARETURN);
            Label label7 = new Label();
            methodVisitor.visitLabel(label7);
            methodVisitor.visitLocalVariable("resources", "Landroid/content/res/Resources;", null, label3, label1, 1);
            methodVisitor.visitLocalVariable("this", L_SELF_PATH_NAME, null, label0, label7, 0);
            methodVisitor.visitMaxs(3, 2);
            methodVisitor.visitEnd();
        }

        void addMethod_initInjectActivityResource() {
            MethodVisitor methodVisitor;
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "initInjectActivityResource", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            Label label1 = new Label();
            Label label2 = new Label();
            methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitFieldInsn(GETSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
            Label label4 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label4);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitLabel(label4);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, SELF_PATH_NAME, "getApplicationContext", "()Landroid/content/Context;", false);
            methodVisitor.visitVarInsn(ASTORE, 1);
            methodVisitor.visitLabel(label0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitLdcInsn("injectActivityResource");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
            methodVisitor.visitVarInsn(ASTORE, 2);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "android/os/Handler");
            methodVisitor.visitFieldInsn(PUTSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
            methodVisitor.visitLabel(label1);
            Label label7 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label7);
            methodVisitor.visitLabel(label2);
            methodVisitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{SELF_PATH_NAME, "android/content/Context"}, 1, new Object[]{"java/lang/Exception"});
            methodVisitor.visitVarInsn(ASTORE, 2);
            Label label8 = new Label();
            methodVisitor.visitLabel(label8);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
            methodVisitor.visitLabel(label7);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(RETURN);
            Label label9 = new Label();
            methodVisitor.visitLabel(label9);
            methodVisitor.visitLocalVariable("injectActivityResourceField", "Ljava/lang/reflect/Field;", null, label5, label1, 2);
            methodVisitor.visitLocalVariable("e", "Ljava/lang/Exception;", null, label8, label7, 2);
            methodVisitor.visitLocalVariable("this", L_SELF_PATH_NAME, null, label3, label9, 0);
            methodVisitor.visitLocalVariable("application", "Landroid/content/Context;", null, label0, label9, 1);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }

        class ChangeMethodVisitor_getResources extends MethodVisitor {
            ChangeMethodVisitor_getResources(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitCode() {
                invokeInjectActivityResource(mv);
                super.visitCode();
            }

            void invokeInjectActivityResource(MethodVisitor methodVisitor) {
                methodVisitor.visitFieldInsn(GETSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
                Label label1 = new Label();
                methodVisitor.visitJumpInsn(IFNULL, label1);

                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_SUPER_CLASS_PATH_NAME, "getResources", "()Landroid/content/res/Resources;", false);
                methodVisitor.visitVarInsn(ASTORE, 1);

                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
                Label label4 = new Label();
                methodVisitor.visitJumpInsn(IFNONNULL, label4);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitTypeInsn(NEW, "android/os/Message");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "android/os/Message", "<init>", "()V", false);
                methodVisitor.visitFieldInsn(PUTFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
                methodVisitor.visitLabel(label4);
                methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"android/content/res/Resources"}, 0, null);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
                methodVisitor.visitVarInsn(ALOAD, 1);
                methodVisitor.visitFieldInsn(PUTFIELD, "android/os/Message", "obj", "Ljava/lang/Object;");

                methodVisitor.visitFieldInsn(GETSTATIC, SELF_PATH_NAME, "injectActivityResource", "Landroid/os/Handler;");
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, SELF_PATH_NAME, "msg", "Landroid/os/Message;");
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "android/os/Handler", "handleMessage", "(Landroid/os/Message;)V", false);

                methodVisitor.visitVarInsn(ALOAD, 1);
                methodVisitor.visitInsn(ARETURN);
                methodVisitor.visitLabel(label1);
            }
        }

        class ChangeMethodVisitor_onCreate extends MethodVisitor {
            ChangeMethodVisitor_onCreate(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitCode() {
                invokeInjectActivityResource(mv);
                super.visitCode();
            }

            void invokeInjectActivityResource(MethodVisitor methodVisitor) {
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_PATH_NAME, "initInjectActivityResource", "()V", false);
            }
        }
    }
}
