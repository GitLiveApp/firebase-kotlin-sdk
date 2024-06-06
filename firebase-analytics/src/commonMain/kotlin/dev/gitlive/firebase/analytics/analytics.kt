package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase

expect val Firebase.analytics: FirebaseAnalytics

expect class FirebaseAnalytics {
    fun logEvent(name: String, parameters: Map<String, String>)
    fun logEvent(name: String, block: FirebaseAnalyticsParameters.() -> Unit)
    fun setUserProperty(name: String, value: String)
    fun setUserId(id: String)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setSessionTimeoutInterval(sessionTimeoutInterval: Long)
    suspend fun getSessionId(): Long?
    fun resetAnalyticsData()
    fun setDefaultEventParameters(parameters: Map<String, String>)
}

expect class FirebaseAnalyticsException

data class FirebaseAnalyticsParameters(
    val parameters: MutableMap<String, String> = mutableMapOf()
) {
    fun param(key: String, value: String) {
        parameters[key] = value
    }
}

object FirebaseAnalyticsEvents {
    const val ADD_PAYMENT_INFO: String = "add_payment_info"
    const val ADD_SHIPPING_INFO: String = "add_shipping_info"
    const val ADD_TO_CART: String = "add_to_cart"
    const val ADD_TO_WISHLIST: String = "add_to_wishlist"
    const val AD_IMPRESSION: String = "ad_impression"
    const val APP_OPEN: String = "app_open"
    const val BEGIN_CHECKOUT: String = "begin_checkout"
    const val CAMPAIGN_DETAILS: String = "campaign_details"
    const val EARN_VIRTUAL_CURRENCY: String = "earn_virtual_currency"
    const val GENERATE_LEAD: String = "generate_lead"
    const val JOIN_GROUP: String = "join_group"
    const val LEVEL_END: String = "level_end"
    const val LEVEL_START: String = "level_start"
    const val LEVEL_UP: String = "level_up"
    const val LOGIN: String = "login"
    const val POST_SCORE: String = "post_score"
    const val PURCHASE: String = "purchase"
    const val REFUND: String = "refund"
    const val REMOVE_FROM_CART: String = "remove_from_cart"
    const val SCREEN_VIEW: String = "screen_view"
    const val SEARCH: String = "search"
    const val SELECT_CONTENT: String = "select_content"
    const val SELECT_ITEM: String = "select_item"
    const val SELECT_PROMOTION: String = "select_promotion"
    const val SHARE: String = "share"
    const val SIGN_UP: String = "sign_up"
    const val SPEND_VIRTUAL_CURRENCY: String = "spend_virtual_currency"
    const val TUTORIAL_BEGIN: String = "tutorial_begin"
    const val TUTORIAL_COMPLETE: String = "tutorial_complete"
    const val UNLOCK_ACHIEVEMENT: String = "unlock_achievement"
    const val VIEW_CART: String = "view_cart"
    const val VIEW_ITEM: String = "view_item"
    const val VIEW_ITEM_LIST: String = "view_item_list"
    const val VIEW_PROMOTION: String = "view_promotion"
    const val VIEW_SEARCH_RESULTS: String = "view_search_results"
}