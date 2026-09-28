/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import android.os.Bundle
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.analytics.internal.GET_INSTANCE_ANDROID_ONLY
import dev.gitlive.firebase.analytics.internal.NativeAnalytics
import kotlin.jvm.JvmStatic

/**
 * The entry point of Firebase Analytics, mirroring `com.google.firebase.analytics.FirebaseAnalytics`; obtained with
 * [com.google.firebase.Firebase.analytics][analytics].
 *
 * On Android this class is a header stub for the real one (its bytecode is deleted after compilation); on the other
 * platforms it is real code over the platform SDK. On the JVM it is a no-op, as firebase-java-sdk has no analytics.
 * Constants and enums are shared. The JS SDK has no session timeout, session ID or analytics data reset: those
 * calls do nothing and [getSessionId] completes with null there.
 */
public class FirebaseAnalytics private constructor(internal val native: NativeAnalytics) {

    /** Logs [name] with [params] (see [Event] and [Param] for the recommended names). */
    public fun logEvent(name: String, params: Bundle?): Unit = native.logEvent(name, params)

    /** Parameters added to every event; null clears them. */
    public fun setDefaultEventParameters(parameters: Bundle?): Unit = native.setDefaultEventParameters(parameters)

    /** Sets the user property [name] to [value]; null removes it. */
    public fun setUserProperty(name: String, value: String?): Unit = native.setUserProperty(name, value)

    public fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>): Unit = native.setConsent(consentSettings)

    public fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = native.setAnalyticsCollectionEnabled(enabled)

    public fun setUserId(id: String?): Unit = native.setUserId(id)

    /** The session timeout in [milliseconds]. */
    public fun setSessionTimeoutDuration(milliseconds: Long): Unit = native.setSessionTimeoutDuration(milliseconds)

    public fun getAppInstanceId(): Task<String?> = native.getAppInstanceId()

    /** The app instance ID when it is known synchronously (Android, Apple), else null. */
    @Deprecated("Use getAppInstanceId()", ReplaceWith("getAppInstanceId()"))
    public fun getFirebaseInstanceId(): String? = native.getFirebaseInstanceId()

    public fun getSessionId(): Task<Long?> = native.getSessionId()

    public fun resetAnalyticsData(): Unit = native.resetAnalyticsData()

    override fun equals(other: Any?): Boolean = other is FirebaseAnalytics && other.native == native

    override fun hashCode(): Int = native.hashCode()

    override fun toString(): String = "FirebaseAnalytics"

    /** The recommended event names. */
    public open class Event protected constructor() {
        public companion object {
            public const val ADD_PAYMENT_INFO: String = "add_payment_info"
            public const val ADD_SHIPPING_INFO: String = "add_shipping_info"
            public const val ADD_TO_CART: String = "add_to_cart"
            public const val ADD_TO_WISHLIST: String = "add_to_wishlist"
            public const val AD_IMPRESSION: String = "ad_impression"
            public const val APP_OPEN: String = "app_open"
            public const val BEGIN_CHECKOUT: String = "begin_checkout"
            public const val CAMPAIGN_DETAILS: String = "campaign_details"
            public const val EARN_VIRTUAL_CURRENCY: String = "earn_virtual_currency"
            public const val GENERATE_LEAD: String = "generate_lead"
            public const val IN_APP_PURCHASE: String = "in_app_purchase"
            public const val JOIN_GROUP: String = "join_group"
            public const val LEVEL_END: String = "level_end"
            public const val LEVEL_START: String = "level_start"
            public const val LEVEL_UP: String = "level_up"
            public const val LOGIN: String = "login"
            public const val POST_SCORE: String = "post_score"
            public const val PURCHASE: String = "purchase"
            public const val REFUND: String = "refund"
            public const val REMOVE_FROM_CART: String = "remove_from_cart"
            public const val SCREEN_VIEW: String = "screen_view"
            public const val SEARCH: String = "search"
            public const val SELECT_CONTENT: String = "select_content"
            public const val SELECT_ITEM: String = "select_item"
            public const val SELECT_PROMOTION: String = "select_promotion"
            public const val SHARE: String = "share"
            public const val SIGN_UP: String = "sign_up"
            public const val SPEND_VIRTUAL_CURRENCY: String = "spend_virtual_currency"
            public const val TUTORIAL_BEGIN: String = "tutorial_begin"
            public const val TUTORIAL_COMPLETE: String = "tutorial_complete"
            public const val UNLOCK_ACHIEVEMENT: String = "unlock_achievement"
            public const val VIEW_CART: String = "view_cart"
            public const val VIEW_ITEM: String = "view_item"
            public const val VIEW_ITEM_LIST: String = "view_item_list"
            public const val VIEW_PROMOTION: String = "view_promotion"
            public const val VIEW_SEARCH_RESULTS: String = "view_search_results"
        }
    }

    /** The recommended parameter names. */
    public open class Param protected constructor() {
        public companion object {
            public const val ACHIEVEMENT_ID: String = "achievement_id"
            public const val ACLID: String = "aclid"
            public const val AD_FORMAT: String = "ad_format"
            public const val AD_PLATFORM: String = "ad_platform"
            public const val AD_SOURCE: String = "ad_source"
            public const val AD_UNIT_NAME: String = "ad_unit_name"
            public const val AFFILIATION: String = "affiliation"
            public const val CAMPAIGN: String = "campaign"
            public const val CAMPAIGN_ID: String = "campaign_id"
            public const val CHARACTER: String = "character"
            public const val CONTENT: String = "content"
            public const val CONTENT_TYPE: String = "content_type"
            public const val COUPON: String = "coupon"
            public const val CP1: String = "cp1"
            public const val CREATIVE_FORMAT: String = "creative_format"
            public const val CREATIVE_NAME: String = "creative_name"
            public const val CREATIVE_SLOT: String = "creative_slot"
            public const val CURRENCY: String = "currency"
            public const val DESTINATION: String = "destination"
            public const val DISCOUNT: String = "discount"
            public const val END_DATE: String = "end_date"
            public const val EXTEND_SESSION: String = "extend_session"
            public const val FLIGHT_NUMBER: String = "flight_number"
            public const val FREE_TRIAL: String = "free_trial"
            public const val GROUP_ID: String = "group_id"
            public const val INDEX: String = "index"
            public const val ITEMS: String = "items"
            public const val ITEM_BRAND: String = "item_brand"
            public const val ITEM_CATEGORY: String = "item_category"
            public const val ITEM_CATEGORY2: String = "item_category2"
            public const val ITEM_CATEGORY3: String = "item_category3"
            public const val ITEM_CATEGORY4: String = "item_category4"
            public const val ITEM_CATEGORY5: String = "item_category5"
            public const val ITEM_ID: String = "item_id"
            public const val ITEM_LIST_ID: String = "item_list_id"
            public const val ITEM_LIST_NAME: String = "item_list_name"
            public const val ITEM_NAME: String = "item_name"
            public const val ITEM_VARIANT: String = "item_variant"
            public const val LEVEL: String = "level"
            public const val LEVEL_NAME: String = "level_name"
            public const val LOCATION: String = "location"
            public const val LOCATION_ID: String = "location_id"
            public const val MARKETING_TACTIC: String = "marketing_tactic"
            public const val MEDIUM: String = "medium"
            public const val METHOD: String = "method"
            public const val NUMBER_OF_NIGHTS: String = "number_of_nights"
            public const val NUMBER_OF_PASSENGERS: String = "number_of_passengers"
            public const val NUMBER_OF_ROOMS: String = "number_of_rooms"
            public const val ORIGIN: String = "origin"
            public const val PAYMENT_TYPE: String = "payment_type"
            public const val PRICE: String = "price"
            public const val PRICE_IS_DISCOUNTED: String = "price_is_discounted"
            public const val PRODUCT_ID: String = "product_id"
            public const val PRODUCT_NAME: String = "product_name"
            public const val PROMOTION_ID: String = "promotion_id"
            public const val PROMOTION_NAME: String = "promotion_name"
            public const val QUANTITY: String = "quantity"
            public const val SCORE: String = "score"
            public const val SCREEN_CLASS: String = "screen_class"
            public const val SCREEN_NAME: String = "screen_name"
            public const val SEARCH_TERM: String = "search_term"
            public const val SHIPPING: String = "shipping"
            public const val SHIPPING_TIER: String = "shipping_tier"
            public const val SOURCE: String = "source"
            public const val SOURCE_PLATFORM: String = "source_platform"
            public const val START_DATE: String = "start_date"
            public const val SUBSCRIPTION: String = "subscription"
            public const val SUCCESS: String = "success"
            public const val TAX: String = "tax"
            public const val TERM: String = "term"
            public const val TRANSACTION_ID: String = "transaction_id"
            public const val TRAVEL_CLASS: String = "travel_class"
            public const val VALUE: String = "value"
            public const val VIRTUAL_CURRENCY_NAME: String = "virtual_currency_name"
        }
    }

    /** The recommended user property names. */
    public open class UserProperty protected constructor() {
        public companion object {
            public const val ALLOW_AD_PERSONALIZATION_SIGNALS: String = "allow_personalized_ads"
            public const val SIGN_UP_METHOD: String = "sign_up_method"
        }
    }

    public enum class ConsentType {
        AD_STORAGE,
        ANALYTICS_STORAGE,
        AD_USER_DATA,
        AD_PERSONALIZATION,
    }

    public enum class ConsentStatus {
        GRANTED,
        DENIED,
    }

    public companion object {
        /** Android only: use [com.google.firebase.Firebase.analytics][analytics] from common code. */
        @JvmStatic
        @Deprecated(GET_INSTANCE_ANDROID_ONLY, ReplaceWith("Firebase.analytics", "com.google.firebase.Firebase", "com.google.firebase.analytics.analytics"), DeprecationLevel.ERROR)
        public fun getInstance(context: Any?): FirebaseAnalytics = throw UnsupportedOperationException(GET_INSTANCE_ANDROID_ONLY)

        internal fun create(native: NativeAnalytics): FirebaseAnalytics = FirebaseAnalytics(native)
    }
}
