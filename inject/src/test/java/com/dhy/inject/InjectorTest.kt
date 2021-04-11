package com.dhy.inject

import androidx.appcompat.app.AppCompatActivity
import com.dhy.transform.*
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.util.ASMifierUtil
import java.io.File
import java.io.PrintWriter

class InjectorTest {
    @Before
    fun setup() {
        GRADLE_USER_HOME_DIR = BuildConfig.GRADLE_USER_HOME_DIR
    }

    @Test
    fun inject() {
        val injected = inject(
            listOf(
                InjectDep("androidx.appcompat:appcompat:1.2.0", listOf(AppCompatActivity::class.java)),
//                InjectDep("androidx.appcompat:appcompat:1.1.0", listOf(AppCompatActivity::class.java))
            )
        )
        val folder = File(projectRootDir, "injected")
        folder.deleteAll()
        injected.forEach {
            val single = File(folder, it.pom.name.trimInjectedPom())
            it.depFile.moveTo(single)
            it.pom.moveTo(single)
        }
    }

    @Test
    fun showASMifier() {
        val file = File(projectRootDir, "InjectActivityResourceDump.java")
        val pw = PrintWriter(file)
        ASMifierUtil.main(arrayOf(InjectActivityResourceDemo::class.java.name), pw)
        pw.flush()
        pw.close()

        val target = AppCompatActivity::class.java
        val self = InjectActivityResourceDemo::class.java
        var code = file.readText()
        code = code.adaptCode(self, target, true)
        code = code.split('\n')
            .filter { !it.contains(".visitLineNumber(") }
            .joinToString("\n")
        file.writeBytes(code.toByteArray())
        println(file)
    }

    @Test
    fun injectDemo() {
        val self = EmptyActivity::class.java

        val injected = activityInjectorWeave(self.classFileInputStream()!!, self)
        val file = File(projectRootDir, self.name + ".class")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.writeBytes(injected)
        println(file)
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}