package dev.gitlive.firebase.perf.session

import com.google.firebase.perf.session.PerfSession

public val PerfSession.android: PerfSession get() = android

public class PerfSession internal constructor(internal val android: PerfSession)
