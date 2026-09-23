package compat

/**
 * Parses the JVM API dumps written by the binary-compatibility-validator plugin (`api/android/<module>.api`) and the
 * header stub dumps written by `utils.stripHeaderStubs`, whose member lines may end in `// deprecated: <message>` for a
 * member deprecated with an error.
 */
object BcvApiParser {

    private const val DEPRECATION_COMMENT = "  // deprecated: "

    private val classHeader = Regex("""^public\s+((?:(?:final|abstract|open|static|synthetic|annotation|enum|interface)\s+)*)class\s+([\w/$]+)(?:\s*:\s*.*)?\s*\{$""")
    private val method = Regex("""^public\s+((?:(?:static|final|abstract|open|synthetic)\s+)*)fun\s+(\S+)\s+(\(.*)$""")
    private val field = Regex("""^public\s+((?:(?:static|final|enum|synthetic)\s+)*)field\s+(\S+)\s+(\S+)$""")

    fun parse(text: String): List<ApiClass> {
        val classes = mutableListOf<ApiClass>()
        var current: String? = null
        var members = mutableListOf<ApiMember>()
        for (rawLine in text.lines()) {
            val trimmed = rawLine.trim()
            val commentAt = trimmed.indexOf(DEPRECATION_COMMENT)
            val line = if (commentAt < 0) trimmed else trimmed.substring(0, commentAt)
            val deprecation = if (commentAt < 0) null else trimmed.substring(commentAt + DEPRECATION_COMMENT.length)
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
                    ApiMember(ApiMember.Kind.CONSTRUCTOR, "<init>", params, null, false, deprecation = deprecation)
                } else {
                    ApiMember(ApiMember.Kind.METHOD, name, params, returnType, "static" in modifiers, deprecation = deprecation)
                }
                return@let
            }
            field.find(line)?.let { match ->
                val modifiers = match.groupValues[1]
                if ("synthetic" in modifiers || match.groupValues[2] == "Companion") return@let
                members += ApiMember(ApiMember.Kind.FIELD, match.groupValues[2], emptyList(), TypeNames.fromFieldDescriptor(match.groupValues[3]), "static" in modifiers, deprecation = deprecation)
            }
        }
        return classes
    }
}
