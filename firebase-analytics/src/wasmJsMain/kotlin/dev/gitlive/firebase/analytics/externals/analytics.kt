@file:JsModule("firebase/analytics")

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(app: FirebaseAnalytics, name: String, parameters: JsAny?)
public external fun setUserProperty(app: FirebaseAnalytics, name: String, value: String)
public external fun setUserId(app: FirebaseAnalytics, id: String?)
public external fun resetAnalyticsData(app: FirebaseAnalytics)
public external fun setDefaultEventParameters(app: FirebaseAnalytics, parameters: JsAny)
public external fun setAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
public external fun setSessionTimeoutInterval(app: FirebaseAnalytics, sessionTimeoutInterval: Double)
public external fun getSessionId(app: FirebaseAnalytics): Promise<JsNumber?>
public external fun setConsent(app: FirebaseAnalytics, consentSettings: JsAny)

public external interface FirebaseAnalytics : JsAny
