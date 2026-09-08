package utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipFile

/**
 * "Header stubs" are the Android/JVM `actual` declarations of the `com.google.*` compatibility layer. They only exist so
 * that common code compiles against the Firebase Android SDK API; on Android and the JVM the real SDK (and Play Services'
 * `Task`) provides these classes, so the stubs must not be shipped.
 *
 * They are compiled like any other source and then deleted from the compilation output, so the AAR/JAR, project
 * consumers, D8 and every test runtime classpath bind to the real classes instead. Before deletion, every public member
 * of a stub is checked against the real class in the reference jars (the compile classpath) so the stubs cannot drift,
 * and a BCV-style dump of the stubs is written to `build/header-stubs/<compilation>.api` for the source-compatibility report.
 *
 * Rules for code in this module: the stubs' companion members compile to calls through the `Companion` object, which the
 * real classes do not have, so this module's own code must reach static SDK members through the SDK's Kotlin extensions
 * (real static facade methods) or a Java helper; consumers are unaffected because they compile against the real classes.
 */
fun Project.stripHeaderStubs(
    packageDirs: List<String>,
    androidReferenceJars: FileCollection,
    jvmReferenceJars: FileCollection,
) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        if (!name.startsWith("compile") || name.contains("Test")) return@configureEach
        val android = name.contains("Android")
        if (!android && name != "compileKotlinJvm") return@configureEach
        val references = if (android) androidReferenceJars else jvmReferenceJars
        inputs.files(references).withPropertyName("headerStubReferenceJars").optional()
        // Not declared as a task output: the Kotlin compile task pre-creates declared outputs as directories.
        val dump = project.layout.buildDirectory.file("header-stubs/$name.api")
        doLast {
            val stubDirs = packageDirs.map { destinationDirectory.dir(it).get().asFile }.filter { it.exists() }
            val stubs = stubDirs.flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "class" }.toList() }
            val classes = stubs.map { readMembers(it.readBytes()) }
            verifyHeaderStubs(classes, references.files.filter { it.isFile })
            dump.get().asFile.also { it.parentFile.mkdirs() }.writeText(dumpStubs(classes))
            stubDirs.forEach { it.deleteRecursively() }
            logger.info("Removed header stubs ${packageDirs} from ${destinationDirectory.get()}")
        }
    }
    tasks.withType(Jar::class.java).configureEach {
        packageDirs.forEach { exclude("$it/**") }
    }
}

private class Member(val name: String, val descriptor: String, val access: Int) {
    val isStatic get() = access and Opcodes.ACC_STATIC != 0
    val signature get() = "$name$descriptor"
}

private class ClassMembers : ClassVisitor(Opcodes.ASM9) {
    lateinit var name: String
    var access = 0
    var superName: String? = null
    val methods = mutableListOf<Member>()
    val fields = mutableListOf<Member>()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        this.name = name
        this.access = access
        this.superName = superName
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) != 0 && access and Opcodes.ACC_SYNTHETIC == 0 && name != "<clinit>") {
            methods += Member(name, descriptor, access)
        }
        return null
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        if (access and Opcodes.ACC_PUBLIC != 0 && access and Opcodes.ACC_SYNTHETIC == 0) fields += Member(name, descriptor, access)
        return null
    }

    /** Kotlin-only classes and members that have no counterpart on the real (Java) class and are never reached at runtime. */
    val isKotlinOnly get() = name.endsWith("\$Companion") || name.contains("\$WhenMappings") || name.contains("\$EntriesMappings")
}

private fun readMembers(bytes: ByteArray): ClassMembers = ClassMembers().also { ClassReader(bytes).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }

private val kotlinOnlyMethods = setOf("getEntries")

private fun verifyHeaderStubs(stubs: List<ClassMembers>, referenceJars: List<File>) {
    val problems = mutableListOf<String>()
    val zips = referenceJars.map { ZipFile(it) }
    try {
        for (stub in stubs) {
            if (stub.isKotlinOnly) continue
            val entryName = "${stub.name}.class"
            val realBytes = zips.firstNotNullOfOrNull { zip -> zip.getEntry(entryName)?.let { zip.getInputStream(it).use { s -> s.readBytes() } } }
            if (realBytes == null) {
                problems += "${stub.name}: no such class on the compile classpath"
                continue
            }
            val real = readMembers(realBytes)
            for (member in stub.methods) {
                if (member.name in kotlinOnlyMethods) continue
                val match = real.methods.firstOrNull { it.signature == member.signature }
                when {
                    match == null -> problems += "${stub.name}.${member.signature} does not exist on the real class"
                    match.isStatic != member.isStatic -> problems += "${stub.name}.${member.signature} is ${if (match.isStatic) "static" else "not static"} on the real class"
                }
            }
            for (field in stub.fields) {
                if (field.name == "Companion") continue
                val match = real.fields.firstOrNull { it.signature == field.signature }
                if (match == null || match.isStatic != field.isStatic) problems += "${stub.name}.${field.name} does not exist on the real class"
            }
        }
    } finally {
        zips.forEach { it.close() }
    }
    if (problems.isNotEmpty()) {
        throw GradleException("Header stubs do not match the real Firebase Android SDK classes:\n" + problems.joinToString("\n") { "  $it" })
    }
}

/**
 * Writes the stubs in the binary-compatibility-validator dump format so the source-compatibility report can read them.
 * Companion objects are included (the Android SDK's Kotlin classes have them too); the mapping classes are not.
 */
private fun dumpStubs(stubs: List<ClassMembers>): String = buildString {
    for (stub in stubs.sortedBy { it.name }) {
        if (stub.name.contains("\$WhenMappings") || stub.name.contains("\$EntriesMappings")) continue
        val modifiers = buildList {
            if (stub.access and Opcodes.ACC_ABSTRACT != 0 && stub.access and Opcodes.ACC_INTERFACE == 0) add("abstract")
            if (stub.access and Opcodes.ACC_FINAL != 0) add("final")
        }.joinToString(" ") { "$it " }
        val kind = if (stub.access and Opcodes.ACC_INTERFACE != 0) "abstract interface" else "class"
        val superClause = stub.superName?.takeIf { it != "java/lang/Object" }?.let { " : $it" }.orEmpty()
        appendLine("public $modifiers$kind ${stub.name}$superClause {")
        for (field in stub.fields.sortedBy { it.name }) {
            if (field.name == "Companion") continue
            val fieldModifiers = buildList {
                if (field.isStatic) add("static")
                if (field.access and Opcodes.ACC_FINAL != 0) add("final")
                if (field.access and Opcodes.ACC_ENUM != 0) add("enum")
            }.joinToString(" ") { "$it " }
            appendLine("\tpublic ${fieldModifiers}field ${field.name} ${field.descriptor}")
        }
        for (method in stub.methods.sortedWith(compareBy({ it.name }, { it.descriptor }))) {
            if (method.name in kotlinOnlyMethods) continue
            val methodModifiers = buildList {
                if (method.isStatic) add("static")
                if (method.access and Opcodes.ACC_FINAL != 0) add("final")
                if (method.access and Opcodes.ACC_ABSTRACT != 0) add("abstract")
            }.joinToString(" ") { "$it " }
            appendLine("\tpublic ${methodModifiers}fun ${method.name} ${method.descriptor}")
        }
        appendLine("}")
        appendLine()
    }
}
