/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import android.content.Context
import android.os.Bundle
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.analytics.internal.NativeAnalytics
import dev.gitlive.firebase.analytics.internal.compatAnalytics

/** Real code over the platform SDK through [native]; see the common declaration. */
public actual class FirebaseAnalytics private constructor(internal actual val native: NativeAnalytics) {
    public actual fun logEvent(name: String, params: Bundle?): Unit = native.logEvent(name, params)

    public actual fun setDefaultEventParameters(parameters: Bundle?): Unit = native.setDefaultEventParameters(parameters)

    public actual fun setUserProperty(name: String, value: String?): Unit = native.setUserProperty(name, value)

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>): Unit = native.setConsent(consentSettings)

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = native.setAnalyticsCollectionEnabled(enabled)

    public actual fun setUserId(id: String?): Unit = native.setUserId(id)

    /** The session timeout in [milliseconds]. */
    public actual fun setSessionTimeoutDuration(milliseconds: Long): Unit = native.setSessionTimeoutDuration(milliseconds)

    public actual fun getAppInstanceId(): Task<String?> = native.getAppInstanceId()

    @Deprecated("Use getAppInstanceId()", ReplaceWith("getAppInstanceId()"))
    public actual fun getFirebaseInstanceId(): String? = native.getFirebaseInstanceId()

    public actual fun getSessionId(): Task<Long?> = native.getSessionId()

    public actual fun resetAnalyticsData(): Unit = native.resetAnalyticsData()

    override fun equals(other: Any?): Boolean = other is FirebaseAnalytics && other.native == native

    override fun hashCode(): Int = native.hashCode()

    override fun toString(): String = "FirebaseAnalytics"

    /** The recommended event names. */
    public actual open class Event protected actual constructor() {
        public actual companion object {
            public actual val ADD_PAYMENT_INFO: String = "add_payment_info"
            public actual val ADD_SHIPPING_INFO: String = "add_shipping_info"
            public actual val ADD_TO_CART: String = "add_to_cart"
            public actual val ADD_TO_WISHLIST: String = "add_to_wishlist"
            public actual val AD_IMPRESSION: String = "ad_impression"
            public actual val APP_OPEN: String = "app_open"
            public actual val BEGIN_CHECKOUT: String = "begin_checkout"
            public actual val CAMPAIGN_DETAILS: String = "campaign_details"
            public actual val EARN_VIRTUAL_CURRENCY: String = "earn_virtual_currency"
            public actual val GENERATE_LEAD: String = "generate_lead"
            public actual val IN_APP_PURCHASE: String = "in_app_purchase"
            public actual val JOIN_GROUP: String = "join_group"
            public actual val LEVEL_END: String = "level_end"
            public actual val LEVEL_START: String = "level_start"
            public actual val LEVEL_UP: String = "level_up"
            public actual val LOGIN: String = "login"
            public actual val POST_SCORE: String = "post_score"
            public actual val PURCHASE: String = "purchase"
            public actual val REFUND: String = "refund"
            public actual val REMOVE_FROM_CART: String = "remove_from_cart"
            public actual val SCREEN_VIEW: String = "screen_view"
            public actual val SEARCH: String = "search"
            public actual val SELECT_CONTENT: String = "select_content"
            public actual val SELECT_ITEM: String = "select_item"
            public actual val SELECT_PROMOTION: String = "select_promotion"
            public actual val SHARE: String = "share"
            public actual val SIGN_UP: String = "sign_up"
            public actual val SPEND_VIRTUAL_CURRENCY: String = "spend_virtual_currency"
            public actual val TUTORIAL_BEGIN: String = "tutorial_begin"
            public actual val TUTORIAL_COMPLETE: String = "tutorial_complete"
            public actual val UNLOCK_ACHIEVEMENT: String = "unlock_achievement"
            public actual val VIEW_CART: String = "view_cart"
            public actual val VIEW_ITEM: String = "view_item"
            public actual val VIEW_ITEM_LIST: String = "view_item_list"
            public actual val VIEW_PROMOTION: String = "view_promotion"
            public actual val VIEW_SEARCH_RESULTS: String = "view_search_results"
        }
    }

    /** The recommended parameter names. */
    public actual open class Param protected actual constructor() {
        public actual companion object {
            public actual val ACHIEVEMENT_ID: String = "achievement_id"
            public actual val ACLID: String = "aclid"
            public actual val AD_FORMAT: String = "ad_format"
            public actual val AD_PLATFORM: String = "ad_platform"
            public actual val AD_SOURCE: String = "ad_source"
            public actual val AD_UNIT_NAME: String = "ad_unit_name"
            public actual val AFFILIATION: String = "affiliation"
            public actual val CAMPAIGN: String = "campaign"
            public actual val CAMPAIGN_ID: String = "campaign_id"
            public actual val CHARACTER: String = "character"
            public actual val CONTENT: String = "content"
            public actual val CONTENT_TYPE: String = "content_type"
            public actual val COUPON: String = "coupon"
            public actual val CP1: String = "cp1"
            public actual val CREATIVE_FORMAT: String = "creative_format"
            public actual val CREATIVE_NAME: String = "creative_name"
            public actual val CREATIVE_SLOT: String = "creative_slot"
            public actual val CURRENCY: String = "currency"
            public actual val DESTINATION: String = "destination"
            public actual val DISCOUNT: String = "discount"
            public actual val END_DATE: String = "end_date"
            public actual val EXTEND_SESSION: String = "extend_session"
            public actual val FLIGHT_NUMBER: String = "flight_number"
            public actual val FREE_TRIAL: String = "free_trial"
            public actual val GROUP_ID: String = "group_id"
            public actual val INDEX: String = "index"
            public actual val ITEMS: String = "items"
            public actual val ITEM_BRAND: String = "item_brand"
            public actual val ITEM_CATEGORY: String = "item_category"
            public actual val ITEM_CATEGORY2: String = "item_category2"
            public actual val ITEM_CATEGORY3: String = "item_category3"
            public actual val ITEM_CATEGORY4: String = "item_category4"
            public actual val ITEM_CATEGORY5: String = "item_category5"
            public actual val ITEM_ID: String = "item_id"
            public actual val ITEM_LIST_ID: String = "item_list_id"
            public actual val ITEM_LIST_NAME: String = "item_list_name"
            public actual val ITEM_NAME: String = "item_name"
            public actual val ITEM_VARIANT: String = "item_variant"
            public actual val LEVEL: String = "level"
            public actual val LEVEL_NAME: String = "level_name"
            public actual val LOCATION: String = "location"
            public actual val LOCATION_ID: String = "location_id"
            public actual val MARKETING_TACTIC: String = "marketing_tactic"
            public actual val MEDIUM: String = "medium"
            public actual val METHOD: String = "method"
            public actual val NUMBER_OF_NIGHTS: String = "number_of_nights"
            public actual val NUMBER_OF_PASSENGERS: String = "number_of_passengers"
            public actual val NUMBER_OF_ROOMS: String = "number_of_rooms"
            public actual val ORIGIN: String = "origin"
            public actual val PAYMENT_TYPE: String = "payment_type"
            public actual val PRICE: String = "price"
            public actual val PRICE_IS_DISCOUNTED: String = "price_is_discounted"
            public actual val PRODUCT_ID: String = "product_id"
            public actual val PRODUCT_NAME: String = "product_name"
            public actual val PROMOTION_ID: String = "promotion_id"
            public actual val PROMOTION_NAME: String = "promotion_name"
            public actual val QUANTITY: String = "quantity"
            public actual val SCORE: String = "score"
            public actual val SCREEN_CLASS: String = "screen_class"
            public actual val SCREEN_NAME: String = "screen_name"
            public actual val SEARCH_TERM: String = "search_term"
            public actual val SHIPPING: String = "shipping"
            public actual val SHIPPING_TIER: String = "shipping_tier"
            public actual val SOURCE: String = "source"
            public actual val SOURCE_PLATFORM: String = "source_platform"
            public actual val START_DATE: String = "start_date"
            public actual val SUBSCRIPTION: String = "subscription"
            public actual val SUCCESS: String = "success"
            public actual val TAX: String = "tax"
            public actual val TERM: String = "term"
            public actual val TRANSACTION_ID: String = "transaction_id"
            public actual val TRAVEL_CLASS: String = "travel_class"
            public actual val VALUE: String = "value"
            public actual val VIRTUAL_CURRENCY_NAME: String = "virtual_currency_name"
        }
    }

    /** The recommended user property names. */
    public actual open class UserProperty protected actual constructor() {
        public actual companion object {
            public actual val ALLOW_AD_PERSONALIZATION_SIGNALS: String = "allow_personalized_ads"
            public actual val SIGN_UP_METHOD: String = "sign_up_method"
        }
    }
    public actual enum class ConsentType {
        AD_STORAGE,
        ANALYTICS_STORAGE,
        AD_USER_DATA,
        AD_PERSONALIZATION,
    }

    public actual enum class ConsentStatus {
        GRANTED,
        DENIED,
    }

    public actual companion object {
        public actual fun getInstance(context: Context): FirebaseAnalytics = compatAnalytics(null)

        internal actual fun create(native: NativeAnalytics): FirebaseAnalytics = FirebaseAnalytics(native)
    }
}
