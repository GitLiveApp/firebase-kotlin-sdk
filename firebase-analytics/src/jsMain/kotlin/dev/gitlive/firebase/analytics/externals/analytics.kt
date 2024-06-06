package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise


external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

external fun jsLogEvent(app: FirebaseAnalytics, name: String, parameters: Map<String, String>)
external fun jsSetUserProperty(app: FirebaseAnalytics, name: String, value: String)
external fun jsSetUserId(app: FirebaseAnalytics, id: String)
external fun jsResetAnalyticsData(app: FirebaseAnalytics)
external fun jsSetDefaultEventParameters(app: FirebaseAnalytics, parameters: Map<String, String>)
external fun jsSetAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
external fun jsSetSessionTimeoutInterval(app: FirebaseAnalytics, sessionTimeoutInterval: Long)
external fun jsGetSessionId(app: FirebaseAnalytics): Promise<Long?>

external interface FirebaseAnalytics