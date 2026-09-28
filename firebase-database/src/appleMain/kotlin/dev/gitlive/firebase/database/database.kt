/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildAdded
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildChanged
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildMoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildRemoved
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseQuery
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.ChildEvent.Type.ADDED
import dev.gitlive.firebase.database.ChildEvent.Type.CHANGED
import dev.gitlive.firebase.database.ChildEvent.Type.MOVED
import dev.gitlive.firebase.database.ChildEvent.Type.REMOVED
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios
import com.google.firebase.database.DataSnapshot as CompatDataSnapshot
import com.google.firebase.database.MutableData as CompatMutableData

/** The underlying Firebase iOS SDK object. */
public val FirebaseDatabase.ios: FIRDatabase get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val Query.ios: FIRDatabaseQuery get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val DatabaseReference.ios: FIRDatabaseReference get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val DataSnapshot.ios: FIRDataSnapshot get() = compat.ios

/** The underlying Firebase iOS SDK reference the disconnect operations are registered on. */
public val OnDisconnect.ios: FIRDatabaseReference get() = compat.ios

public fun Type.toEventType(): FIRDataEventType = when (this) {
    ADDED -> FIRDataEventTypeChildAdded
    CHANGED -> FIRDataEventTypeChildChanged
    MOVED -> FIRDataEventTypeChildMoved
    REMOVED -> FIRDataEventTypeChildRemoved
}

@Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
public val Query.persistenceEnabled: Boolean get() = compat.ios.ref.database.persistenceEnabled

@Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
public val OnDisconnect.persistenceEnabled: Boolean get() = compat.ios.database.persistenceEnabled

@Suppress("UNCHECKED_CAST")
internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = ios as Map<String, Any?>

internal actual val CompatDataSnapshot.nativeValue: Any? get() = value

internal actual var CompatMutableData.nativeValue: Any?
    get() = value
    set(value) {
        this.value = value
    }
