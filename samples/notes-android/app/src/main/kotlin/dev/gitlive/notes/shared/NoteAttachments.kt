// KMP: this file moves to commonMain; only the stream upload changes.
package dev.gitlive.notes.shared

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import java.io.InputStream // KMP: kotlinx.io.RawSource

/** Files attached to notes, in Cloud Storage under `users/{uid}/notes/{noteId}/`. */
class NoteAttachments(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private fun reference(noteId: String, name: String): StorageReference =
        storage.reference.child("users/${auth.currentUser?.uid ?: "anonymous"}/notes/$noteId/$name")

    /** Uploads text as a file and returns its download URL. */
    suspend fun uploadText(noteId: String, name: String, text: String): String {
        val reference = reference(noteId, name)
        reference.putBytes(text.encodeToByteArray()).await()
        return reference.getDownloadUrl().await().toString()
    }

    /** Uploads a stream; `putStream(InputStream)` is JVM-only and deprecated with an error in common code. */
    fun upload(noteId: String, name: String, stream: InputStream): UploadTask = // KMP: source: RawSource (Android callers pass stream.asSource())
        reference(noteId, name).putStream(stream) // KMP: putStream(source), from com.google.firebase.storage

    suspend fun download(noteId: String, name: String): ByteArray = reference(noteId, name).getBytes(ONE_MEGABYTE).await()

    private companion object {
        const val ONE_MEGABYTE = 1024L * 1024
    }
}
