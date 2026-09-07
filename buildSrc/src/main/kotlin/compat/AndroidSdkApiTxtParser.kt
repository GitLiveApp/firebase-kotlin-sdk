package compat

/**
 * Parses Metalava signature files (`// Signature format: 3.0`) as published in the firebase-android-sdk repository
 * (`<module>/api.txt`). Only public and protected members are kept.
 */
object AndroidSdkApiTxtParser {

    private val classHeader = Regex(
        """^\s*(?:@[\w.]+(?:\([^)]*\))?\s+)*(public|protected|private)?\s*((?:(?:abstract|final|static|sealed|open|default)\s+)*)(class|interface|enum|@interface|annotation)\s+([\w.]+)(?:<.*?>)?(.*)\{\s*$""",
    )
    private val annotation = Regex("""@[\w.]+(?:\([^)]*\))?\s*""")

    fun parse(text: String): List<ApiClass> {
        val classes = mutableListOf<ApiClass>()
        var currentPackage = ""
        var currentClass: String? = null
        var currentDeprecated = false
        var members = mutableListOf<ApiMember>()

        fun flush() {
            currentClass?.let { classes += ApiClass(it, members.toList(), currentDeprecated) }
            currentClass = null
            members = mutableListOf()
        }

        for (rawLine in text.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("//")) continue
            if (line.startsWith("package ")) {
                currentPackage = line.removePrefix("package ").substringBefore(' ').substringBefore('{').trim()
                continue
            }
            val header = classHeader.find(line)
            if (header != null && !line.startsWith("method") && !line.startsWith("ctor") && !line.startsWith("field") && !line.startsWith("property")) {
                flush()
                val visibility = header.groupValues[1]
                if (visibility == "private") continue
                val name = header.groupValues[4]
                currentClass = "$currentPackage." + name.replace('.', '$')
                currentDeprecated = line.contains("@Deprecated")
                continue
            }
            if (line == "}") {
                if (currentClass != null) flush()
                continue
            }
            val className = currentClass ?: continue
            parseMember(line, className)?.let { members += it }
        }
        flush()
        return classes
    }

    private fun parseMember(line: String, className: String): ApiMember? {
        val deprecated = line.contains("@Deprecated")
        val kind = line.substringBefore(' ')
        var rest = line.substringAfter(' ').removeSuffix(";").trim()
        rest = annotation.replace(rest, "")
        val words = rest.split(Regex("\\s+"), limit = 2)
        val visibility = words[0]
        if (visibility != "public" && visibility != "protected") return null
        rest = words.getOrElse(1) { "" }
        var isStatic = false
        while (true) {
            val modifier = rest.substringBefore(' ')
            if (modifier in setOf("static", "final", "abstract", "default", "synchronized", "native", "open", "inline", "operator", "infix", "suspend")) {
                if (modifier == "static") isStatic = true
                rest = rest.substringAfter(' ').trim()
            } else {
                break
            }
        }
        // generic method type parameters, e.g. "<T> T foo(...)"
        if (rest.startsWith("<")) rest = rest.substring(TypeNames.stripGenericsPrefixLength(rest)).trim()
        return when (kind) {
            "ctor" -> {
                val params = parseParameters(rest.substringAfter('(').substringBeforeLast(')'))
                ApiMember(ApiMember.Kind.CONSTRUCTOR, "<init>", params, null, false, deprecated)
            }
            "method" -> {
                val beforeParen = rest.substringBefore('(')
                val name = beforeParen.substringAfterLast(' ')
                val type = TypeNames.fromApiTxt(beforeParen.substringBeforeLast(' '))
                val params = parseParameters(rest.substringAfter('(').substringBeforeLast(')'))
                ApiMember(ApiMember.Kind.METHOD, name, params, type, isStatic, deprecated)
            }
            "field", "enum_constant" -> {
                val declaration = rest.substringBefore('=').trim()
                val name = declaration.substringAfterLast(' ')
                val type = TypeNames.fromApiTxt(declaration.substringBeforeLast(' '))
                ApiMember(ApiMember.Kind.FIELD, name, emptyList(), type, isStatic || kind == "enum_constant", deprecated)
            }
            "property" -> {
                // Kotlin property: represented by its getter, which the BCV dump also shows.
                val declaration = rest.trim()
                val name = declaration.substringAfterLast(' ')
                val type = TypeNames.fromApiTxt(declaration.substringBeforeLast(' '))
                val getter = if (type == "boolean" && name.startsWith("is")) name else "get" + name.replaceFirstChar { it.uppercase() }
                ApiMember(ApiMember.Kind.METHOD, getter, emptyList(), type, isStatic, deprecated)
            }
            else -> null
        }
    }

    private fun parseParameters(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val params = mutableListOf<String>()
        var depth = 0
        val current = StringBuilder()
        for (c in text) {
            when {
                c == '<' -> { depth++; current.append(c) }
                c == '>' -> { depth--; current.append(c) }
                c == ',' && depth == 0 -> { params += current.toString(); current.clear() }
                else -> current.append(c)
            }
        }
        params += current.toString()
        return params.map { param ->
            // "Type name" or "Type" (metalava may include parameter names)
            val cleaned = annotation.replace(param.trim(), "").trim()
            val type = cleaned.split(Regex("\\s+")).let { if (it.size > 1 && !cleaned.endsWith("...") && !cleaned.endsWith("]")) it.dropLast(1).joinToString(" ") else if (it.size > 1 && it.last().none { ch -> ch == '[' || ch == '.' }) it.dropLast(1).joinToString(" ") else cleaned }
            TypeNames.fromApiTxt(type)
        }
    }
}

private fun TypeNames.stripGenericsPrefixLength(text: String): Int {
    var depth = 0
    for ((i, c) in text.withIndex()) {
        if (c == '<') depth++
        if (c == '>') {
            depth--
            if (depth == 0) return i + 1
        }
    }
    return 0
}
