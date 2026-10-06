package compat

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipFile

/**
 * Writes a Metalava-style signature file (`// Signature format: 3.0`, see [AndroidSdkApiTxtParser]) for the public API
 * of compiled classes, for the Firebase Android SDK modules that are not open source (`firebase-auth`, `firebase-analytics`)
 * and so publish no `api.txt`.
 *
 * The public API is read from the bytecode: public and protected, non-synthetic members of public classes under the given
 * packages. What the SDK keeps out of its documented API is left out here too: obfuscated members and classes (`zz`
 * names), the `internal` packages and members whose signature involves an internal or obfuscated type, members marked
 * `@KeepForSdk`, `@Hide`, `@ShowFirstParty` or `@RestrictTo`, and the `Parcelable` plumbing (`CREATOR`, `writeToParcel`).
 * Types are erased, and `@Deprecated` members are marked as such.
 */
object AndroidSdkApiJarDumper {

    private val hiddenAnnotations = setOf(
        "Lcom/google/android/gms/common/annotation/KeepForSdk;",
        "Lcom/google/android/gms/common/internal/Hide;",
        "Lcom/google/android/gms/common/internal/ShowFirstParty;",
        "Landroidx/annotation/RestrictTo;",
    )
    private val deprecatedAnnotations = setOf("Ljava/lang/Deprecated;", "Lkotlin/Deprecated;")

    /** Dumps the classes of [jars] whose internal name starts with one of [packages] (e.g. `com/google/firebase/auth/`). */
    fun dump(jars: List<File>, packages: List<String>): String {
        val classes = jars.flatMap { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { entry -> entry.name.endsWith(".class") && packages.any { entry.name.startsWith(it) } }
                    .map { zip.getInputStream(it).use { stream -> stream.readBytes() } }
                    .toList()
            }
        }
        return dumpClasses(classes)
    }

    fun dumpClasses(classFiles: List<ByteArray>): String {
        val classes = classFiles.map { bytes -> ClassInfo().also { ClassReader(bytes).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) } }
            .filter { it.isApi }
            .sortedBy { it.name }
        return buildString {
            appendLine("// Signature format: 3.0")
            for ((pkg, members) in classes.groupBy { it.name.substringBeforeLast('/').replace('/', '.') }.toSortedMap()) {
                appendLine("package $pkg {")
                appendLine()
                for (cls in members) {
                    append(cls.render())
                    appendLine()
                }
                appendLine("}")
                appendLine()
            }
        }
    }

    private class MemberInfo(val name: String, val descriptor: String, val access: Int, val value: Any?) {
        var hidden = false
        var deprecated = false

        fun annotationVisitor(descriptor: String): AnnotationVisitor? {
            if (descriptor in hiddenAnnotations) hidden = true
            if (descriptor in deprecatedAnnotations) deprecated = true
            return null
        }

        val isApi: Boolean get() =
            access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) != 0 &&
                access and (Opcodes.ACC_SYNTHETIC or Opcodes.ACC_BRIDGE) == 0 &&
                !hidden && name != "<clinit>" && !name.startsWith("zz") && name != "Companion" &&
                !isParcelablePlumbing && descriptor.referencedTypes().none { it.isHiddenType() }

        private val isParcelablePlumbing: Boolean get() =
            (name == "CREATOR" && descriptor == "Landroid/os/Parcelable\$Creator;") || (name == "writeToParcel" && descriptor == "(Landroid/os/Parcel;I)V")
    }

    private class ClassInfo : ClassVisitor(Opcodes.ASM9) {
        lateinit var name: String
        var access = 0
        var superName: String? = null
        var interfaces: List<String> = emptyList()
        var innerAccess: Int? = null
        var hidden = false
        var deprecated = false
        val methods = mutableListOf<MemberInfo>()
        val fields = mutableListOf<MemberInfo>()

        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            this.name = name
            this.access = access
            this.superName = superName
            this.interfaces = interfaces?.toList().orEmpty()
        }

        override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
            if (name == this.name) innerAccess = access
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (descriptor in hiddenAnnotations) hidden = true
            if (descriptor in deprecatedAnnotations) deprecated = true
            return null
        }

        override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
            val member = MemberInfo(name, descriptor, access, null).also { methods += it }
            return object : MethodVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? = member.annotationVisitor(descriptor)
            }
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
            val member = MemberInfo(name, descriptor, access, value).also { fields += it }
            return object : FieldVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? = member.annotationVisitor(descriptor)
            }
        }

        val effectiveAccess: Int get() = innerAccess ?: access
        val isInterface: Boolean get() = access and Opcodes.ACC_INTERFACE != 0
        val isAnnotation: Boolean get() = access and Opcodes.ACC_ANNOTATION != 0
        val isEnum: Boolean get() = access and Opcodes.ACC_ENUM != 0

        val isApi: Boolean get() =
            effectiveAccess and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) != 0 &&
                access and Opcodes.ACC_SYNTHETIC == 0 && !hidden && !name.isHiddenType() && !isResourceClass &&
                !name.substringAfterLast('/').substringAfterLast('$').let { it.isEmpty() || it[0].isDigit() }

        /** The generated Android resource class (`R` and its `R$string`-style nested classes) is not API. */
        private val isResourceClass: Boolean get() = name.substringAfterLast('/').substringBefore('$') == "R"

        fun render(): String = buildString {
            val simpleName = name.substringAfterLast('/').replace('$', '.')
            val modifiers = buildList {
                if (deprecated) add("@Deprecated")
                add(if (effectiveAccess and Opcodes.ACC_PROTECTED != 0) "protected" else "public")
                if (!isInterface && access and Opcodes.ACC_ABSTRACT != 0) add("abstract")
                if (name.contains('$') && effectiveAccess and Opcodes.ACC_STATIC != 0 && !isEnum) add("static")
                if (access and Opcodes.ACC_FINAL != 0 && !isEnum) add("final")
                add(
                    when {
                        isAnnotation -> "@interface"
                        isInterface -> "interface"
                        isEnum -> "enum"
                        else -> "class"
                    },
                )
            }
            val supers = buildList {
                superName?.takeIf { !isInterface && !isEnum && it != "java/lang/Object" && !it.isHiddenType() }?.let { add("extends ${it.dotted()}") }
                val visibleInterfaces = interfaces.filter { !it.isHiddenType() && it != "java/lang/annotation/Annotation" }
                if (visibleInterfaces.isNotEmpty()) add((if (isInterface) "extends " else "implements ") + visibleInterfaces.joinToString(" ") { it.dotted() })
            }
            appendLine("  ${modifiers.joinToString(" ")} $simpleName${supers.joinToString("") { " $it" }} {")
            val ctors = methods.filter { it.isApi && it.name == "<init>" }.sortedBy { it.descriptor }
            for (ctor in ctors) {
                appendLine("    ctor ${ctor.prefix()}$simpleName(${ctor.parameters()});")
            }
            val apiMethods = methods.filter { it.isApi && it.name != "<init>" && !(isEnum && (it.name == "values" || it.name == "valueOf")) }
            for (method in apiMethods.sortedWith(compareBy({ it.name }, { it.descriptor }))) {
                val returnType = TypeNames.fromDescriptor(method.descriptor).second
                appendLine("    method ${method.prefix()}${method.modifiers()}${returnType.simple()} ${method.name}(${method.parameters()});")
            }
            for (field in fields.filter { it.isApi && it.name != "\$VALUES" }.sortedBy { it.name }) {
                val type = TypeNames.fromFieldDescriptor(field.descriptor).simple()
                if (field.access and Opcodes.ACC_ENUM != 0) {
                    appendLine("    enum_constant ${field.prefix()}static final $type ${field.name};")
                } else {
                    val value = field.value?.let { " = ${it.literal()}" }.orEmpty()
                    appendLine("    field ${field.prefix()}${field.modifiers()}$type ${field.name}$value;")
                }
            }
            appendLine("  }")
        }

        private fun MemberInfo.prefix(): String = (if (deprecated) "@Deprecated " else "") + (if (access and Opcodes.ACC_PROTECTED != 0) "protected " else "public ")

        private fun MemberInfo.modifiers(): String = buildString {
            if (access and Opcodes.ACC_STATIC != 0) append("static ")
            if (access and Opcodes.ACC_FINAL != 0) append("final ")
            if (access and Opcodes.ACC_ABSTRACT != 0 && (!isInterface || isAnnotation)) append("abstract ")
        }

        private fun MemberInfo.parameters(): String {
            val types = TypeNames.fromDescriptor(descriptor).first.map { it.simple() }
            val varargs = access and Opcodes.ACC_VARARGS != 0
            return types.mapIndexed { index, type -> if (varargs && index == types.lastIndex) type.removeSuffix("[]") + "..." else type }.joinToString(", ")
        }
    }

    private fun Any.literal(): String = when (this) {
        is String -> "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        is Long -> "${this}L"
        is Float -> "${this}F"
        else -> toString()
    }

    private fun String.dotted(): String = replace('/', '.').replace('$', '.')

    /** `java.lang.String` as Metalava writes it (`String`); other types fully qualified, nested classes dotted. */
    private fun String.simple(): String = if (startsWith("java.lang.") && substringAfter("java.lang.").none { it == '.' }) substringAfter("java.lang.") else this

    /** The internal names of the classes a descriptor references. */
    private fun String.referencedTypes(): List<String> = Regex("L([^;]+);").findAll(this).map { it.groupValues[1] }.toList()

    /** Obfuscated (`zz`), library-internal or injected (components, inject) types are not part of the documented API. */
    private fun String.isHiddenType(): Boolean {
        val segments = split('/', '$')
        return segments.any { it.startsWith("zz") } || segments.dropLast(1).any { it == "internal" } ||
            startsWith("com/google/firebase/components/") || startsWith("com/google/firebase/inject/") || startsWith("com/google/firebase/heartbeatinfo/") ||
            startsWith("com/google/android/gms/common/internal/safeparcel/")
    }
}
