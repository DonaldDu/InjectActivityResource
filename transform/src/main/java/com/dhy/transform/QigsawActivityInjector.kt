package com.dhy.transform

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.IOException
import java.io.InputStream

object QigsawActivityInjector {
    private const val CLASS_WOVEN = "com/google/android/play/core/splitinstall/SplitInstallHelper"

    private const val METHOD_WOVEN = "loadResources"

    @Throws(IOException::class)
    fun weave(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        inputStream.close()
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val cv = ActivityClassVisitor(cw)
        cr.accept(cv, ASM5)
        return cw.toByteArray()
    }

    private class ActivityClassVisitor(classWriter: ClassWriter) : ClassVisitor(ASM5, classWriter), Opcodes {
        var needInsert = true
        lateinit var superName: String
        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
            super.visit(version, access, name, signature, superName, interfaces)
            this.superName = superName
        }

        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
            if (name == "getResources") {
                needInsert = false
                return ChangeMethodVisitor_getResources(cv.visitMethod(access, name, desc, signature, exceptions))
            }
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

        override fun visitEnd() {
            if (needInsert) addMethod_getResources()
            super.visitEnd()
        }

        fun addMethod_getResources() {
            val mv = cv.visitMethod(ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false)
            mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, METHOD_WOVEN, "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false)
            mv.visitInsn(ARETURN)
            mv.visitMaxs(2, 1)
            mv.visitEnd()
        }

        inner class ChangeMethodVisitor_getResources(mv: MethodVisitor?) : MethodVisitor(ASM5, mv) {
            override fun visitCode() {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ALOAD, 0)
                mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false)
                mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, METHOD_WOVEN, "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false)
                super.visitCode()
            }
        }
    }
}