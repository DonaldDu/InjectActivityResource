package asm.com.dhy.inject;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class InjectActivityResourceDemoDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        RecordComponentVisitor recordComponentVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V1_7, ACC_PUBLIC | ACC_SUPER, SELF_PATH_NAME, null, SELF_SUPER_CLASS_PATH_NAME, null);

        classWriter.visitSource("InjectActivityResourceDemo.java", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "injectActivityResource", "Landroid/os/Handler;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE, "msg", "Landroid/os/Message;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_SUPER_CLASS_PATH_NAME, "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", L_SELF_PATH_NAME, null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {

        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "onCreate", "(Landroid/os/Bundle;)V", null, null);
            methodVisitor.visitAnnotableParameterCount(1, false);
            {
                annotationVisitor0 = methodVisitor.visitParameterAnnotation(0, "Landroidx/annotation/Nullable;", false);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_PATH_NAME, "initInjectActivityResource", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, SELF_SUPER_CLASS_PATH_NAME, "onCreate", "(Landroid/os/Bundle;)V", false);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", L_SELF_PATH_NAME, null, label0, label3, 0);
            methodVisitor.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, label0, label3, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
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
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
