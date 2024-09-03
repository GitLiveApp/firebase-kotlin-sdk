package dev.gitlive.firebase.perf.session

import com.google.firebase.perf.session.PerfSession as AndroidPerfSession

public val PerfSession.android: AndroidPerfSession get() = android

public class PerfSession internal constructor(internal val android: AndroidPerfSession)
