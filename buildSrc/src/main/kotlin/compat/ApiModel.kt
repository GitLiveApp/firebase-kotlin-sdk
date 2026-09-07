package compat

/** A member of a public API class, normalised so the Android SDK's api.txt and a BCV dump can be compared. */
data class ApiMember(
    val kind: Kind,
    /** `<init>` for constructors. */
    val name: String,
    /** Erased, dotted parameter type names, e.g. `java.lang.String`, `boolean`, `com.google.firebase.FirebaseApp`, `int[]`. */
    val parameters: List<String>,
    /** Erased, dotted return/field type, null for constructors. */
    val type: String?,
    val isStatic: Boolean,
    val isDeprecated: Boolean = false,
) {
    enum class Kind { CONSTRUCTOR, METHOD, FIELD }

    val signature: String get() = "$name(${parameters.joinToString(",")})"

    /** Human readable form used in reports. */
    fun render(className: String): String {
        val simpleClass = className.substringAfterLast('.').replace('$', '.')
        return when (kind) {
            Kind.CONSTRUCTOR -> "${simpleClass.substringAfterLast('.')}(${parameters.joinToString { it.simple() }})"
            Kind.METHOD -> "${if (isStatic) "static " else ""}${type?.simple() ?: "void"} $name(${parameters.joinToString { it.simple() }})"
            Kind.FIELD -> "${if (isStatic) "static " else ""}${type?.simple()} $name"
        }
    }

    private fun String.simple(): String = substringAfterLast('.').replace('$', '.')
}

/** A public API class; [name] is dotted with `$` separating nested classes (`com.google.firebase.FirebaseOptions$Builder`). */
data class ApiClass(
    val name: String,
    val members: List<ApiMember>,
    val isDeprecated: Boolean = false,
)

/** Maps Java type names as written in api.txt / JVM descriptors to one canonical dotted form. */
object TypeNames {
    private val javaLang = setOf(
        "String", "Object", "Integer", "Long", "Boolean", "Double", "Float", "Short", "Byte", "Character", "Void",
        "Class", "Exception", "Throwable", "RuntimeException", "Number", "CharSequence", "Iterable", "Runnable", "Enum",
    )

    /** Canonicalises a type written in api.txt: strips nullability and generics, qualifies java.lang shorthands. */
    fun fromApiTxt(raw: String): String {
        var type = stripGenerics(raw.trim()).replace("?", "").replace("!", "").trim()
        var arrays = 0
        while (type.endsWith("[]") || type.endsWith("...")) {
            type = type.removeSuffix("[]").removeSuffix("...").trim()
            arrays++
        }
        if (type in javaLang) type = "java.lang.$type"
        return type + "[]".repeat(arrays)
    }

    fun stripGenerics(type: String): String {
        val out = StringBuilder()
        var depth = 0
        for (c in type) {
            when {
                c == '<' -> depth++
                c == '>' -> depth--
                depth == 0 -> out.append(c)
            }
        }
        return out.toString()
    }

    /** Parses a JVM method descriptor `(Ljava/lang/String;Z)V` into parameter types and return type. */
    fun fromDescriptor(descriptor: String): Pair<List<String>, String> {
        val params = mutableListOf<String>()
        var i = 1
        while (descriptor[i] != ')') {
            val (type, next) = readType(descriptor, i)
            params += type
            i = next
        }
        val (returnType, _) = readType(descriptor, i + 1)
        return params to returnType
    }

    fun fromFieldDescriptor(descriptor: String): String = readType(descriptor, 0).first

    private fun readType(descriptor: String, start: Int): Pair<String, Int> {
        var i = start
        var arrays = 0
        while (descriptor[i] == '[') {
            arrays++
            i++
        }
        val (base, next) = when (descriptor[i]) {
            'L' -> {
                val end = descriptor.indexOf(';', i)
                // nested classes are written with dots in api.txt, so normalise `$` here too
                descriptor.substring(i + 1, end).replace('/', '.').replace('$', '.') to end + 1
            }
            'Z' -> "boolean" to i + 1
            'B' -> "byte" to i + 1
            'C' -> "char" to i + 1
            'S' -> "short" to i + 1
            'I' -> "int" to i + 1
            'J' -> "long" to i + 1
            'F' -> "float" to i + 1
            'D' -> "double" to i + 1
            'V' -> "void" to i + 1
            else -> error("Unexpected descriptor char '${descriptor[i]}' in $descriptor")
        }
        return base + "[]".repeat(arrays) to next
    }
}
