package relocate

import org.gradle.api.provider.Property

/** Configuration for a project that republishes one Firebase Android SDK artifact under a relocated package. */
abstract class RelocatedArtifactExtension {
    /** Original coordinates, `group:artifact` (version taken from the Firebase BoM) or `group:artifact:version`. */
    abstract val original: Property<String>

    /** For AARs: the android namespace (manifest `package`) of the original artifact; read from the manifest when unset. */
    abstract val namespace: Property<String>

    /**
     * For artifacts outside the Firebase group that nevertheless contain `com.google.firebase` classes
     * (e.g. `play-services-basement` ships `com.google.firebase.FirebaseException`): keep the original artifact as a
     * dependency and publish only its relocated `com.google.firebase` classes. Defaults to false.
     */
    abstract val extractRelocatedClassesOnly: Property<Boolean>
}
