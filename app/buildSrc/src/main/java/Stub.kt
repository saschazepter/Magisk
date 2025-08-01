import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.security.SecureRandom
import java.util.Random
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.asKotlinRandom

private val kRANDOM get() = RANDOM.asKotlinRandom()

private val c1 = mutableListOf<String>()
private val c2 = mutableListOf<String>()
private val c3 = mutableListOf<String>()

fun initRandom(dict: File) {
    RANDOM = if (RAND_SEED != 0) Random(RAND_SEED.toLong()) else SecureRandom()
    c1.clear()
    c2.clear()
    c3.clear()
    for (a in chain('a'..'z', 'A'..'Z')) {
        if (a != 'a' && a != 'A') {
            c1.add("$a")
        }
        for (b in chain('a'..'z', 'A'..'Z', '0'..'9')) {
            c2.add("$a$b")
            for (c in chain('a'..'z', 'A'..'Z', '0'..'9')) {
                c3.add("$a$b$c")
            }
        }
    }
    c1.shuffle(RANDOM)
    c2.shuffle(RANDOM)
    c3.shuffle(RANDOM)
    PrintStream(dict).use {
        for (c in chain(c1, c2, c3)) {
            it.println(c)
        }
    }
}

private fun <T> chain(vararg iters: Iterable<T>) = sequence {
    iters.forEach { it.forEach { v -> yield(v) } }
}

private fun PrintStream.byteField(name: String, bytes: ByteArray) {
    println("public static byte[] $name() {")
    print("byte[] buf = {")
    print(bytes.joinToString(",") { it.toString() })
    println("};")
    println("return buf;")
    println("}")
}

@CacheableTask
private abstract class ManifestUpdater: DefaultTask() {
    @get:Input
    abstract val applicationId: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedManifest: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val factoryClassDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val appClassDir: DirectoryProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @TaskAction
    fun taskAction() {
        fun String.ind(level: Int) = replaceIndentByMargin("    ".repeat(level))

        val cmpList = mutableListOf<String>()

        cmpList.add("""
            |<provider
            |    android:name="x.COMPONENT_PLACEHOLDER_0"
            |    android:authorities="${'$'}{applicationId}.provider"
            |    android:directBootAware="true"
            |    android:exported="false"
            |    android:grantUriPermissions="true" />""".ind(2)
        )

        cmpList.add("""
            |<receiver
            |    android:name="x.COMPONENT_PLACEHOLDER_1"
            |    android:exported="false">
            |    <intent-filter>
            |        <action android:name="android.intent.action.LOCALE_CHANGED" />
            |        <action android:name="android.intent.action.UID_REMOVED" />
            |        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            |    </intent-filter>
            |    <intent-filter>
            |        <action android:name="android.intent.action.PACKAGE_REPLACED" />
            |        <action android:name="android.intent.action.PACKAGE_FULLY_REMOVED" />
            |
            |        <data android:scheme="package" />
            |    </intent-filter>
            |</receiver>""".ind(2)
        )

        cmpList.add("""
            |<activity
            |    android:name="x.COMPONENT_PLACEHOLDER_2"
            |    android:exported="true">
            |    <intent-filter>
            |        <action android:name="android.intent.action.MAIN" />
            |        <category android:name="android.intent.category.LAUNCHER" />
            |    </intent-filter>
            |</activity>""".ind(2)
        )

        cmpList.add("""
            |<activity
            |    android:name="x.COMPONENT_PLACEHOLDER_3"
            |    android:directBootAware="true"
            |    android:exported="false"
            |    android:taskAffinity="">
            |    <intent-filter>
            |        <action android:name="android.intent.action.VIEW"/>
            |        <category android:name="android.intent.category.DEFAULT"/>
            |    </intent-filter>
            |</activity>""".ind(2)
        )

        cmpList.add("""
            |<service
            |    android:name="x.COMPONENT_PLACEHOLDER_4"
            |    android:exported="false"
            |    android:foregroundServiceType="dataSync" />""".ind(2)
        )

        cmpList.add("""
            |<service
            |    android:name="x.COMPONENT_PLACEHOLDER_5"
            |    android:exported="false"
            |    android:permission="android.permission.BIND_JOB_SERVICE" />""".ind(2)
        )

        // Shuffle the order of the components
        cmpList.shuffle(RANDOM)
        val (factoryPkg, factoryClass) = factoryClassDir.asFileTree.firstNotNullOf {
            it.parentFile!!.name to it.name.removeSuffix(".java")
        }
        val (appPkg, appClass) = appClassDir.asFileTree.firstNotNullOf {
            it.parentFile!!.name to it.name.removeSuffix(".java")
        }
        val components = cmpList.joinToString("\n\n")
            .replace("\${applicationId}", applicationId.get())
        val manifest = mergedManifest.asFile.get().readText().replace(Regex(".*\\<application"), """
            |<application
            |    android:appComponentFactory="$factoryPkg.$factoryClass"
            |    android:name="$appPkg.$appClass"""".ind(1)
        ).replace(Regex(".*\\<\\/application"), "$components\n    </application")
        outputManifest.get().asFile.writeText(manifest)
    }
}


private fun genStubClasses(factoryOutDir: File, appOutDir: File) {
    val classNameGenerator = sequence {
        fun notJavaKeyword(name: String) = when (name) {
            "do", "if", "for", "int", "new", "try" -> false
            else -> true
        }

        fun List<String>.process() = asSequence()
            .filter(::notJavaKeyword)
            // Distinct by lower case to support case insensitive file systems
            .distinctBy { it.lowercase() }

        val names = mutableListOf<String>()
        names.addAll(c1)
        names.addAll(c2.process().take(30))
        names.addAll(c3.process().take(30))
        names.shuffle(RANDOM)

        while (true) {
            val cls = StringBuilder()
            cls.append(names.random(kRANDOM))
            cls.append('.')
            cls.append(names.random(kRANDOM))
            // Old Android does not support capitalized package names
            // Check Android 7.0.0 PackageParser#buildClassName
            yield(cls.toString().replaceFirstChar { it.lowercase() })
        }
    }.distinct().iterator()

    fun genClass(type: String, outDir: File) {
        val clzName = classNameGenerator.next()
        val (pkg, name) = clzName.split('.')
        val pkgDir = File(outDir, pkg)
        pkgDir.mkdirs()
        PrintStream(File(pkgDir, "$name.java")).use {
            it.println("package $pkg;")
            it.println("public class $name extends com.topjohnwu.magisk.$type {}")
        }
    }

    genClass("DelegateComponentFactory", factoryOutDir)
    genClass("StubApplication", appOutDir)
}

private fun genEncryptedResources(res: ByteArray, outDir: File) {
    val mainPkgDir = File(outDir, "com/topjohnwu/magisk")
    mainPkgDir.mkdirs()

    // Generate iv and key
    val iv = ByteArray(16)
    val key = ByteArray(32)
    RANDOM.nextBytes(iv)
    RANDOM.nextBytes(key)

    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
    val bos = ByteArrayOutputStream()

    ByteArrayInputStream(res).use {
        CipherOutputStream(bos, cipher).use { os ->
            it.transferTo(os)
        }
    }

    PrintStream(File(mainPkgDir, "Bytes.java")).use {
        it.println("package com.topjohnwu.magisk;")
        it.println("public final class Bytes {")

        it.byteField("key", key)
        it.byteField("iv", iv)
        it.byteField("res", bos.toByteArray())

        it.println("}")
    }
}

fun Project.setupStubApk() {
    setupAppCommon()

    androidComponents.onVariants { variant ->
        val variantName = variant.name
        val variantCapped = variantName.replaceFirstChar { it.uppercase() }
        val manifestUpdater =
            project.tasks.register("${variantName}ManifestProducer", ManifestUpdater::class.java) {
                dependsOn("generate${variantCapped}ObfuscatedClass")
                applicationId = variant.applicationId
                appClassDir.set(layout.buildDirectory.dir("generated/source/app/$variantName"))
                factoryClassDir.set(layout.buildDirectory.dir("generated/source/factory/$variantName"))
            }
        variant.artifacts.use(manifestUpdater)
            .wiredWithFiles(
                ManifestUpdater::mergedManifest,
                ManifestUpdater::outputManifest)
            .toTransform(SingleArtifact.MERGED_MANIFEST)
    }

    androidApp.applicationVariants.all {
        val variantCapped = name.replaceFirstChar { it.uppercase() }
        val variantLowered = name.lowercase()
        val outFactoryClassDir = layout.buildDirectory.file("generated/source/factory/${variantLowered}").get().asFile
        val outAppClassDir = layout.buildDirectory.file("generated/source/app/${variantLowered}").get().asFile
        val outResDir = layout.buildDirectory.dir("generated/source/res/${variantLowered}").get().asFile
        val aapt = File(androidApp.sdkDirectory, "build-tools/${androidApp.buildToolsVersion}/aapt2")
        val apk = layout.buildDirectory.file("intermediates/linked_resources_binary_format/" +
                "${variantLowered}/process${variantCapped}Resources/linked-resources-binary-format-${variantLowered}.ap_").get().asFile

        val genManifestTask = tasks.register("generate${variantCapped}ObfuscatedClass") {
            inputs.property("seed", RAND_SEED)
            outputs.dirs(outFactoryClassDir, outAppClassDir)
            doLast {
                outFactoryClassDir.mkdirs()
                outAppClassDir.mkdirs()
                genStubClasses(outFactoryClassDir, outAppClassDir)
            }
        }
        registerJavaGeneratingTask(genManifestTask, outFactoryClassDir, outAppClassDir)

        val processResourcesTask = tasks.named("process${variantCapped}Resources") {
            outputs.dir(outResDir)
            doLast {
                val apkTmp = File("${apk}.tmp")
                providers.exec {
                    commandLine(aapt, "optimize", "-o", apkTmp, "--collapse-resource-names", apk)
                }.result.get()

                val bos = ByteArrayOutputStream()
                ZipFile(apkTmp).use { src ->
                    ZipOutputStream(apk.outputStream()).use {
                        it.setLevel(Deflater.BEST_COMPRESSION)
                        it.putNextEntry(ZipEntry("AndroidManifest.xml"))
                        src.getInputStream(src.getEntry("AndroidManifest.xml")).transferTo(it)
                        it.closeEntry()
                    }
                    DeflaterOutputStream(bos, Deflater(Deflater.BEST_COMPRESSION)).use {
                        src.getInputStream(src.getEntry("resources.arsc")).transferTo(it)
                    }
                }
                apkTmp.delete()
                genEncryptedResources(bos.toByteArray(), outResDir)
            }
        }

        registerJavaGeneratingTask(processResourcesTask, outResDir)
    }
    // Override optimizeReleaseResources task
    val apk = layout.buildDirectory.file("intermediates/linked_resources_binary_format/" +
            "release/processReleaseResources/linked-resources-binary-format-release.ap_").get().asFile
    val optRes = layout.buildDirectory.file("intermediates/optimized_processed_res/" +
            "release/optimizeReleaseResources/resources-release-optimize.ap_").get().asFile
    afterEvaluate {
        tasks.named("optimizeReleaseResources") {
            doLast { apk.copyTo(optRes, true) }
        }
    }
    tasks.named<Delete>("clean") {
        delete.addAll(listOf("src/debug/AndroidManifest.xml", "src/release/AndroidManifest.xml"))
    }
}
