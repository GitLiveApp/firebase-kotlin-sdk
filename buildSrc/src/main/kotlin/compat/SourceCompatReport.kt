package compat

/**
 * A line of a module's `api/android-sdk/exclusions.txt`: a class or `class#member` pattern. Lines whose comment
 * contains `@hide` describe members that are hidden in the Android SDK (`@hide` in their javadoc): they are reported
 * but not counted; the other exclusions are intentionally not mirrored and count as unavailable.
 */
data class Exclusion(val pattern: Regex, val hidden: Boolean) {
    fun matches(name: String): Boolean = pattern.matches(name)
}

/**
 * Compares the Android SDK API (from api.txt) with this SDK's `com.google.*` API (from the BCV dump) and renders a report.
 *
 * Every public Android SDK member is classified as:
 * - `OK`   present with the same name, parameters and static-ness;
 * - `MAP`  present through an accepted mapping (e.g. `android.content.Context` widened to `Object`, or a different return type);
 * - `MISS` not available (Android code using it will not compile against this SDK);
 * - `OMIT` intentionally not mirrored (listed in the module's exclusions), also counted as unavailable;
 * - `SKIP` deprecated in the Android SDK, not counted;
 * - `HIDE` hidden in the Android SDK (listed in the exclusions with `@hide`), not counted.
 *
 * Classes in an `internal` package, deprecated classes and classes excluded with `@hide` are not listed at all.
 * The percentage in the header is the share of counted members that are available; it is also the module's API
 * coverage badge in the README.
 */
object SourceCompatReport {

    private val acceptedParameterMappings = mapOf(
        "android.content.Context" to "java.lang.Object",
        "android.app.Activity" to "java.lang.Object",
        "android.net.Uri" to "java.lang.String",
    )

    fun generate(
        module: String,
        ref: String,
        androidSdk: List<ApiClass>,
        ours: List<ApiClass>,
        exclusions: List<Exclusion>,
    ): String {
        val oursByName = ours.associateBy { it.name }
        val out = StringBuilder()
        var ok = 0
        var mapped = 0
        var missing = 0
        var omitted = 0
        val body = StringBuilder()

        for (sdkClass in androidSdk.sortedBy { it.name }) {
            if (sdkClass.name.contains(".internal.") || sdkClass.isDeprecated) continue
            val outerName = sdkClass.name.substringBefore('$')
            val classExclusion = exclusions.firstOrNull { it.matches(sdkClass.name) || it.matches(outerName) }
            if (classExclusion?.hidden == true) continue
            val ourClass = oursByName[sdkClass.name]
            val note = when {
                classExclusion != null -> "  (class omitted)"
                ourClass == null -> "  (class missing)"
                else -> ""
            }
            body.appendLine(sdkClass.name.replace('$', '.') + note)
            for (member in sdkClass.members.sortedWith(compareBy({ it.kind }, { it.name }, { it.parameters.size }))) {
                val rendered = member.render(sdkClass.name)
                val exclusion = classExclusion ?: exclusions.firstOrNull { it.matches("${sdkClass.name}#${member.name}") }
                when {
                    member.isDeprecated -> body.appendLine("  SKIP  $rendered")
                    exclusion?.hidden == true -> body.appendLine("  HIDE  $rendered")
                    exclusion != null -> { omitted++; body.appendLine("  OMIT  $rendered") }
                    ourClass == null -> { missing++; body.appendLine("  MISS  $rendered") }
                    else -> {
                        val (status, detail) = classify(member, ourClass)
                        when (status) {
                            "OK" -> ok++
                            "MAP" -> mapped++
                            else -> missing++
                        }
                        body.appendLine("  ${status.padEnd(4)}  $rendered${detail?.let { "  [$it]" } ?: ""}")
                    }
                }
            }
        }
        val total = ok + mapped + missing + omitted
        val percent = if (total == 0) 100 else (ok + mapped) * 100 / total
        out.appendLine("# Android SDK source compatibility of $module")
        out.appendLine("# Android SDK api.txt ref: $ref")
        out.appendLine("# $percent% of $total public members available ($ok identical, $mapped mapped, $missing missing, $omitted omitted)")
        out.appendLine("#")
        out.append(body)
        return out.toString()
    }

    private fun classify(member: ApiMember, ourClass: ApiClass): Pair<String, String?> {
        val candidates = ourClass.members.filter { it.kind == member.kind && it.name == member.name && it.parameters.size == member.parameters.size }
        if (candidates.isEmpty()) return "MISS" to null
        val exact = candidates.firstOrNull { it.parameters == member.parameters }
        val mappedParams = candidates.firstOrNull { candidate ->
            candidate.parameters.zip(member.parameters).all { (ours, theirs) -> ours == theirs || acceptedParameterMappings[theirs] == ours }
        }
        val match = exact ?: mappedParams ?: return "MISS" to null
        val notes = mutableListOf<String>()
        if (exact == null) {
            match.parameters.zip(member.parameters).filter { (ours, theirs) -> ours != theirs }
                .forEach { (ours, theirs) -> notes += "${theirs.substringAfterLast('.')} -> ${ours.substringAfterLast('.')}" }
        }
        if (member.kind != ApiMember.Kind.CONSTRUCTOR && member.isStatic != match.isStatic) {
            return "MISS" to (if (member.isStatic) "not static in this SDK" else "static in this SDK")
        }
        if (member.type != null && match.type != null && member.type != match.type && !isAcceptedReturnType(member.type, match.type)) {
            notes += "returns ${match.type.substringAfterLast('.')}"
        }
        return (if (notes.isEmpty()) "OK" else "MAP") to notes.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun isAcceptedReturnType(theirs: String, ours: String): Boolean =
        theirs == "java.lang.Void" && ours == "java.lang.Void" || (theirs == "java.util.List" && ours == "java.util.List")
}
