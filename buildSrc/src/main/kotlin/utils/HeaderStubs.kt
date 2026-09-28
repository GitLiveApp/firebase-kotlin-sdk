package utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.objectweb.asm.AnnotationVisitor
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
 * The exception is a member deprecated with `DeprecationLevel.ERROR` (or `HIDDEN`): it exists so that common code using
 * an Android-only member gets a compile error naming the replacement, cannot be called, and so need not match the real
 * class; the dump records its message so the report can count the member as provided.
 *
 * A declaration that is identical on every platform (listener interfaces, a trivial exception subclass) does not need
 * expect/actual at all: plain common code under these packages is compiled, verified and stripped on Android/JVM the
 * same way, and is real code on the other platforms.
 *
 * Rules for code in this module: the stubs' companion members compile to calls through the `Companion` object, which the
 * real classes do not have, so this module's own code must reach static SDK members through the SDK's Kotlin extensions
 * (real static facade methods) or a Java helper; consumers are unaffected because they compile against the real classes.
 */
fun Project.stripHeaderStubs(
    packageDirs: List<String>,
    androidReferenceJars: FileCollection,
    jvmReferenceJars: FileCollection,
    /** Class files under [packageDirs] (e.g. `com/google/firebase/FirebaseInitializeKt.class`) that are real code and are shipped. */
    keepClasses: List<String> = emptyList(),
    /**
     * Members of the Android SDK that `firebase-java-sdk` (the JVM target's reference) does not have or keeps non-public,
     * as `internal/class/Name.member(descriptor)` (a field: `internal/class/Name.field`; a whole class: `internal/class/Name`).
     * They are verified against the Android SDK only; JVM code using them fails to compile against the java SDK, as it
     * would without this layer.
     */
    jvmMissingMembers: List<String> = emptyList(),
    /**
     * `false` when `firebase-java-sdk` does not provide the module at all: the JVM actuals under [packageDirs] are then
     * real implementations that are shipped, and only the Android compilations are stubs.
     */
    jvmStubs: Boolean = true,
) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        if (!name.startsWith("compile") || name.contains("Test")) return@configureEach
        val android = name.contains("Android")
        if (!android && (name != "compileKotlinJvm" || !jvmStubs)) return@configureEach
        val references = if (android) androidReferenceJars else jvmReferenceJars
        inputs.files(references).withPropertyName("headerStubReferenceJars").optional()
        // Not declared as a task output: the Kotlin compile task pre-creates declared outputs as directories.
        val dump = project.layout.buildDirectory.file("header-stubs/$name.api")
        doLast {
            val destination = destinationDirectory.get().asFile
            val stubDirs = packageDirs.map { destination.resolve(it) }.filter { it.exists() }
            val stubs = stubDirs.flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "class" }.toList() }
                .filter { it.relativeTo(destination).invariantSeparatorsPath !in keepClasses }
            val classes = stubs.map { readMembers(it.readBytes()) }
            verifyHeaderStubs(classes, references.files.filter { it.isFile }, tolerated = if (android) emptySet() else jvmMissingMembers.toSet())
            dump.get().asFile.also { it.parentFile.mkdirs() }.writeText(dumpStubs(classes))
            stubs.forEach { it.delete() }
            stubDirs.forEach { dir -> dir.walkBottomUp().filter { it.isDirectory && it.listFiles().isNullOrEmpty() }.forEach { it.delete() } }
            logger.info("Removed header stubs ${packageDirs} from $destination")
        }
    }
    tasks.withType(Jar::class.java).configureEach {
        if (!jvmStubs && name.startsWith("jvm")) return@configureEach
        exclude { element -> !element.isDirectory && packageDirs.any { element.path.startsWith("$it/") } && element.path !in keepClasses }
    }
}

private class Member(val name: String, val descriptor: String, val access: Int) {
    val isStatic get() = access and Opcodes.ACC_STATIC != 0
    val signature get() = "$name$descriptor"

    /** The message of a `@kotlin.Deprecated` annotation with level `ERROR` or `HIDDEN`; such a member cannot be called. */
    var deprecation: String? = null

    /** Reads `@kotlin.Deprecated` and records the message when the level makes the member uncallable. */
    fun annotationVisitor(descriptor: String): AnnotationVisitor? {
        if (descriptor != "Lkotlin/Deprecated;") return null
        return object : AnnotationVisitor(Opcodes.ASM9) {
            var message = ""
            var uncallable = false
            override fun visit(name: String?, value: Any?) {
                if (name == "message") message = value.toString()
            }
            override fun visitEnum(name: String?, descriptor: String?, value: String?) {
                if (name == "level") uncallable = value == "ERROR" || value == "HIDDEN"
            }
            override fun visitEnd() {
                if (uncallable) deprecation = message
            }
        }
    }
}

private class ClassMembers : ClassVisitor(Opcodes.ASM9) {
    lateinit var name: String
    var access = 0
    var superName: String? = null
    var interfaces: List<String> = emptyList()
    val methods = mutableListOf<Member>()
    val fields = mutableListOf<Member>()

    /** Names of the class's own non-public fields: they shadow an inherited public constant of the same name. */
    val hiddenFields = mutableSetOf<String>()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        this.name = name
        this.access = access
        this.superName = superName
        this.interfaces = interfaces?.toList().orEmpty()
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) == 0 || access and Opcodes.ACC_SYNTHETIC != 0 || name == "<clinit>") return null
        val member = Member(name, descriptor, access).also { methods += it }
        return object : MethodVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? = member.annotationVisitor(descriptor)
        }
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        if (access and Opcodes.ACC_SYNTHETIC != 0) return null
        if (access and Opcodes.ACC_PUBLIC == 0) {
            hiddenFields += name
            return null
        }
        val member = Member(name, descriptor, access).also { fields += it }
        return object : FieldVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? = member.annotationVisitor(descriptor)
        }
    }

    /**
     * Kotlin-only classes and members that have no counterpart on the real (Java) class and are never reached at runtime,
     * including the non-public classes of plain common code (private implementations, lambdas), which only its own
     * stripped code uses.
     */
    val isKotlinOnly get() = name.endsWith("\$Companion") || name.contains("\$WhenMappings") || name.contains("\$EntriesMappings") || access and Opcodes.ACC_PUBLIC == 0 || isAnonymous

    /** An anonymous object or lambda class (`Outer$fn$1`), an implementation detail of plain common code. */
    val isAnonymous get() = anonymousClass.containsMatchIn(name)
}

private fun readMembers(bytes: ByteArray): ClassMembers = ClassMembers().also { ClassReader(bytes).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }

private val kotlinOnlyMethods = setOf("getEntries")
private val anonymousClass = Regex("""\$\d+(\$|$)""")

/**
 * A Kotlin `var` in a stub compiles to a `void` setter, while the SDK's builders return themselves from their setters.
 * Kotlin code compiled against the real class assigns such a property through the fluent setter (a synthetic property
 * accepts any return type), so the stub's setter is matched by the real setter with the same parameters. This module's
 * own shipped code must use the fluent function form, which the stubs also declare.
 */
private fun Member.isFluentSetterOf(setter: Member): Boolean =
    setter.name.startsWith("set") && setter.descriptor.endsWith(")V") && name == setter.name && !isStatic &&
        descriptor.substringBefore(')') == setter.descriptor.substringBefore(')') && !descriptor.endsWith(")V")

private fun verifyHeaderStubs(stubs: List<ClassMembers>, referenceJars: List<File>, tolerated: Set<String>) {
    val problems = mutableListOf<String>()
    val zips = referenceJars.map { ZipFile(it) }
    val cache = mutableMapOf<String, ClassMembers?>()
    fun realClass(name: String): ClassMembers? = cache.getOrPut(name) {
        zips.firstNotNullOfOrNull { zip -> zip.getEntry("$name.class")?.let { zip.getInputStream(it).use { s -> readMembers(s.readBytes()) } } }
    }

    /** The public members of a real class including those inherited from its superclasses and interfaces (Java constants live on interfaces). */
    fun inherited(real: ClassMembers, seen: MutableSet<String> = mutableSetOf()): List<ClassMembers> =
        if (!seen.add(real.name)) emptyList() else listOf(real) + (listOfNotNull(real.superName) + real.interfaces).mapNotNull { realClass(it) }.flatMap { inherited(it, seen) }

    /** A Kotlin file facade whose name is not in the reference jars is verified against every facade of its package: the facade name is a compilation detail. */
    fun packageFacades(stub: ClassMembers): List<ClassMembers> {
        val dir = stub.name.substringBeforeLast('/') + "/"
        return zips.flatMap { zip -> zip.entries().asSequence().filter { it.name.startsWith(dir) && it.name.endsWith("Kt.class") && !it.name.substring(dir.length).contains('/') }.toList() }
            .mapNotNull { realClass(it.name.removeSuffix(".class")) }
    }
    try {
        for (stub in stubs) {
            if (stub.isKotlinOnly || stub.name in tolerated) continue
            val real = realClass(stub.name)
            val reals = when {
                real != null -> inherited(real)
                stub.name.endsWith("Kt") -> packageFacades(stub).takeIf { it.isNotEmpty() }
                else -> null
            }
            if (reals == null) {
                problems += "${stub.name}: no such class on the compile classpath"
                continue
            }
            for (member in stub.methods) {
                if (member.name in kotlinOnlyMethods || member.deprecation != null || "${stub.name}.${member.signature}" in tolerated) continue
                val match = reals.firstNotNullOfOrNull { candidate -> candidate.methods.firstOrNull { it.signature == member.signature } }
                    ?: reals.firstNotNullOfOrNull { candidate -> candidate.methods.firstOrNull { it.isFluentSetterOf(member) } }
                when {
                    match == null -> problems += "${stub.name}.${member.signature} does not exist on the real class"
                    match.isStatic != member.isStatic -> problems += "${stub.name}.${member.signature} is ${if (match.isStatic) "static" else "not static"} on the real class"
                }
            }
            for (field in stub.fields) {
                if (field.name == "Companion" || field.deprecation != null || "${stub.name}.${field.name}" in tolerated) continue
                val match = reals.firstNotNullOfOrNull { candidate -> candidate.fields.firstOrNull { it.signature == field.signature } }
                when {
                    match == null || match.isStatic != field.isStatic -> problems += "${stub.name}.${field.name} does not exist on the real class"
                    field.name in reals.first().hiddenFields -> problems += "${stub.name}.${field.name} is shadowed by a non-public field of the real class"
                }
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
 * Companion objects are included (the Android SDK's Kotlin classes have them too); the mapping classes are not. A member
 * that is deprecated with an error carries its message as a trailing `// deprecated: ...` comment.
 */
private fun dumpStubs(stubs: List<ClassMembers>): String = buildString {
    for (stub in stubs.sortedBy { it.name }) {
        if (stub.name.contains("\$WhenMappings") || stub.name.contains("\$EntriesMappings") || stub.access and Opcodes.ACC_PUBLIC == 0 || stub.isAnonymous) continue
        val modifiers = buildList {
            if (stub.access and Opcodes.ACC_ABSTRACT != 0 && stub.access and Opcodes.ACC_INTERFACE == 0) add("abstract")
            if (stub.access and Opcodes.ACC_FINAL != 0) add("final")
        }.joinToString(" ") { "$it " }
        val kind = when {
            stub.access and Opcodes.ACC_ANNOTATION != 0 -> "abstract interface annotation class"
            stub.access and Opcodes.ACC_INTERFACE != 0 -> "abstract interface class"
            else -> "class"
        }
        val superClause = stub.superName?.takeIf { it != "java/lang/Object" }?.let { " : $it" }.orEmpty()
        appendLine("public $modifiers$kind ${stub.name}$superClause {")
        for (field in stub.fields.sortedBy { it.name }) {
            if (field.name == "Companion") continue
            val fieldModifiers = buildList {
                if (field.isStatic) add("static")
                if (field.access and Opcodes.ACC_FINAL != 0) add("final")
                if (field.access and Opcodes.ACC_ENUM != 0) add("enum")
            }.joinToString(" ") { "$it " }
            appendLine("\tpublic ${fieldModifiers}field ${field.name} ${field.descriptor}${field.deprecationComment()}")
        }
        for (method in stub.methods.sortedWith(compareBy({ it.name }, { it.descriptor }))) {
            if (method.name in kotlinOnlyMethods) continue
            val methodModifiers = buildList {
                if (method.isStatic) add("static")
                if (method.access and Opcodes.ACC_FINAL != 0) add("final")
                if (method.access and Opcodes.ACC_ABSTRACT != 0) add("abstract")
            }.joinToString(" ") { "$it " }
            appendLine("\tpublic ${methodModifiers}fun ${method.name} ${method.descriptor}${method.deprecationComment()}")
        }
        appendLine("}")
        appendLine()
    }
}

private fun Member.deprecationComment(): String = deprecation?.let { "  // deprecated: ${it.replace('\n', ' ')}" }.orEmpty()
