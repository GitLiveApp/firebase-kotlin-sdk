/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference.CompletionListener

/**
 * Writes the server applies when this client disconnects, as the Android SDK's `OnDisconnect`; obtained from
 * [DatabaseReference.onDisconnect]. Each operation completes once the server has registered it.
 */
public expect class OnDisconnect {
    /** Writes [value] at the location on disconnect; null deletes it. */
    public fun setValue(value: Any?): Task<Nothing?>
    public fun setValue(value: Any?, listener: CompletionListener?)

    /** Writes [value] with a String [priority] on disconnect. */
    public fun setValue(value: Any?, priority: String?): Task<Nothing?>
    public fun setValue(value: Any?, priority: String?, listener: CompletionListener?)

    /** Writes [value] with a numeric [priority] on disconnect. */
    public fun setValue(value: Any?, priority: Double): Task<Nothing?>
    public fun setValue(value: Any?, priority: Double, listener: CompletionListener?)

    /** Writes [value] with a [priority] given as a map on disconnect. */
    public fun setValue(value: Any?, priority: Map<*, *>?, listener: CompletionListener?)

    /** Writes the children in [update] on disconnect (see [DatabaseReference.updateChildren]). */
    public fun updateChildren(update: Map<String, Any?>): Task<Nothing?>
    public fun updateChildren(update: Map<String, Any?>, listener: CompletionListener?)

    /** Deletes the data at the location on disconnect. */
    public fun removeValue(): Task<Nothing?>
    public fun removeValue(listener: CompletionListener?)

    /** Cancels every disconnect operation registered for the location. */
    public fun cancel(): Task<Nothing?>
    public fun cancel(listener: CompletionListener)
}
