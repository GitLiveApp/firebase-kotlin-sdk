package dev.gitlive.firebase.perf.session

import dev.gitlive.firebase.android.perf.session.PerfSession as AndroidPerfSession

public val PerfSession.android: AndroidPerfSession get() = android

public class PerfSession internal constructor(internal val android: AndroidPerfSession)
