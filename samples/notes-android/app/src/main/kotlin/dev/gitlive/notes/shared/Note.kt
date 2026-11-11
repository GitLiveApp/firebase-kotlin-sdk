// KMP: this file moves to commonMain. The Android SDK maps the class by reflection, which only exists on Android and the
// JVM; the Firebase Kotlin SDK maps a @Serializable class with kotlinx.serialization on every platform, so the mapping
// annotations change as marked (each Android one is deprecated with a ReplaceWith towards its replacement).
package dev.gitlive.notes.shared

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId // KMP: com.google.firebase.serialization.DocumentId
import com.google.firebase.firestore.Exclude // KMP: kotlinx.serialization.Transient
import com.google.firebase.firestore.PropertyName // KMP: kotlinx.serialization.SerialName
import com.google.firebase.firestore.ServerTimestamp // KMP: com.google.firebase.serialization.ServerTimestamp

// KMP: @Serializable (kotlinx.serialization), with the serialization Gradle plugin on the shared module
data class Note(
    @get:DocumentId var id: String = "", // KMP: @DocumentId (no use-site target: the annotation applies to the property)
    @get:PropertyName("note_text") @set:PropertyName("note_text") var text: String = "", // KMP: @SerialName("note_text")
    @get:ServerTimestamp var createdAt: Timestamp? = null, // KMP: @ServerTimestamp @Contextual (Timestamp needs the contextual serializer)
    @get:Exclude @set:Exclude var isDraft: Boolean = false, // KMP: @Transient (which needs the default value the property already has)
)
