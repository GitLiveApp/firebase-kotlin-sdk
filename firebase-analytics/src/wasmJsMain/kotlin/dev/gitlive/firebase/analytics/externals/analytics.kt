@file:JsModule("firebase/analytics")

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(analytics: FirebaseAnalytics, name: String, parameters: JsAny?)
public external fun setUserProperties(analytics: FirebaseAnalytics, properties: JsAny)
public external fun setUserId(analytics: FirebaseAnalytics, id: String?)
public external fun setDefaultEventParameters(parameters: JsAny?)
public external fun setAnalyticsCollectionEnabled(analytics: FirebaseAnalytics, enabled: Boolean)
public external fun getGoogleAnalyticsClientId(analytics: FirebaseAnalytics): Promise<JsString>
public external fun setConsent(consentSettings: JsAny)

public external interface FirebaseAnalytics : JsAny
