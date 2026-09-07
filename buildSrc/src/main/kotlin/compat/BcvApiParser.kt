package compat

/** Parses the JVM API dumps written by the binary-compatibility-validator plugin (`api/android/<module>.api`). */
object BcvApiParser {

    private val classHeader = Regex("""^public\s+((?:(?:final|abstract|open|static|synthetic|annotation|enum|interface)\s+)*)class\s+([\w/$]+)(?:\s*:\s*.*)?\s*\{$""")
    private val method = Regex("""^public\s+((?:(?:static|final|abstract|open|synthetic)\s+)*)fun\s+(\S+)\s+(\(.*)$""")
    private val field = Regex("""^public\s+((?:(?:static|final|enum|synthetic)\s+)*)field\s+(\S+)\s+(\S+)$""")

    fun parse(text: String): List<ApiClass> {
        val classes = mutableListOf<ApiClass>()
        var current: String? = null
        var members = mutableListOf<ApiMember>()
        for (rawLine in text.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue
            classHeader.find(line)?.let {
                current = it.groupValues[2].replace('/', '.')
                members = mutableListOf()
                return@let
            }
            if (line == "}") {
                current?.let { classes += ApiClass(it, members.toList()) }
                current = null
                continue
            }
            if (current == null) continue
            method.find(line)?.let { match ->
                val modifiers = match.groupValues[1]
                if ("synthetic" in modifiers) return@let
                val name = match.groupValues[2]
                if (name.endsWith("\$default")) return@let
                val (params, returnType) = TypeNames.fromDescriptor(match.groupValues[3])
                members += if (name == "<init>") {
                    ApiMember(ApiMember.Kind.CONSTRUCTOR, "<init>", params, null, false)
                } else {
                    ApiMember(ApiMember.Kind.METHOD, name, params, returnType, "static" in modifiers)
                }
                return@let
            }
            field.find(line)?.let { match ->
                val modifiers = match.groupValues[1]
                if ("synthetic" in modifiers) return@let
                members += ApiMember(ApiMember.Kind.FIELD, match.groupValues[2], emptyList(), TypeNames.fromFieldDescriptor(match.groupValues[3]), "static" in modifiers)
            }
        }
        return classes
    }
}
