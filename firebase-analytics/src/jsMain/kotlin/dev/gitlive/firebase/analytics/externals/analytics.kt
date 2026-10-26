@file:Suppress("ktlint:standard:property-naming", "PropertyName")
@file:JsModule("firebase/analytics")
@file:JsNonModule

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json

public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(app: FirebaseAnalytics, name: String, parameters: Json?)
public external fun setUserProperties(app: FirebaseAnalytics, properties: Json)
public external fun setUserId(app: FirebaseAnalytics, id: String?)
public external fun setDefaultEventParameters(parameters: Json)
public external fun setAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
public external fun setConsent(consentSettings: Json)

public external interface FirebaseAnalytics
