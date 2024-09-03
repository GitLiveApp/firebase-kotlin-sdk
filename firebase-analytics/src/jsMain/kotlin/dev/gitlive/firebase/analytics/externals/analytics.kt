@file:Suppress("ktlint:standard:property-naming", "PropertyName")
@file:JsModule("firebase/analytics")
@file:JsNonModule

package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

public external fun logEvent(app: FirebaseAnalytics, name: String, parameters: Map<String, Any>?)
public external fun setUserProperty(app: FirebaseAnalytics, name: String, value: String)
public external fun setUserId(app: FirebaseAnalytics, id: String?)
public external fun resetAnalyticsData(app: FirebaseAnalytics)
public external fun setDefaultEventParameters(app: FirebaseAnalytics, parameters: Map<String, String>)
public external fun setAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
public external fun setSessionTimeoutInterval(app: FirebaseAnalytics, sessionTimeoutInterval: Long)
public external fun getSessionId(app: FirebaseAnalytics): Promise<Long?>
public external fun setConsent(app: FirebaseAnalytics, consentSettings: ConsentSettings)

public external interface FirebaseAnalytics

public external class ConsentSettings {
    public var ad_personalization: String?
    public var ad_storage: String?
    public var ad_user_data: String?
    public var analytics_storage: String?
    public var functionality_storage: String?
    public var personalization_storage: String?
    public var security_storage: String?
}
