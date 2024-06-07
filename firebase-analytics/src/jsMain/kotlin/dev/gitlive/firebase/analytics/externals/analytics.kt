package dev.gitlive.firebase.analytics.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise


external fun getAnalytics(app: FirebaseApp? = definedExternally): FirebaseAnalytics

external fun logEvent(app: FirebaseAnalytics, name: String, parameters: Map<String, Any>?)
external fun setUserProperty(app: FirebaseAnalytics, name: String, value: String)
external fun setUserId(app: FirebaseAnalytics, id: String)
external fun resetAnalyticsData(app: FirebaseAnalytics)
external fun setDefaultEventParameters(app: FirebaseAnalytics, parameters: Map<String, String>)
external fun setAnalyticsCollectionEnabled(app: FirebaseAnalytics, enabled: Boolean)
external fun setSessionTimeoutInterval(app: FirebaseAnalytics, sessionTimeoutInterval: Long)
external fun getSessionId(app: FirebaseAnalytics): Promise<Long?>
external fun setConsent(app: FirebaseAnalytics, consentSettings: ConsentSettings)

external interface FirebaseAnalytics

external class ConsentSettings() {
    var ad_personalization: String?
    var ad_storage: String?
    var ad_user_data: String?
    var analytics_storage: String?
    var functionality_storage: String?
    var personalization_storage: String?
    var security_storage: String?
}
