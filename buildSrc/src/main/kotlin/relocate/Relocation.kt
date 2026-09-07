package relocate

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.File
import java.io.Serializable
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** The package prefix under which the Firebase Android SDK is relocated (dotted, without trailing dot). */
const val RELOCATED_PACKAGE_PREFIX = "dev.gitlive.firebase.android"

/** Maven group used for the published relocated artifacts. */
const val RELOCATED_GROUP = "dev.gitlive.firebase.android"

/** Package prefixes (dotted, without trailing dot) that are relocated. */
val DEFAULT_RELOCATIONS: Map<String, String> = mapOf(
    "com.google.firebase" to RELOCATED_PACKAGE_PREFIX,
)

/**
 * Describes a set of package prefix relocations and applies them to class names, descriptors,
 * string constants and text files.
 */
class PackageRelocation(mappings: Map<String, String>) : Serializable {

    private val dotted: List<Pair<String, String>> = mappings.map { (from, to) -> "$from." to "$to." }
    private val slashed: List<Pair<String, String>> = dotted.map { (from, to) -> from.replace('.', '/') to to.replace('.', '/') }

    /** All prefixes (dotted and slashed) that must not appear in relocated output. */
    val forbiddenPrefixes: List<String> get() = dotted.map { it.first } + slashed.map { it.first }

    /**
     * Maps a JVM internal name such as `com/google/firebase/FirebaseApp`. Every occurrence of a relocated package is
     * replaced, not only a leading one, so that classes whose name embeds a package (Play Services Dynamite descriptors
     * like `com/google/android/gms/dynamite/descriptors/com/google/firebase/auth/ModuleDescriptor`) stay consistent with
     * the module id string constants that name them.
     */
    fun mapInternalName(name: String): String {
        var result = name
        for ((from, to) in slashed) result = result.replace(from, to)
        return result
    }

    fun isRelocated(internalName: String): Boolean = slashed.any { internalName.contains(it.first) }

    /** Maps a dotted package or class name, treating an exact prefix match (e.g. `com.google.firebase`) as relocated too. */
    fun mapDotted(name: String): String {
        for ((from, to) in dotted) {
            if (name.startsWith(from)) return to + name.substring(from.length)
            if (name == from.dropLast(1)) return to.dropLast(1)
        }
        return name
    }

    /** Replaces every occurrence of a relocated prefix inside an arbitrary string (constants, manifests, proguard rules). */
    fun mapString(value: String): String {
        var result = value
        for ((from, to) in dotted) result = result.replace(from, to)
        for ((from, to) in slashed) result = result.replace(from, to)
        return result
    }

    /**
     * Rewrites an `AndroidManifest.xml`: `package` attributes, component names and component meta-data.
     * The `package` attribute itself is removed because AGP 8 takes the namespace from the build script.
     */
    fun mapManifest(xml: String): String {
        var result = xml.replace(Regex("""\s+package="[^"]*""""), "")
        for ((from, to) in dotted) {
            val bare = from.dropLast(1)
            result = result.replace(Regex(Regex.escape(bare) + """(?=[."])"""), to.dropLast(1))
        }
        return result
    }
}

/** ASM remapper that relocates class names and string constants. */
class RelocatingRemapper(
    private val relocation: PackageRelocation,
    private val keepClasses: Set<String>,
) : Remapper() {
    override fun map(internalName: String): String =
        if (internalName in keepClasses) internalName else relocation.mapInternalName(internalName)

    override fun mapValue(value: Any?): Any? = when (value) {
        is String -> relocation.mapString(value)
        else -> super.mapValue(value)
    }
}

/** Drops `@kotlin.Metadata` so the Kotlin compiler treats relocated classes as plain Java classes. */
private class StripKotlinMetadata(next: ClassVisitor) : ClassVisitor(Opcodes.ASM9, next) {
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? =
        if (descriptor == "Lkotlin/Metadata;") null else super.visitAnnotation(descriptor, visible)
}

/** Collects, per class, the `hashCode` of every string constant that relocation rewrites, mapped to the new hash. */
private class StringHashCollector(private val relocation: PackageRelocation) : ClassVisitor(Opcodes.ASM9) {
    val hashes = HashMap<Int, Int>()
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor =
        object : MethodVisitor(Opcodes.ASM9) {
            override fun visitLdcInsn(value: Any?) {
                if (value is String) {
                    val mapped = relocation.mapString(value)
                    if (mapped != value) hashes[value.hashCode()] = mapped.hashCode()
                }
            }
        }
}

/**
 * `switch`/`when` over strings compiles to a `lookupswitch` on the strings' hash codes followed by `equals` checks; when
 * a relocated string constant is such a case, its precomputed hash key must be rewritten too.
 */
private class RemapStringSwitches(next: ClassVisitor, private val hashes: Map<Int, Int>) : ClassVisitor(Opcodes.ASM9, next) {
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        val next = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null
        if (hashes.isEmpty()) return next
        return object : MethodVisitor(Opcodes.ASM9, next) {
            override fun visitLookupSwitchInsn(dflt: org.objectweb.asm.Label?, keys: IntArray?, labels: Array<out org.objectweb.asm.Label>?) {
                if (keys == null || labels == null || keys.none { it in hashes }) return super.visitLookupSwitchInsn(dflt, keys, labels)
                val remapped = keys.indices.map { (hashes[keys[it]] ?: keys[it]) to labels[it] }.sortedBy { it.first }
                super.visitLookupSwitchInsn(dflt, remapped.map { it.first }.toIntArray(), remapped.map { it.second }.toTypedArray())
            }
        }
    }
}

/** Relocates class names inside the SourceDebugExtension (Kotlin inline-function SMAP) attribute, which ClassRemapper leaves alone. */
private class RemapSourceDebugExtension(next: ClassVisitor, private val relocation: PackageRelocation) : ClassVisitor(Opcodes.ASM9, next) {
    override fun visitSource(source: String?, debug: String?) = super.visitSource(source, debug?.let(relocation::mapString))
}

private class NativeMethodDetector : ClassVisitor(Opcodes.ASM9) {
    var hasNativeMethods = false
    lateinit var name: String
    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        this.name = name
    }
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access and Opcodes.ACC_NATIVE != 0) hasNativeMethods = true
        return null
    }
}

data class JarRelocationReport(
    val relocatedClasses: Int,
    val keptNativeClasses: List<String>,
    val unrelocatedReferences: List<String>,
    val remappedKotlinMetadata: Int = 0,
    val strippedKotlinMetadata: Int = 0,
)

/**
 * Relocates every class in [input] into [output].
 * Classes with native methods keep their name (JNI symbol names encode the package) but still have their references remapped.
 */
fun relocateJar(
    input: File,
    output: File,
    relocation: PackageRelocation,
    stripKotlinMetadata: Boolean,
    onlyRelocatedClasses: Boolean = false,
): JarRelocationReport {
    val keepClasses = mutableSetOf<String>()
    ZipFile(input).use { zip ->
        for (entry in zip.entries()) {
            if (!entry.name.endsWith(".class")) continue
            val detector = NativeMethodDetector()
            zip.getInputStream(entry).use { ClassReader(it).accept(detector, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }
            if (detector.hasNativeMethods && relocation.isRelocated(detector.name)) keepClasses += detector.name
        }
    }
    val remapper = RelocatingRemapper(relocation, keepClasses)
    val metadataRemapper = if (stripKotlinMetadata) null else KotlinMetadataRemapper(relocation)
    var relocated = 0
    val leftovers = mutableListOf<String>()
    output.parentFile.mkdirs()
    ZipOutputStream(output.outputStream().buffered()).use { out ->
        ZipFile(input).use { zip ->
            val written = mutableSetOf<String>()
            for (entry in zip.entries().toList().sortedBy { it.name }) {
                if (entry.isDirectory) continue
                val name = entry.name
                if (onlyRelocatedClasses && !(name.endsWith(".class") && relocation.isRelocated(name))) continue
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                val (newName, newBytes) = when {
                    name.endsWith(".class") -> {
                        val reader = ClassReader(bytes)
                        val writer = ClassWriter(0)
                        val hashes = StringHashCollector(relocation).also { reader.accept(it, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }.hashes
                        val chain: ClassVisitor = RemapStringSwitches(RemapSourceDebugExtension(ClassRemapper(writer, remapper), relocation), hashes)
                        val visitor: ClassVisitor = if (metadataRemapper == null) StripKotlinMetadata(chain) else RemapKotlinMetadata(StripKotlinMetadata(chain), writer, metadataRemapper)
                        reader.accept(visitor, 0)
                        val mapped = remapper.map(reader.className)
                        if (mapped != reader.className) relocated++
                        val result = writer.toByteArray()
                        if (reader.className !in keepClasses && containsAny(result, relocation.forbiddenPrefixes)) leftovers += mapped
                        "$mapped.class" to result
                    }
                    name.startsWith("META-INF/") && name.endsWith(".kotlin_module") ->
                        if (metadataRemapper == null) continue else "META-INF/${RELOCATED_GROUP}-${name.removePrefix("META-INF/")}" to metadataRemapper.remapModule(bytes)
                    name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC")) -> continue
                    name.startsWith("META-INF/services/") -> {
                        val service = name.removePrefix("META-INF/services/")
                        "META-INF/services/${relocation.mapDotted(service)}" to relocation.mapString(String(bytes)).toByteArray()
                    }
                    name.startsWith("META-INF/") && (name.endsWith(".pro") || name.endsWith(".txt") || name.endsWith(".properties")) ->
                        name to relocation.mapString(String(bytes)).toByteArray()
                    else -> relocation.mapInternalName(name) to bytes
                }
                if (!written.add(newName)) continue
                out.putNextEntry(ZipEntry(newName).also { it.time = 0L })
                out.write(newBytes)
                out.closeEntry()
            }
        }
    }
    return JarRelocationReport(
        relocated,
        keepClasses.sorted(),
        leftovers.sorted(),
        remappedKotlinMetadata = metadataRemapper?.remapped ?: 0,
        strippedKotlinMetadata = metadataRemapper?.unreadable ?: 0,
    )
}

/** True if [bytes] contain any of [prefixes] as an ASCII substring (constant pool strings are modified UTF-8, ASCII for these prefixes). */
fun containsAny(bytes: ByteArray, prefixes: List<String>): Boolean = prefixes.any { indexOf(bytes, it.toByteArray()) >= 0 }

private fun indexOf(haystack: ByteArray, needle: ByteArray): Int {
    if (needle.isEmpty()) return 0
    outer@ for (i in 0..haystack.size - needle.size) {
        for (j in needle.indices) if (haystack[i + j] != needle[j]) continue@outer
        return i
    }
    return -1
}

/** Lists the classes inside [jar] whose bytes reference any of [prefixes]. */
fun findReferencingClasses(jar: File, prefixes: List<String>, limit: Int = 5): List<String> {
    val found = mutableListOf<String>()
    ZipFile(jar).use { zip ->
        for (entry in zip.entries()) {
            if (!entry.name.endsWith(".class")) continue
            val bytes = zip.getInputStream(entry).use { it.readBytes() }
            if (containsAny(bytes, prefixes)) {
                found += entry.name.removeSuffix(".class")
                if (found.size >= limit) break
            }
        }
    }
    return found
}

/**
 * Lists the classes under the relocated packages that [jar] references (JVM internal names in its constant pools), except
 * those under [allowedPackages]. Dotted strings such as intent actions are ignored: they are protocol identifiers, not classes.
 */
fun findClassReferences(jar: File, prefixes: List<String>, allowedPackages: List<String>, limit: Int = 8): List<String> {
    val slashedPrefixes = prefixes.filter { it.contains('/') }
    val pattern = Regex("(?:" + slashedPrefixes.joinToString("|") { Regex.escape(it) } + ")[A-Za-z0-9_/$]*")
    val found = linkedSetOf<String>()
    ZipFile(jar).use { zip ->
        for (entry in zip.entries()) {
            if (!entry.name.endsWith(".class")) continue
            val text = zip.getInputStream(entry).use { String(it.readBytes(), Charsets.ISO_8859_1) }
            for (match in pattern.findAll(text)) {
                val reference = match.value
                if (allowedPackages.any { reference.startsWith(it) }) continue
                found += reference
                if (found.size >= limit) return found.toList()
            }
        }
    }
    return found.toList()
}
