// Android-only: the Activity-taking overload of addSnapshotListener, which stops the listener when the activity stops,
// involves android.app.Activity, so the Firebase Kotlin SDK does not declare it (listed as @platform in
// firebase-firestore's api/android-sdk/exclusions.txt). The other overloads are available in common code.
// KMP: this file moves to shared/src/androidMain, as only Android UI code can call it; or drop it and have the activity
// call NotesRepository.listen() in onStart and ListenerRegistration.remove() in onStop. The read changes as marked even
// though the file stays on Android: once Note carries the serialization annotations, the Android SDK's reflective
// toObjects(Class) no longer knows the renamed field or the document id.
package dev.gitlive.notes.shared.platform

import android.app.Activity
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dev.gitlive.notes.shared.Note
import dev.gitlive.notes.shared.NotesRepository

/** Streams the notes, newest first, until [activity] stops. */
fun NotesRepository.listenWhile(activity: Activity, onNotes: (List<Note>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
    notes.orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener(activity) { snapshot, error ->
        if (error != null) onError(error) else onNotes(snapshot!!.toObjects(Note::class.java)) // KMP: snapshot!!.toObjects<Note>(), from com.google.firebase.serialization
    }
