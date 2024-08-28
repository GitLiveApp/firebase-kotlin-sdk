package dev.gitlive.firebase.perf.session

public val PerfSession.android: com.google.firebase.perf.session.PerfSession get() = android

public class PerfSession internal constructor(internal val android: com.google.firebase.perf.session.PerfSession)
