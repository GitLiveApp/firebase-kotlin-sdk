@file:JsModule("firebase/analytics")

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(app: FirebaseAnalytics, name: String, parameters: JsAny?)
public external fun setUserProperties(app: FirebaseAnalytics, properties: JsAny)
public external fun setUserId(app: FirebaseAnalytics, id: String?)
public external fun setDefaultEventParameters(parameters: JsAny)
public external fun setAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
public external fun setConsent(consentSettings: JsAny)

public external interface FirebaseAnalytics : JsAny
