package compat


// The Android/JVM-only members of Task are error-deprecated in firebase-app with messages naming the replacement; these notes
// stand in for them on the inherited rows of the Task subclasses.
private const val TASK_SCOPED_OVERLOADS = "the Executor and Activity overloads of the Task API are Android/JVM-only (error-deprecated in com.google.android.gms.tasks.Task)"
private const val TASK_GET_RESULT_CLASS = "getResult(Class) is JVM-only (error-deprecated in com.google.android.gms.tasks.Task); use result or exception"
/**
 * A line of a module's `api/android-sdk/exclusions.txt`: a class pattern, a `class#member` pattern, or a
 * `class#member(Type, Type)` pattern that selects one overload by the simple names of its parameter types
 * (constructors are `class#<init>(...)`). [uncountedStatus] is the report status of a member that is excluded from
 * the count: `HIDE` when the comment contains `@hide` (hidden in the Android SDK), `PLAT` when it contains `@platform`
 * (the member's signature involves an Android/JVM-only type such as `Context`, `Parcel`, `Date` or `Instant`) and `UNAV`
 * when it contains `@unavailable` (the feature does not exist in the pinned iOS or JS SDK, such as the Firestore pipeline API).
 * Any other exclusion is intentionally not mirrored and counts as unavailable (`OMIT`).
 */
data class Exclusion(val pattern: Regex, val uncountedStatus: String?) {
    fun matchesClass(name: String): Boolean = pattern.matches(name)

    /** [className] is the binary name (`Outer$Nested`); patterns are written in source form (`Outer.Nested#member`), so both are tried. */
    fun matchesMember(className: String, member: ApiMember): Boolean =
        listOf(className, className.replace('$', '.')).distinct().any { name ->
            pattern.matches("$name#${member.name}") ||
                pattern.matches("$name#${member.name}(${member.parameters.joinToString(",") { it.simpleName() }})") ||
                pattern.matches("$name#${member.name}(${member.parameters.joinToString(",") { it.nestedName() }})")
        }

    /** `TaskSnapshot` for `com.google.firebase.storage.UploadTask$TaskSnapshot`. */
    private fun String.simpleName(): String = substringAfterLast('.').substringAfterLast('$')

    /** `UploadTask.TaskSnapshot` for `com.google.firebase.storage.UploadTask$TaskSnapshot` (the name without its package). */
    private fun String.nestedName(): String = replace('$', '.').split('.').dropWhile { it.firstOrNull()?.isLowerCase() == true }.joinToString(".")
}

/**
 * Compares the Android SDK API (from api.txt) with this SDK's `com.google.*` API (from the BCV dump) and renders a report.
 *
 * Every public Android SDK member is classified as:
 * - `OK`   present with the same name, parameters and static-ness;
 * - `MAP`  present with the same parameters but a different return type, a public field provided as a Kotlin property, or
 *          declared with an error-level deprecation whose message names the multiplatform replacement (an Android-only
 *          member that common code cannot call, while Android code still binds to the real member). Parameter types must
 *          match the Android SDK's exactly: a platform type in a signature is declared through its stand-in class;
 * - `MISS` not available (Android code using it will not compile against this SDK);
 * - `OMIT` intentionally not mirrored (listed in the module's exclusions), also counted as unavailable;
 * - `SKIP` deprecated in the Android SDK, not counted;
 * - `HIDE` hidden in the Android SDK (listed in the exclusions with `@hide`), not counted;
 * - `PLAT` involves an Android/JVM-only type (listed in the exclusions with `@platform`), not counted;
 * - `UNAV` not available in the pinned iOS or JS SDK (listed in the exclusions with `@unavailable`), not counted.
 *
 * Classes in an `internal` package, deprecated classes and classes excluded with `@hide`, `@platform` or `@unavailable` are not listed
 * at all. A Kotlin `Companion` field is not a member of the API surface and is ignored, and the members of a Kotlin
 * file facade (`FirebaseKt`) are looked up across every facade of the package, since this SDK may declare a top-level
 * function in a differently named file.
 * The percentage in the header is the share of counted members that are available; it is also the module's API
 * coverage badge in the README.
 */
object SourceCompatReport {

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
                // An excluded or SDK-deprecated member the layer declares with a deprecation, so common code gets its message.
                val declared = ourClass?.let { declaredMember(member, it, oursByName) }
                val declaredDeprecation = declared?.deprecation?.let { "  [deprecated: $it]" }.orEmpty()
                when {
                    member.isDeprecated -> {
                        val note = when {
                            declaredDeprecation.isNotEmpty() -> declaredDeprecation
                            ourClass == null || (declared == null && classify(member, ourClass, oursByName).first == "MISS") -> "  [not declared]"
                            else -> ""
                        }
                        body.appendLine("  SKIP  $rendered$note")
                    }
                    exclusion?.uncountedStatus != null -> body.appendLine("  ${exclusion.uncountedStatus}  $rendered$declaredDeprecation")
                    exclusion != null -> { omitted++; body.appendLine("  OMIT  $rendered$declaredDeprecation") }
                    ourClass == null -> { missing++; body.appendLine("  MISS  $rendered") }
                    else -> {
                        val (status, detail) = classify(member, ourClass, oursByName)
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

    /**
     * The members of `com.google.android.gms.tasks.Task` as this SDK declares it (firebase-app), for the modules whose
     * classes extend it: the Android SDK's api.txt lists the overrides, this SDK's classes inherit them.
     */
    private val taskMembers = listOf(
        ApiMember(ApiMember.Kind.METHOD, "isComplete", emptyList(), "boolean", false),
        ApiMember(ApiMember.Kind.METHOD, "isSuccessful", emptyList(), "boolean", false),
        ApiMember(ApiMember.Kind.METHOD, "isCanceled", emptyList(), "boolean", false),
        ApiMember(ApiMember.Kind.METHOD, "getResult", emptyList(), "java.lang.Object", false),
        ApiMember(ApiMember.Kind.METHOD, "getException", emptyList(), "java.lang.Exception", false),
        ApiMember(ApiMember.Kind.METHOD, "addOnSuccessListener", listOf("com.google.android.gms.tasks.OnSuccessListener"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "addOnFailureListener", listOf("com.google.android.gms.tasks.OnFailureListener"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "addOnCompleteListener", listOf("com.google.android.gms.tasks.OnCompleteListener"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "addOnCanceledListener", listOf("com.google.android.gms.tasks.OnCanceledListener"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "continueWith", listOf("com.google.android.gms.tasks.Continuation"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "continueWithTask", listOf("com.google.android.gms.tasks.Continuation"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "onSuccessTask", listOf("com.google.android.gms.tasks.SuccessContinuation"), "com.google.android.gms.tasks.Task", false),
        ApiMember(ApiMember.Kind.METHOD, "getResult", listOf("java.lang.Class"), "java.lang.Object", false, deprecation = TASK_GET_RESULT_CLASS),
    ) + listOf(
        "addOnSuccessListener" to "com.google.android.gms.tasks.OnSuccessListener",
        "addOnFailureListener" to "com.google.android.gms.tasks.OnFailureListener",
        "addOnCompleteListener" to "com.google.android.gms.tasks.OnCompleteListener",
        "addOnCanceledListener" to "com.google.android.gms.tasks.OnCanceledListener",
    ).flatMap { (name, listener) ->
        listOf("java.util.concurrent.Executor", "android.app.Activity").map { scope ->
            ApiMember(ApiMember.Kind.METHOD, name, listOf(scope, listener), "com.google.android.gms.tasks.Task", false, deprecation = TASK_SCOPED_OVERLOADS)
        }
    } + listOf(
        "continueWith" to "com.google.android.gms.tasks.Continuation",
        "continueWithTask" to "com.google.android.gms.tasks.Continuation",
        "onSuccessTask" to "com.google.android.gms.tasks.SuccessContinuation",
    ).map { (name, continuation) ->
        ApiMember(ApiMember.Kind.METHOD, name, listOf("java.util.concurrent.Executor", continuation), "com.google.android.gms.tasks.Task", false, deprecation = TASK_SCOPED_OVERLOADS)
    }


    /**
     * The layer's declaration of [member] on [ourClass]: its own or an inherited one, or, for a static field, the getter of
     * a companion property of the same name (a `val CREATOR` with a getter).
     */
    private fun declaredMember(member: ApiMember, ourClass: ApiClass, oursByName: Map<String, ApiClass>): ApiMember? {
        val matches = { it: ApiMember -> it.kind == member.kind && it.name == member.name && member.parametersMatch(it.parameters) }
        // An unresolved api.txt parameter type matches any of ours: prefer the exact overload, then a deprecated one.
        val preferred = compareByDescending<ApiMember> { it.parameters == member.parameters }.thenByDescending { it.deprecation != null }
        val companion = oursByName["${ourClass.name}\$Companion"]?.members.orEmpty()
        return ourClass.members.filter(matches).sortedWith(preferred).firstOrNull()
            ?: inheritedMembers(ourClass, oursByName).filter(matches).sortedWith(preferred).firstOrNull()
            ?: when {
                // a static field or method provided by the companion: a `val CREATOR` getter, or a function without @JvmStatic
                member.kind == ApiMember.Kind.FIELD && member.isStatic -> companion.firstOrNull { it.kind == ApiMember.Kind.METHOD && it.name == "get${member.name}" && it.parameters.isEmpty() }
                member.kind == ApiMember.Kind.METHOD && member.isStatic -> companion.filter(matches).sortedWith(preferred).firstOrNull()
                else -> null
            }
    }

    /** The members [ourClass] inherits: from its supertypes in [oursByName], or from Task when the chain reaches it. */
    private fun inheritedMembers(ourClass: ApiClass, oursByName: Map<String, ApiClass>): List<ApiMember> = ourClass.superTypes.flatMap { superType ->
        when {
            superType == "com.google.android.gms.tasks.Task" -> taskMembers
            else -> oursByName[superType]?.let { it.members + inheritedMembers(it, oursByName) }.orEmpty()
        }
    }

    private fun classify(member: ApiMember, ourClass: ApiClass, oursByName: Map<String, ApiClass>): Pair<String, String?> {
        if (member.kind == ApiMember.Kind.METHOD && !member.isStatic && ourClass.members.none { it.kind == member.kind && it.name == member.name && member.parametersMatch(it.parameters) }) {
            val inherited = inheritedMembers(ourClass, oursByName).firstOrNull { it.kind == member.kind && it.name == member.name && member.parametersMatch(it.parameters) }
            if (inherited != null) return "MAP" to "inherited"
        }
        // The constructor of an inner class takes the outer instance first in the JVM dump; api.txt does not list it.
        val outerName = ourClass.name.substringBeforeLast('$').replace('$', '.')
        val candidates = ourClass.members
            .map { candidate ->
                if (candidate.kind == ApiMember.Kind.CONSTRUCTOR && ourClass.name.contains('$') && candidate.parameters.firstOrNull() == outerName && candidate.parameters.size == member.parameters.size + 1) {
                    candidate.copy(parameters = candidate.parameters.drop(1))
                } else {
                    candidate
                }
            }
            .filter { it.kind == member.kind && it.name == member.name }
        if (candidates.isEmpty() && member.kind == ApiMember.Kind.FIELD && !member.isStatic) return classifyFieldAsProperty(member, ourClass)
        val match = candidates.firstOrNull { member.parametersMatch(it.parameters) } ?: return "MISS" to null
        val notes = mutableListOf<String>()
        if (member.kind != ApiMember.Kind.CONSTRUCTOR && member.isStatic != match.isStatic) {
            return "MISS" to (if (member.isStatic) "not static in this SDK" else "static in this SDK")
        }
        if (member.type != null && match.type != null && !member.acceptsType(match.type)) {
            notes += "returns ${match.type.substringAfterLast('.')}"
        }
        match.deprecation?.let { notes += "deprecated: $it" }
        return (if (notes.isEmpty()) "OK" else "MAP") to notes.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    /** A public instance field is provided as a Kotlin property: a getter, plus a setter when the field is assignable. */
    private fun classifyFieldAsProperty(field: ApiMember, ourClass: ApiClass): Pair<String, String?> {
        val capitalised = field.name.replaceFirstChar { it.uppercase() }
        val getterNames = if (field.type == "boolean" && field.name.startsWith("is")) setOf(field.name) else setOf("get$capitalised", "is$capitalised")
        val getter = ourClass.members.firstOrNull { it.kind == ApiMember.Kind.METHOD && !it.isStatic && it.name in getterNames && it.parameters.isEmpty() && it.type != null && field.acceptsType(it.type) }
            ?: return "MISS" to null
        if (!field.isFinal) {
            ourClass.members.firstOrNull { it.kind == ApiMember.Kind.METHOD && !it.isStatic && it.name == "set$capitalised" && it.parameters.size == 1 && field.acceptsType(it.parameters.single()) }
                ?: return "MISS" to "read-only property in this SDK"
        }
        return "MAP" to "property ${getter.name}()"
    }

    /**
     * Whether this SDK's parameters [ours] match the Android SDK member's exactly, a type variable (`T`, `ResultT`) matching
     * its erasure in the dump: its bound, or `Object`.
     */
    private fun ApiMember.parametersMatch(ours: List<String>): Boolean =
        ours.size == parameters.size && ours.zip(parameters).all { (ours, theirs) -> ours == theirs || theirs == UNRESOLVED_TYPE || erasure(theirs) == ours }

    /**
     * Whether this SDK's type [ours] provides the Android SDK member's [type]: the same type, or the erasure of a type
     * variable (`T with(...)` of `AbstractOptions<T extends AbstractOptions<T>>` returns `AbstractOptions` in the dump).
     * A type metalava could not resolve when the api.txt was generated (`error.NonExistentClass`) accepts any type.
     */
    private fun ApiMember.acceptsType(ours: String): Boolean = type == ours || type == UNRESOLVED_TYPE || erasure(type!!) == ours

    /** The erasure of [type] when it is a type variable such as `T` or `ResultT`: its bound, or `Object`; null otherwise. */
    private fun ApiMember.erasure(type: String): String? = when {
        type in typeVariableBounds -> typeVariableBounds.getValue(type)
        type.first().isUpperCase() && !type.contains('.') && !type.endsWith("[]") -> "java.lang.Object"
        else -> null
    }

    private const val UNRESOLVED_TYPE = "error.NonExistentClass"

}
