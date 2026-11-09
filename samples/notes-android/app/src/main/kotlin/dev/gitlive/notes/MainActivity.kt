// The Android UI; stays in the app module after the port and keeps using the shared classes as it does here.
package dev.gitlive.notes

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import dev.gitlive.notes.shared.Note
import dev.gitlive.notes.shared.NoteAttachments
import dev.gitlive.notes.shared.NotesBackend
import dev.gitlive.notes.shared.NotesMessaging
import dev.gitlive.notes.shared.NotesRepository
import dev.gitlive.notes.shared.NotesSession
import dev.gitlive.notes.shared.NotesSettings
import dev.gitlive.notes.shared.NotesTelemetry
import dev.gitlive.notes.shared.platform.listenWhile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DateFormat

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val session = NotesSession()
    private val repository = NotesRepository()
    private val attachments = NoteAttachments()
    private val backend = NotesBackend()
    private val settings = NotesSettings()
    private val messaging = NotesMessaging()
    private lateinit var telemetry: NotesTelemetry

    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var list: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        telemetry = NotesTelemetry(applicationContext)
        setContentView(buildLayout())

        scope.launch {
            try {
                val user = telemetry.traced("sign_in") { session.signIn() }
                telemetry.identify(user)
                settings.load()
                input.hint = "A note of up to ${settings.maxNoteLength} characters"
                status.text = "Signed in as ${user.uid.take(8)}, installation ${telemetry.installationId().take(8)}, messaging token ${messaging.token().take(8)}"
            } catch (e: Exception) {
                telemetry.failed("Start-up failed", e)
                status.text = "Start-up failed: ${e.message}"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // The Activity-scoped listener is removed automatically when the activity stops.
        repository.listenWhile(this, ::showNotes) { status.text = "Listening failed: ${it.message}" }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun addNote() {
        val limit = settings.maxNoteLength.toInt().takeIf { it > 0 } ?: Int.MAX_VALUE // 0 until the settings have loaded
        val text = input.text.toString().trim().take(limit)
        if (text.isEmpty()) return
        input.text.clear()
        scope.launch {
            try {
                val document = repository.add(text).await()
                telemetry.noteAdded(text.length)
                val url = attachments.uploadText(document.id, "note.txt", text)
                repository.rename(document.id, text).await()
                status.text = "Added ${document.id} with attachment $url"
            } catch (e: Exception) {
                telemetry.failed("Adding a note failed", e)
                status.text = "Adding failed: ${e.message}"
            }
        }
    }

    private fun callBackend() {
        scope.launch {
            try {
                status.text = "Backend says: ${backend.echo(settings.greeting)}"
            } catch (e: Exception) {
                telemetry.failed("Calling the backend failed", e)
                status.text = "Backend call failed: ${e.message}"
            }
        }
    }

    private fun showNotes(notes: List<Note>) {
        val format = DateFormat.getDateTimeInstance()
        list.text = notes.joinToString("\n\n") { note ->
            // KMP: createdOn returns a kotlin.time.Instant after the port; this Android code formats Date(it.toEpochMilliseconds())
            val created = repository.createdOn(note)?.let(format::format) ?: "saving…"
            "${note.text}\n$created  (${note.id})"
        }
        telemetry.notesListed(notes.size)
    }

    private fun buildLayout(): ScrollView {
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }
        input = EditText(this).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
        status = TextView(this)
        list = TextView(this)
        column.addView(input)
        column.addView(Button(this).apply { text = "Add note"; setOnClickListener { addNote() } })
        column.addView(Button(this).apply { text = "Call backend"; setOnClickListener { callBackend() } })
        column.addView(status)
        column.addView(list)
        return ScrollView(this).apply { addView(column) }
    }
}
