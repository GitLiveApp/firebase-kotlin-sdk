/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import android.content.Context
import android.os.Bundle
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.analytics.internal.NativeAnalytics
import dev.gitlive.firebase.analytics.stub
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/*
 * Header stub for com.google.firebase:firebase-analytics (see buildSrc utils/HeaderStubs.kt): compiled against, verified
 * to match the real class, and deleted from the output so the real SDK binds at runtime.
 */
public actual class FirebaseAnalytics private constructor() {
    internal actual val native: NativeAnalytics get() = stub()

    public actual fun logEvent(name: String, params: Bundle?): Unit = stub()
    public actual fun setDefaultEventParameters(parameters: Bundle?): Unit = stub()
    public actual fun setUserProperty(name: String, value: String?): Unit = stub()
    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>): Unit = stub()
    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = stub()
    public actual fun setUserId(id: String?): Unit = stub()
    public actual fun setSessionTimeoutDuration(milliseconds: Long): Unit = stub()
    public actual fun getAppInstanceId(): Task<String?> = stub()

    @Deprecated("Use getAppInstanceId()", ReplaceWith("getAppInstanceId()"))
    public actual fun getFirebaseInstanceId(): String? = stub()
    public actual fun getSessionId(): Task<Long?> = stub()
    public actual fun resetAnalyticsData(): Unit = stub()

    /** The recommended event names. */
    public actual open class Event protected actual constructor() {
        public actual companion object {
            @JvmField public actual val ADD_PAYMENT_INFO: String = "add_payment_info"

            @JvmField public actual val ADD_SHIPPING_INFO: String = "add_shipping_info"

            @JvmField public actual val ADD_TO_CART: String = "add_to_cart"

            @JvmField public actual val ADD_TO_WISHLIST: String = "add_to_wishlist"

            @JvmField public actual val AD_IMPRESSION: String = "ad_impression"

            @JvmField public actual val APP_OPEN: String = "app_open"

            @JvmField public actual val BEGIN_CHECKOUT: String = "begin_checkout"

            @JvmField public actual val CAMPAIGN_DETAILS: String = "campaign_details"

            @JvmField public actual val EARN_VIRTUAL_CURRENCY: String = "earn_virtual_currency"

            @JvmField public actual val GENERATE_LEAD: String = "generate_lead"

            @JvmField public actual val IN_APP_PURCHASE: String = "in_app_purchase"

            @JvmField public actual val JOIN_GROUP: String = "join_group"

            @JvmField public actual val LEVEL_END: String = "level_end"

            @JvmField public actual val LEVEL_START: String = "level_start"

            @JvmField public actual val LEVEL_UP: String = "level_up"

            @JvmField public actual val LOGIN: String = "login"

            @JvmField public actual val POST_SCORE: String = "post_score"

            @JvmField public actual val PURCHASE: String = "purchase"

            @JvmField public actual val REFUND: String = "refund"

            @JvmField public actual val REMOVE_FROM_CART: String = "remove_from_cart"

            @JvmField public actual val SCREEN_VIEW: String = "screen_view"

            @JvmField public actual val SEARCH: String = "search"

            @JvmField public actual val SELECT_CONTENT: String = "select_content"

            @JvmField public actual val SELECT_ITEM: String = "select_item"

            @JvmField public actual val SELECT_PROMOTION: String = "select_promotion"

            @JvmField public actual val SHARE: String = "share"

            @JvmField public actual val SIGN_UP: String = "sign_up"

            @JvmField public actual val SPEND_VIRTUAL_CURRENCY: String = "spend_virtual_currency"

            @JvmField public actual val TUTORIAL_BEGIN: String = "tutorial_begin"

            @JvmField public actual val TUTORIAL_COMPLETE: String = "tutorial_complete"

            @JvmField public actual val UNLOCK_ACHIEVEMENT: String = "unlock_achievement"

            @JvmField public actual val VIEW_CART: String = "view_cart"

            @JvmField public actual val VIEW_ITEM: String = "view_item"

            @JvmField public actual val VIEW_ITEM_LIST: String = "view_item_list"

            @JvmField public actual val VIEW_PROMOTION: String = "view_promotion"

            @JvmField public actual val VIEW_SEARCH_RESULTS: String = "view_search_results"
        }
    }

    /** The recommended parameter names. */
    public actual open class Param protected actual constructor() {
        public actual companion object {
            @JvmField public actual val ACHIEVEMENT_ID: String = "achievement_id"

            @JvmField public actual val ACLID: String = "aclid"

            @JvmField public actual val AD_FORMAT: String = "ad_format"

            @JvmField public actual val AD_PLATFORM: String = "ad_platform"

            @JvmField public actual val AD_SOURCE: String = "ad_source"

            @JvmField public actual val AD_UNIT_NAME: String = "ad_unit_name"

            @JvmField public actual val AFFILIATION: String = "affiliation"

            @JvmField public actual val CAMPAIGN: String = "campaign"

            @JvmField public actual val CAMPAIGN_ID: String = "campaign_id"

            @JvmField public actual val CHARACTER: String = "character"

            @JvmField public actual val CONTENT: String = "content"

            @JvmField public actual val CONTENT_TYPE: String = "content_type"

            @JvmField public actual val COUPON: String = "coupon"

            @JvmField public actual val CP1: String = "cp1"

            @JvmField public actual val CREATIVE_FORMAT: String = "creative_format"

            @JvmField public actual val CREATIVE_NAME: String = "creative_name"

            @JvmField public actual val CREATIVE_SLOT: String = "creative_slot"

            @JvmField public actual val CURRENCY: String = "currency"

            @JvmField public actual val DESTINATION: String = "destination"

            @JvmField public actual val DISCOUNT: String = "discount"

            @JvmField public actual val END_DATE: String = "end_date"

            @JvmField public actual val EXTEND_SESSION: String = "extend_session"

            @JvmField public actual val FLIGHT_NUMBER: String = "flight_number"

            @JvmField public actual val FREE_TRIAL: String = "free_trial"

            @JvmField public actual val GROUP_ID: String = "group_id"

            @JvmField public actual val INDEX: String = "index"

            @JvmField public actual val ITEMS: String = "items"

            @JvmField public actual val ITEM_BRAND: String = "item_brand"

            @JvmField public actual val ITEM_CATEGORY: String = "item_category"

            @JvmField public actual val ITEM_CATEGORY2: String = "item_category2"

            @JvmField public actual val ITEM_CATEGORY3: String = "item_category3"

            @JvmField public actual val ITEM_CATEGORY4: String = "item_category4"

            @JvmField public actual val ITEM_CATEGORY5: String = "item_category5"

            @JvmField public actual val ITEM_ID: String = "item_id"

            @JvmField public actual val ITEM_LIST_ID: String = "item_list_id"

            @JvmField public actual val ITEM_LIST_NAME: String = "item_list_name"

            @JvmField public actual val ITEM_NAME: String = "item_name"

            @JvmField public actual val ITEM_VARIANT: String = "item_variant"

            @JvmField public actual val LEVEL: String = "level"

            @JvmField public actual val LEVEL_NAME: String = "level_name"

            @JvmField public actual val LOCATION: String = "location"

            @JvmField public actual val LOCATION_ID: String = "location_id"

            @JvmField public actual val MARKETING_TACTIC: String = "marketing_tactic"

            @JvmField public actual val MEDIUM: String = "medium"

            @JvmField public actual val METHOD: String = "method"

            @JvmField public actual val NUMBER_OF_NIGHTS: String = "number_of_nights"

            @JvmField public actual val NUMBER_OF_PASSENGERS: String = "number_of_passengers"

            @JvmField public actual val NUMBER_OF_ROOMS: String = "number_of_rooms"

            @JvmField public actual val ORIGIN: String = "origin"

            @JvmField public actual val PAYMENT_TYPE: String = "payment_type"

            @JvmField public actual val PRICE: String = "price"

            @JvmField public actual val PRICE_IS_DISCOUNTED: String = "price_is_discounted"

            @JvmField public actual val PRODUCT_ID: String = "product_id"

            @JvmField public actual val PRODUCT_NAME: String = "product_name"

            @JvmField public actual val PROMOTION_ID: String = "promotion_id"

            @JvmField public actual val PROMOTION_NAME: String = "promotion_name"

            @JvmField public actual val QUANTITY: String = "quantity"

            @JvmField public actual val SCORE: String = "score"

            @JvmField public actual val SCREEN_CLASS: String = "screen_class"

            @JvmField public actual val SCREEN_NAME: String = "screen_name"

            @JvmField public actual val SEARCH_TERM: String = "search_term"

            @JvmField public actual val SHIPPING: String = "shipping"

            @JvmField public actual val SHIPPING_TIER: String = "shipping_tier"

            @JvmField public actual val SOURCE: String = "source"

            @JvmField public actual val SOURCE_PLATFORM: String = "source_platform"

            @JvmField public actual val START_DATE: String = "start_date"

            @JvmField public actual val SUBSCRIPTION: String = "subscription"

            @JvmField public actual val SUCCESS: String = "success"

            @JvmField public actual val TAX: String = "tax"

            @JvmField public actual val TERM: String = "term"

            @JvmField public actual val TRANSACTION_ID: String = "transaction_id"

            @JvmField public actual val TRAVEL_CLASS: String = "travel_class"

            @JvmField public actual val VALUE: String = "value"

            @JvmField public actual val VIRTUAL_CURRENCY_NAME: String = "virtual_currency_name"
        }
    }

    /** The recommended user property names. */
    public actual open class UserProperty protected actual constructor() {
        public actual companion object {
            @JvmField public actual val ALLOW_AD_PERSONALIZATION_SIGNALS: String = "allow_personalized_ads"

            @JvmField public actual val SIGN_UP_METHOD: String = "sign_up_method"
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
        @JvmStatic
        public actual fun getInstance(context: Context): FirebaseAnalytics = stub()

        internal actual fun create(native: NativeAnalytics): FirebaseAnalytics = stub()
    }
}
