/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import dev.gitlive.firebase.analytics.SET_CURRENT_SCREEN_DEPRECATED
import android.content.Context
import android.os.Bundle
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.analytics.internal.NativeAnalytics

/**
 * The entry point of Firebase Analytics, mirroring `com.google.firebase.analytics.FirebaseAnalytics`; obtained with
 * [com.google.firebase.Firebase.analytics][analytics].
 *
 * On Android this class is a header stub for the real one (its bytecode is deleted after compilation); on the other
 * platforms it is real code over the platform SDK through an internal [NativeAnalytics]. On the JVM it is a no-op, as
 * firebase-java-sdk has no analytics. The JS SDK has no session timeout, session ID or analytics data reset (sessions are
 * managed by gtag): on JS and wasmJs [setSessionTimeoutDuration] and [resetAnalyticsData] do nothing, [getSessionId]
 * completes with null, and the three carry `@dev.gitlive.firebase.UnsupportedOnJs`, so code compiled for JS gets a
 * warning until it opts in.
 *
 * The [Event], [Param] and [UserProperty] names are compile-time constants in the Android SDK (static finals); here they are
 * `val`s with the same values, as an expect declaration cannot be `const`. Compare, match and concatenate them as usual,
 * but off Android they cannot initialize a `const val` or be an annotation argument.
 */
public expect class FirebaseAnalytics {
    internal val native: NativeAnalytics

    /** Logs [name] with [params] (see [Event] and [Param] for the recommended names). */
    public fun logEvent(name: String, params: Bundle?)

    /** Deprecated in the SDK and Android-only: sets the current screen; log a `SCREEN_VIEW` event instead. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(SET_CURRENT_SCREEN_DEPRECATED, level = DeprecationLevel.ERROR)
    public fun setCurrentScreen(activity: android.app.Activity, screenName: String?, screenClassOverride: String?)

    /** Parameters added to every event; null clears them. */
    public fun setDefaultEventParameters(parameters: Bundle?)

    /** Sets the user property [name] to [value]; null removes it. */
    public fun setUserProperty(name: String, value: String?)

    public fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>)

    public fun setAnalyticsCollectionEnabled(enabled: Boolean)

    public fun setUserId(id: String?)

    /** The session timeout in [milliseconds]. */
    public fun setSessionTimeoutDuration(milliseconds: Long)

    public fun getAppInstanceId(): Task<String?>

    /** The app instance ID when it is known synchronously (Android, Apple), else null. */
    @Deprecated("Use getAppInstanceId()", ReplaceWith("getAppInstanceId()"))
    public fun getFirebaseInstanceId(): String?

    public fun getSessionId(): Task<Long?>

    public fun resetAnalyticsData()

    /** The recommended event names. */
    public open class Event protected constructor() {
        public companion object {
            public val ADD_PAYMENT_INFO: String
            public val ADD_SHIPPING_INFO: String
            public val ADD_TO_CART: String
            public val ADD_TO_WISHLIST: String
            public val AD_IMPRESSION: String
            public val APP_OPEN: String
            public val BEGIN_CHECKOUT: String
            public val CAMPAIGN_DETAILS: String
            public val EARN_VIRTUAL_CURRENCY: String
            public val GENERATE_LEAD: String
            public val IN_APP_PURCHASE: String
            public val JOIN_GROUP: String
            public val LEVEL_END: String
            public val LEVEL_START: String
            public val LEVEL_UP: String
            public val LOGIN: String
            public val POST_SCORE: String
            public val PURCHASE: String
            public val REFUND: String
            public val REMOVE_FROM_CART: String
            public val SCREEN_VIEW: String
            public val SEARCH: String
            public val SELECT_CONTENT: String
            public val SELECT_ITEM: String
            public val SELECT_PROMOTION: String
            public val SHARE: String
            public val SIGN_UP: String
            public val SPEND_VIRTUAL_CURRENCY: String
            public val TUTORIAL_BEGIN: String
            public val TUTORIAL_COMPLETE: String
            public val UNLOCK_ACHIEVEMENT: String
            public val VIEW_CART: String
            public val VIEW_ITEM: String
            public val VIEW_ITEM_LIST: String
            public val VIEW_PROMOTION: String
            public val VIEW_SEARCH_RESULTS: String
        }
    }

    /** The recommended parameter names. */
    public open class Param protected constructor() {
        public companion object {
            public val ACHIEVEMENT_ID: String
            public val ACLID: String
            public val AD_FORMAT: String
            public val AD_PLATFORM: String
            public val AD_SOURCE: String
            public val AD_UNIT_NAME: String
            public val AFFILIATION: String
            public val CAMPAIGN: String
            public val CAMPAIGN_ID: String
            public val CHARACTER: String
            public val CONTENT: String
            public val CONTENT_TYPE: String
            public val COUPON: String
            public val CP1: String
            public val CREATIVE_FORMAT: String
            public val CREATIVE_NAME: String
            public val CREATIVE_SLOT: String
            public val CURRENCY: String
            public val DESTINATION: String
            public val DISCOUNT: String
            public val END_DATE: String
            public val EXTEND_SESSION: String
            public val FLIGHT_NUMBER: String
            public val FREE_TRIAL: String
            public val GROUP_ID: String
            public val INDEX: String
            public val ITEMS: String
            public val ITEM_BRAND: String
            public val ITEM_CATEGORY: String
            public val ITEM_CATEGORY2: String
            public val ITEM_CATEGORY3: String
            public val ITEM_CATEGORY4: String
            public val ITEM_CATEGORY5: String
            public val ITEM_ID: String
            public val ITEM_LIST_ID: String
            public val ITEM_LIST_NAME: String
            public val ITEM_NAME: String
            public val ITEM_VARIANT: String
            public val LEVEL: String
            public val LEVEL_NAME: String
            public val LOCATION: String
            public val LOCATION_ID: String
            public val MARKETING_TACTIC: String
            public val MEDIUM: String
            public val METHOD: String
            public val NUMBER_OF_NIGHTS: String
            public val NUMBER_OF_PASSENGERS: String
            public val NUMBER_OF_ROOMS: String
            public val ORIGIN: String
            public val PAYMENT_TYPE: String
            public val PRICE: String
            public val PRICE_IS_DISCOUNTED: String
            public val PRODUCT_ID: String
            public val PRODUCT_NAME: String
            public val PROMOTION_ID: String
            public val PROMOTION_NAME: String
            public val QUANTITY: String
            public val SCORE: String
            public val SCREEN_CLASS: String
            public val SCREEN_NAME: String
            public val SEARCH_TERM: String
            public val SHIPPING: String
            public val SHIPPING_TIER: String
            public val SOURCE: String
            public val SOURCE_PLATFORM: String
            public val START_DATE: String
            public val SUBSCRIPTION: String
            public val SUCCESS: String
            public val TAX: String
            public val TERM: String
            public val TRANSACTION_ID: String
            public val TRAVEL_CLASS: String
            public val VALUE: String
            public val VIRTUAL_CURRENCY_NAME: String
        }
    }

    /** The recommended user property names. */
    public open class UserProperty protected constructor() {
        public companion object {
            public val ALLOW_AD_PERSONALIZATION_SIGNALS: String
            public val SIGN_UP_METHOD: String
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
        /**
         * The analytics of the default app, as [com.google.firebase.Firebase.analytics][analytics]. Off Android the
         * [context] is the firebase-app stand-in and is ignored (the platform SDKs need none).
         */
        public fun getInstance(context: Context): FirebaseAnalytics

        internal fun create(native: NativeAnalytics): FirebaseAnalytics
    }
}
