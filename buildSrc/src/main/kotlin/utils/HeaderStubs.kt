package utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipFile

/**
 * "Header stubs" are `actual` declarations for Android/JVM that only exist so common code can compile against a class
 * that the real runtime provides (e.g. Play Services' `com.google.android.gms.tasks.Task`, which consumers also get
 * from Play Services and therefore must not be duplicated in our artifacts).
 *
 * They are compiled like any other source and then deleted from the compilation output, so the AAR/JAR, project
 * consumers, D8 and every test runtime classpath bind to the real classes instead. Before deletion, every public
 * instance member of a stub is checked against the real class in [referenceJars] so the stubs cannot drift.
 */
fun Project.stripHeaderStubs(packageDir: String, referenceJars: FileCollection) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        if (!name.startsWith("compile") || name.contains("Test")) return@configureEach
        inputs.files(referenceJars).withPropertyName("headerStubReferenceJars").optional()
        doLast {
            val stubDir = destinationDirectory.dir(packageDir).get().asFile
            if (!stubDir.exists()) return@doLast
            val references = referenceJars.files.filter { it.isFile }
            if (references.isNotEmpty()) verifyHeaderStubs(stubDir, packageDir, references)
            stubDir.deleteRecursively()
            logger.info("Removed header stubs under $packageDir from ${destinationDirectory.get()}")
        }
    }
    tasks.withType(Jar::class.java).configureEach {
        exclude("$packageDir/**")
    }
}

private class MemberCollector : ClassVisitor(Opcodes.ASM9) {
    lateinit var name: String
    var access = 0
    val instanceMembers = mutableSetOf<String>()
    val staticMembers = mutableSetOf<String>()
    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        this.name = name
        this.access = access
    }
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) != 0 && access and Opcodes.ACC_SYNTHETIC == 0 && name != "<clinit>") {
            (if (access and Opcodes.ACC_STATIC != 0) staticMembers else instanceMembers).add("$name$descriptor")
        }
        return null
    }
}

private fun readMembers(bytes: ByteArray): MemberCollector = MemberCollector().also { ClassReader(bytes).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }

private fun verifyHeaderStubs(stubDir: File, packageDir: String, referenceJars: List<File>) {
    val problems = mutableListOf<String>()
    val stubs = stubDir.walkTopDown().filter { it.isFile && it.extension == "class" }.toList()
    val zips = referenceJars.map { ZipFile(it) }
    try {
        for (stubFile in stubs) {
            val stub = readMembers(stubFile.readBytes())
            val entryName = "${stub.name}.class"
            val realBytes = zips.firstNotNullOfOrNull { zip -> zip.getEntry(entryName)?.let { zip.getInputStream(it).use { s -> s.readBytes() } } }
            if (realBytes == null) {
                problems += "${stub.name}: no such class in ${referenceJars.joinToString { it.name }}"
                continue
            }
            val real = readMembers(realBytes)
            for (member in stub.instanceMembers) {
                when {
                    member in real.instanceMembers -> Unit
                    member in real.staticMembers -> problems += "${stub.name}.$member is static in the real class (Kotlin cannot call it through the stub)"
                    else -> problems += "${stub.name}.$member does not exist in the real class"
                }
            }
            for (member in stub.staticMembers) {
                if (member !in real.staticMembers) problems += "${stub.name}.$member (static) does not exist in the real class"
            }
        }
    } finally {
        zips.forEach { it.close() }
    }
    if (problems.isNotEmpty()) {
        throw GradleException("Header stubs under $packageDir do not match the real classes:\n" + problems.joinToString("\n") { "  $it" })
    }
}
