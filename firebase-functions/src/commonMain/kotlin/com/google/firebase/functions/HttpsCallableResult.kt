/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

/**
 * The result of a call through an [HttpsCallableReference], mirroring `com.google.firebase.functions.HttpsCallableResult`
 * from the Firebase Android SDK.
 */
public expect class HttpsCallableResult {
    /**
     * The data returned by the function, deserialized from JSON: `null`, a `String`, a number, a `Boolean`, a `List`
     * or a `Map` on Android, the JVM and Apple platforms; on JS the JS SDK's own value (a primitive, array or object).
     */
    public val data: Any?
}
