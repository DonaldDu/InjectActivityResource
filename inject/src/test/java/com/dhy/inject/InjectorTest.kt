package com.dhy.inject

import androidx.appcompat.app.AppCompatActivity
import com.dhy.transform.*
import org.junit.Before
import org.junit.Test
import java.io.File

class InjectorTest {
    @Test
    fun inject() {
        //仔细想了下，还是注入Qigsaw原生代码，这样运行效率高点，也可以选择注入方式。
        //Qigsaw原生方式用途固定了，Handler方式可作它用（但好像也没什么其它用途了）。
        //true: SplitInstallHelper.loadResources(this, super.getResources());
        //false: Handler-> injectActivityResource.handleMessage(msg);
        SPLIT_INSTALL_HELPER = true
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

    @Before
    fun setup() {
        GRADLE_USER_HOME_DIR = BuildConfig.GRADLE_USER_HOME_DIR
        MAVEN_LOCAL_DIR = BuildConfig.MAVEN_LOCAL_URL.toMavenLocalDir()
    }
}