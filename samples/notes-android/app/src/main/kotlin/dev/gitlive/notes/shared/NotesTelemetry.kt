// KMP: this file moves to commonMain unchanged. android.os.Bundle (the subset analytics events use) and
// android.content.Context are provided in common code, and so are the Analytics, Crashlytics, Performance and
// Installations APIs used here.
package dev.gitlive.notes.shared

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.tasks.await

/** Analytics events, crash reports and performance traces. */
class NotesTelemetry(context: Context) {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val performance = FirebasePerformance.getInstance()

    fun identify(user: FirebaseUser) {
        analytics.setUserId(user.uid)
        analytics.setUserProperty("anonymous", user.isAnonymous.toString())
        crashlytics.setUserId(user.uid)
    }

    fun noteAdded(length: Int) {
        analytics.logEvent(
            "note_added",
            Bundle().apply {
                putLong("length", length.toLong())
            },
        )
    }

    fun notesListed(count: Int) {
        analytics.logEvent(
            FirebaseAnalytics.Event.VIEW_ITEM_LIST,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, "notes")
                putLong("count", count.toLong())
            },
        )
    }

    fun failed(what: String, error: Exception) {
        crashlytics.log(what)
        crashlytics.setCustomKey("last_failure", what)
        crashlytics.recordException(error)
    }

    /** Runs [block] inside a performance trace named [name]. */
    suspend fun <T> traced(name: String, block: suspend () -> T): T {
        val trace = performance.newTrace(name)
        trace.start()
        try {
            return block()
        } finally {
            trace.stop()
        }
    }

    suspend fun installationId(): String = FirebaseInstallations.getInstance().getId().await()
}
