/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("databaseAndroid")
@file:JvmMultifileClass

package dev.gitlive.firebase.database

import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android
import com.google.firebase.database.DataSnapshot as CompatDataSnapshot
import com.google.firebase.database.MutableData as CompatMutableData

/** The underlying Firebase Android SDK object. */
public val FirebaseDatabase.android: com.google.firebase.database.FirebaseDatabase get() = compat

/** The underlying Firebase Android SDK object. */
public val Query.android: com.google.firebase.database.Query get() = compat

/** The underlying Firebase Android SDK object. */
public val DatabaseReference.android: com.google.firebase.database.DatabaseReference get() = compat

/** The underlying Firebase Android SDK object. */
public val DataSnapshot.android: com.google.firebase.database.DataSnapshot get() = compat

/** The underlying Firebase Android SDK object. */
public val OnDisconnect.android: com.google.firebase.database.OnDisconnect get() = compat

@Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
public val Query.persistenceEnabled: Boolean get() = PersistenceSettings.enabled[compat.ref.database] ?: true

@Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
public val OnDisconnect.persistenceEnabled: Boolean get() = true

@Deprecated("Unused; it will be removed in the next major version.")
public val OnDisconnect.database: FirebaseDatabase get() = dev.gitlive.firebase.Firebase.database

internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = android

internal actual val CompatDataSnapshot.nativeValue: Any? get() = value

internal actual var CompatMutableData.nativeValue: Any?
    get() = value
    set(value) {
        this.value = value
    }
