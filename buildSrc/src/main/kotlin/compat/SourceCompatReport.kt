package compat

/**
 * A line of a module's `api/android-sdk/exclusions.txt`: a class pattern, a `class#member` pattern, or a
 * `class#member(Type, Type)` pattern that selects one overload by the simple names of its parameter types
 * (constructors are `class#<init>(...)`). [uncountedStatus] is the report status of a member that is excluded from
 * the count: `HIDE` when the comment contains `@hide` (hidden in the Android SDK), `PLAT` when it contains `@platform`
 * (the member's signature involves an Android/JVM-only type such as `Context`, `Parcel`, `Date` or `Instant`).
 * Any other exclusion is intentionally not mirrored and counts as unavailable (`OMIT`).
 */
data class Exclusion(val pattern: Regex, val uncountedStatus: String?) {
    fun matchesClass(name: String): Boolean = pattern.matches(name)

    fun matchesMember(className: String, member: ApiMember): Boolean =
        pattern.matches("$className#${member.name}") ||
            pattern.matches("$className#${member.name}(${member.parameters.joinToString(",") { it.substringAfterLast('.').replace('$', '.') }})")
}

/**
 * Compares the Android SDK API (from api.txt) with this SDK's `com.google.*` API (from the BCV dump) and renders a report.
 *
 * Every public Android SDK member is classified as:
 * - `OK`   present with the same name, parameters and static-ness;
 * - `MAP`  present through an accepted mapping (e.g. `android.content.Context` widened to `Object`, a different return
 *          type, a public field provided as a Kotlin property, or an instance method provided as a same-named extension
 *          function in one of the package's file facades with a JVM-only parameter type replaced, such as `java.net.URL`
 *          by `String`), or declared with an error-level deprecation whose message names the multiplatform replacement
 *          (an Android-only member that common code cannot call, while Android code still binds to the real member);
 * - `MISS` not available (Android code using it will not compile against this SDK);
 * - `OMIT` intentionally not mirrored (listed in the module's exclusions), also counted as unavailable;
 * - `SKIP` deprecated in the Android SDK, not counted;
 * - `HIDE` hidden in the Android SDK (listed in the exclusions with `@hide`), not counted;
 * - `PLAT` involves an Android/JVM-only type (listed in the exclusions with `@platform`), not counted.
 *
 * Classes in an `internal` package, deprecated classes and classes excluded with `@hide` or `@platform` are not listed
 * at all. A Kotlin `Companion` field is not a member of the API surface and is ignored, and the members of a Kotlin
 * file facade (`FirebaseKt`) are looked up across every facade of the package, since this SDK may declare a top-level
 * function in a differently named file.
 * The percentage in the header is the share of counted members that are available; it is also the module's API
 * coverage badge in the README.
 */
object SourceCompatReport {

    private val acceptedParameterMappings = mapOf(
        "android.content.Context" to "java.lang.Object",
        "android.app.Activity" to "java.lang.Object",
        "android.net.Uri" to "java.lang.String",
        "java.util.Date" to "java.lang.Object",
        "java.time.Instant" to "java.lang.Object",
        "java.util.concurrent.TimeUnit" to "java.lang.Object",
        "java.net.URL" to "java.lang.String",
    )

    fun generate(
        module: String,
        ref: String,
        androidSdk: List<ApiClass>,
        ours: List<ApiClass>,
        exclusions: List<Exclusion>,
    ): String {
        val oursByName = ours.associateBy { it.name }

        // Top-level functions may live in any file facade (`*Kt`) of the package: the facade name is a compilation detail.
        fun facadeMembers(pkg: String): List<ApiMember> =
            ours.filter { it.name.substringBeforeLast('.') == pkg && it.name.substringAfterLast('.').endsWith("Kt") }.flatMap { it.members }
        fun ourClassFor(name: String): ApiClass? {
            if (!name.endsWith("Kt")) return oursByName[name]
            val members = facadeMembers(name.substringBeforeLast('.'))
            return if (members.isEmpty()) null else ApiClass(name, members)
        }
        val out = StringBuilder()
        var ok = 0
        var mapped = 0
        var missing = 0
        var omitted = 0
        val body = StringBuilder()

        for (sdkClass in androidSdk.sortedBy { it.name }) {
            if (sdkClass.name.contains(".internal.") || sdkClass.isDeprecated) continue
            val outerName = sdkClass.name.substringBefore('$')
            val classExclusion = exclusions.firstOrNull { it.matchesClass(sdkClass.name) || it.matchesClass(outerName) }
            if (classExclusion?.uncountedStatus != null) continue
            val ourClass = ourClassFor(sdkClass.name)
            val note = when {
                classExclusion != null -> "  (class omitted)"
                ourClass == null -> "  (class missing)"
                else -> ""
            }
            body.appendLine(sdkClass.name.replace('$', '.') + note)
            for (member in sdkClass.members.sortedWith(compareBy({ it.kind }, { it.name }, { it.parameters.size }))) {
                val rendered = member.render(sdkClass.name)
                val exclusion = classExclusion ?: exclusions.firstOrNull { it.matchesMember(sdkClass.name, member) }
                when {
                    member.isDeprecated -> body.appendLine("  SKIP  $rendered")
                    exclusion?.uncountedStatus != null -> body.appendLine("  ${exclusion.uncountedStatus}  $rendered")
                    exclusion != null -> { omitted++; body.appendLine("  OMIT  $rendered") }
                    ourClass == null -> { missing++; body.appendLine("  MISS  $rendered") }
                    else -> {
                        val (status, detail) = classify(member, ourClass, facadeMembers(outerName.substringBeforeLast('.')))
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

    private fun classify(member: ApiMember, ourClass: ApiClass, facadeMembers: List<ApiMember>): Pair<String, String?> {
        val candidates = ourClass.members.filter { it.kind == member.kind && it.name == member.name && it.parameters.size == member.parameters.size }
        if (candidates.isEmpty()) {
            if (member.kind == ApiMember.Kind.FIELD && !member.isStatic) return classifyFieldAsProperty(member, ourClass)
            if (member.kind == ApiMember.Kind.METHOD && !member.isStatic && !ourClass.name.endsWith("Kt")) return classifyMethodAsExtension(member, ourClass, facadeMembers)
            return "MISS" to null
        }
        val exact = candidates.firstOrNull { it.parameters == member.parameters }
        val mappedParams = candidates.firstOrNull { candidate -> candidate.parameters.mapsFrom(member.parameters) }
        val match = exact ?: mappedParams ?: return "MISS" to null
        val notes = mutableListOf<String>()
        if (exact == null) notes += mappingNotes(match.parameters, member.parameters)
        if (member.kind != ApiMember.Kind.CONSTRUCTOR && member.isStatic != match.isStatic) {
            return "MISS" to (if (member.isStatic) "not static in this SDK" else "static in this SDK")
        }
        if (member.type != null && match.type != null && member.type != match.type && !isAcceptedReturnType(member.type, match.type)) {
            notes += "returns ${match.type.substringAfterLast('.')}"
        }
        match.deprecation?.let { notes += "deprecated: $it" }
        return (if (notes.isEmpty()) "OK" else "MAP") to notes.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    /** A public instance field is provided as a Kotlin property: a getter, plus a setter when the field is assignable. */
    private fun classifyFieldAsProperty(field: ApiMember, ourClass: ApiClass): Pair<String, String?> {
        val capitalised = field.name.replaceFirstChar { it.uppercase() }
        val getterNames = if (field.type == "boolean" && field.name.startsWith("is")) setOf(field.name) else setOf("get$capitalised", "is$capitalised")
        val getter = ourClass.members.firstOrNull { it.kind == ApiMember.Kind.METHOD && !it.isStatic && it.name in getterNames && it.parameters.isEmpty() && it.type == field.type }
            ?: return "MISS" to null
        if (!field.isFinal) {
            ourClass.members.firstOrNull { it.kind == ApiMember.Kind.METHOD && !it.isStatic && it.name == "set$capitalised" && it.parameters == listOf(field.type) }
                ?: return "MISS" to "read-only property in this SDK"
        }
        return "MAP" to "property ${getter.name}()"
    }

    /**
     * An instance method whose signature involves a JVM-only type is provided as a same-named extension function in one
     * of the package's file facades (the receiver is the first parameter), since a member cannot be given an overload
     * that common code could call without shadowing it.
     */
    private fun classifyMethodAsExtension(method: ApiMember, ourClass: ApiClass, facadeMembers: List<ApiMember>): Pair<String, String?> {
        val extension = facadeMembers.firstOrNull { candidate ->
            candidate.kind == ApiMember.Kind.METHOD && candidate.isStatic && candidate.name == method.name &&
                candidate.parameters.size == method.parameters.size + 1 &&
                candidate.parameters.first() == ourClass.name.replace('$', '.') &&
                candidate.parameters.drop(1).mapsFrom(method.parameters)
        } ?: return "MISS" to null
        val notes = mutableListOf("extension function")
        notes += mappingNotes(extension.parameters.drop(1), method.parameters)
        if (method.type != null && extension.type != null && method.type != extension.type && !isAcceptedReturnType(method.type, extension.type)) {
            notes += "returns ${extension.type.substringAfterLast('.')}"
        }
        return "MAP" to notes.joinToString(", ")
    }

    private fun List<String>.mapsFrom(theirs: List<String>): Boolean =
        size == theirs.size && zip(theirs).all { (ours, theirs) -> ours == theirs || acceptedParameterMappings[theirs] == ours }

    private fun mappingNotes(ours: List<String>, theirs: List<String>): List<String> =
        ours.zip(theirs).filter { (ours, theirs) -> ours != theirs }.map { (ours, theirs) -> "${theirs.substringAfterLast('.')} -> ${ours.substringAfterLast('.')}" }

    private fun isAcceptedReturnType(theirs: String, ours: String): Boolean =
        theirs == "java.lang.Void" && ours == "java.lang.Void" || (theirs == "java.util.List" && ours == "java.util.List") ||
            (isTypeVariable(theirs) && ours == "java.lang.Object")

    /** A method type parameter such as `T`, which the BCV dump shows erased. */
    private fun isTypeVariable(type: String): Boolean = type.length <= 2 && type.first().isUpperCase() && !type.contains('.')
}
