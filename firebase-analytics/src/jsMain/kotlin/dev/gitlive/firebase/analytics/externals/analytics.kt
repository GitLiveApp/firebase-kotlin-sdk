@file:Suppress("ktlint:standard:property-naming", "PropertyName")
@file:JsModule("firebase/analytics")
@file:JsNonModule

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(analytics: FirebaseAnalytics, name: String, parameters: Json?)
public external fun setUserProperties(analytics: FirebaseAnalytics, properties: Json)
public external fun setUserId(analytics: FirebaseAnalytics, id: String?)
public external fun setDefaultEventParameters(parameters: Json?)
public external fun setAnalyticsCollectionEnabled(analytics: FirebaseAnalytics, enabled: Boolean)
public external fun getGoogleAnalyticsClientId(analytics: FirebaseAnalytics): Promise<String>
public external fun setConsent(consentSettings: ConsentSettings)

public external interface FirebaseAnalytics {
    public val app: FirebaseApp
}

public external interface ConsentSettings {
    public var ad_personalization: String?
    public var ad_storage: String?
    public var ad_user_data: String?
    public var analytics_storage: String?
    public var functionality_storage: String?
    public var personalization_storage: String?
    public var security_storage: String?
}
