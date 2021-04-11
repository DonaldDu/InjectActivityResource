package com.dhy.transform


import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

var GRADLE_USER_HOME_DIR = ""// "C:/Users/Donald/.gradle"
var MAVEN_LOCAL_DIR = ""// "C:/Users/Donald/.m2/repository/"

/**
 * MAVEN_LOCAL_URL toMavenLocalDir  "file:/C:/Users/Donald/.m2/repository/" -> "C:/Users/Donald/.m2/repository/"
 * */
fun String.toMavenLocalDir(): String {
    return substringAfter("file:/")
}

fun String.adaptCode(self: Class<*>, target: Class<*>, placeHolder: Boolean): String {
    var code = this
    if (placeHolder) {
        //super call
        code = code.replace(self.superclass.pathName().quote(), "SELF_SUPER_CLASS_PATH_NAME")
        //self cal
        code = code.replace(self.pathName().quote(), "SELF_PATH_NAME")
        code = code.replace(self.lPathName().quote(), "L_SELF_PATH_NAME")
    } else {
        //super call
        code = code.replace(self.superclass.pathName().quote(), target.superclass.pathName().quote())
        //self cal
        code = code.replace(self.pathName().quote(), target.pathName().quote())
        code = code.replace(self.lPathName().quote(), target.lPathName().quote())
    }
    return code
}

fun activityInjectorWeave(inputStream: InputStream, self: Class<*>): ByteArray {
    return ActivityInjector.weave(inputStream, self.pathName(), self.lPathName(), self.superclass.pathName())
}

fun Class<*>.pathName(): String {
    return name.path()
}

fun String.path(): String {
    return replace('.', '/')
}

fun Class<*>.lPathName(): String {
    return "L" + name.replace('.', '/') + ";"
}

fun String.quote(): String {
    return "\"$this\""
}

fun inject(tasks: Collection<InjectDep>): List<InjectedDep> {
    return tasks.mapNotNull {
        inject(it.dep, it.classes)
    }
}

fun inject(dep: String, classes: Collection<Class<*>>): InjectedDep? {
    val defFile = findDepFile(dep)
    if (defFile != null) {
        var newDepFile: File? = null
        if (defFile.name.endsWith(".aar")) {
            val aarEntryName = "classes.jar"
            val jar = defFile.unzip(aarEntryName)
            if (jar != null) {
                val newJar = injectToNewFile(jar, classes)
                if (newJar != null) {
                    val aarReplacement: MutableMap<String, EntryReplacement> = mutableMapOf()
                    aarReplacement[aarEntryName] = EntryReplacement(newJar)
                    newDepFile = defFile.replaceEntry(aarReplacement)
                    aarReplacement.values.forEach { it.file.delete() }
                }
            }
        } else {//jar
            newDepFile = injectToNewFile(defFile, classes)
        }
        if (newDepFile != null) {
            println(defFile.parentFile)
            val injectedDepFile = File(defFile.parentFile, "injected-" + defFile.name)
            if (injectedDepFile.exists()) injectedDepFile.delete()
            newDepFile.renameTo(injectedDepFile)

            val pom = createInjectedPom(dep)
            if (pom != null) {
                val injectedPOM = File(defFile.parentFile, pom.name)
                if (injectedPOM.exists()) injectedPOM.delete()
                pom.renameTo(injectedPOM)
                return InjectedDep(injectedDepFile, injectedPOM)
            }
        }
    }
    return null
}

fun String.trimInjectedPom(): String {
    return substringAfter("injected-").substringBeforeLast(".pom")
}

fun File.deleteAll() {
    eachFile { if (!it.isDirectory) it.delete() }
    eachFile { it.delete() }
}

fun File.moveTo(folder: File) {
    val destination = File(folder, name)
    if (destination.exists()) destination.delete()
    if (!folder.exists()) folder.mkdirs()
    val ok = renameTo(destination)
    if (!ok) println("renameTo $destination $ok")
}

class InjectedDep(val depFile: File, val pom: File)
class InjectDep(val dep: String, val classes: Collection<Class<*>>)

fun inject(jar: File, classes: List<Class<*>>): Map<String, EntryReplacement>? {
    val zip = ZipFile(jar)
    val names = classes.map { it.name.replace('.', '/') + ".class" }
        .filter { zip.getEntry(it).comment == null }
    if (names.isEmpty()) {
        zip.close()
        return null
    }
    val replacement: MutableMap<String, EntryReplacement> = mutableMapOf()

    for (e in zip.entries()) {
        if (names.contains(e.name) && e.comment == null) {
            val clazz = classes[names.indexOf(e.name)]
            val bytes = activityInjectorWeave(zip.getInputStream(e), clazz)

            val temp = File.createTempFile("temp", ".class")
            temp.writeBytes(bytes)
            replacement[e.name] = EntryReplacement(temp) { it.comment = "injected" }
        }
    }
    zip.close()

    return replacement
}

fun findDepPOM(dep: String): File? {
    return getGradleDepFolder(dep).findDepPOM(dep) ?: getMavenDepFolder(dep).findDepPOM(dep)
}

fun findDepFile(dep: String): File? {
    return getGradleDepFolder(dep).findDepFile(dep) ?: getMavenDepFolder(dep).findDepFile(dep)
}

private fun getGradleDepFolder(dep: String): File {
    val f21 = File(GRADLE_USER_HOME_DIR, "caches/modules-2/files-2.1")
    return File(f21, dep.replace(':', '/'))
}

private fun getMavenDepFolder(dep: String): File {
    val gav = dep.split(':')
    return File(MAVEN_LOCAL_DIR, "${gav[0].path()}/${gav[1].path()}/${gav[2]}")
}

private fun File.findDepPOM(dep: String): File? {
    val gav = dep.split(":")
    val pom = "${gav[1]}-${gav[2]}.pom"
    var find: File? = null
    eachFile {
        if (it.name == pom) {
            find = it
            return@eachFile
        }
    }
    return find
}

private fun File.findDepFile(dep: String): File? {
    val depFolder = this
    val gav = dep.split(":")
    val aar = "${gav[1]}-${gav[2]}.aar"
    val jar = "${gav[1]}-${gav[2]}.jar"
    var find: File? = null
    depFolder.eachFile {
        if (it.name == aar || it.name == jar) {
            find = it
            return@eachFile
        }
    }
    return find
}

fun createInjectedPom(dep: String): File? {
    val pom = findDepPOM(dep)
    if (pom != null) {
        val gav = dep.split(':')
        val artifact = "<artifactId>${gav[1]}</artifactId>"
        val injectedArtifact = "<artifactId>${gav[1]}.qb</artifactId>"
        val bytes = pom.readText().replace(artifact, injectedArtifact).toByteArray()
        val injectedPom = File(pom.parentFile, "injected-" + pom.name)
        injectedPom.writeBytes(bytes)
        return injectedPom
    }
    return null
}

fun injectToNewFile(jar: File, classes: Collection<Class<*>>): File? {
    val jarReplacement = inject(jar, classes.toList())
    if (jarReplacement != null) {
        val newJar = jar.replaceEntry(jarReplacement)
        if (newJar != null) {
            jarReplacement.values.forEach { it.file.delete() }
            return newJar
        }
    }
    return null
}

fun File.eachFile(each: (File) -> Unit) {
    if (isDirectory) {
        each(this)
        listFiles()?.forEach {
            it.eachFile(each)
        }
    } else {
        each(this)
    }
}

class EntryReplacement(val file: File, val extra: ((newZipEntry: ZipEntry) -> Unit)? = null)

fun File.replaceEntry(replacement: Map<String, EntryReplacement>): File? {
    if (replacement.isEmpty()) return null
    val zip = ZipFile(this)
    val buffer = ByteArray(1024 * 1024)
    val newZip = File.createTempFile("new", ".zip")
    val outputStream = ZipOutputStream(newZip.outputStream())
    for (e in zip.entries()) {
        if (replacement.containsKey(e.name)) {
            val er = replacement[e.name]!!
            val newZipEntry = ZipEntry(e.name)
            er.extra?.invoke(newZipEntry)
            outputStream.putNextEntry(newZipEntry)
            outputStream.write(buffer, er.file.inputStream())
        } else {
            outputStream.putNextEntry(ZipEntry(e.name))
            outputStream.write(buffer, zip.getInputStream(e))
        }
        outputStream.closeEntry()
    }
    outputStream.flush()
    outputStream.close()
    zip.close()
    return newZip
}

fun OutputStream.write(buffer: ByteArray, inputStream: InputStream, closeInput: Boolean = true) {
    if (inputStream.available() > buffer.size) {
        while (true) {
            val size = inputStream.read(buffer)
            if (size > 0) write(buffer, 0, size)
            else break
        }
    } else write(inputStream.readBytes())
    if (closeInput) inputStream.close()
}

fun File.unzip(name: String): File? {
    val zipFile = ZipFile(this)
    val e = zipFile.getEntry(name)
    var temp: File? = null
    if (e != null) {
        temp = File.createTempFile("temp", name)
        val inputStream = zipFile.getInputStream(e)
        val outputStream = FileOutputStream(temp)
        outputStream.write(inputStream.readBytes())
        inputStream.close()
        outputStream.close()
    }
    zipFile.close()
    return temp
}

fun Class<*>.classFileInputStream(): InputStream? {
    val path = name.replace('.', '/') + ".class"
    return classLoader?.getResourceAsStream(path)
}