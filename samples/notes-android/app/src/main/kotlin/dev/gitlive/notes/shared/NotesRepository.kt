// KMP: this file moves to commonMain; only the lines marked change.
package dev.gitlive.notes.shared

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date // KMP: kotlin.time.Instant

/** The notes of the signed-in user in Cloud Firestore, under `users/{uid}/notes`. */
class NotesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    val notes: CollectionReference
        get() = firestore.collection("users").document(auth.currentUser?.uid ?: "anonymous").collection("notes")

    /** Adds a note; the Android SDK maps the class by reflection. */
    fun add(text: String): Task<DocumentReference> = notes.add(Note(text = text)) // KMP: notes.addData(Note(text = text)), from com.google.firebase.serialization

    /** Replaces a note. */
    fun save(note: Note): Task<Void> = notes.document(note.id).set(note) // KMP: Task<Nothing?> (the common spelling of Task<Void>) and notes.document(note.id).setData(note)

    /** Updates two fields by name; the field-and-value overload takes plain values, so it is not reflective. */
    fun rename(id: String, text: String) = notes.document(id).update("note_text", text, "updated_at", FieldValue.serverTimestamp())

    fun delete(id: String) = notes.document(id).delete()

    suspend fun load(id: String): Note? = notes.document(id).get().await().toObject(Note::class.java) // KMP: .toObject<Note>(), from com.google.firebase.serialization (Note::class.java is JVM-only)

    /** Streams the notes, newest first; see [NotesRepository.listenWhile] for the Activity-scoped variant. */
    fun listen(onNotes: (List<Note>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        notes.orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) onError(error) else onNotes(snapshot!!.toObjects(Note::class.java)) // KMP: snapshot!!.toObjects<Note>()
        }

    fun createdOn(note: Note): Date? = note.createdAt?.toDate() // KMP: Instant? and note.createdAt?.toKotlinInstant()
}
