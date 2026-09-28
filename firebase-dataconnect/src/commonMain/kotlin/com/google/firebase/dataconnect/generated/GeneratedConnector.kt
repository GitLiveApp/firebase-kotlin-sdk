/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.generated

import com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
import com.google.firebase.dataconnect.FirebaseDataConnect

/** The base of the connector classes that the Data Connect tooling generates. */
public interface GeneratedConnector<T : GeneratedConnector<T>> {
    /** The instance the generated operations execute on. */
    public val dataConnect: FirebaseDataConnect

    /** A copy of this connector on another instance. */
    @ExperimentalFirebaseDataConnect
    public fun copy(dataConnect: FirebaseDataConnect = this.dataConnect): T

    /** Every operation of the connector. */
    @ExperimentalFirebaseDataConnect
    public fun operations(): List<GeneratedOperation<T, *, *>>

    /** Every query of the connector. */
    @ExperimentalFirebaseDataConnect
    public fun queries(): List<GeneratedQuery<T, *, *>>

    /** Every mutation of the connector. */
    @ExperimentalFirebaseDataConnect
    public fun mutations(): List<GeneratedMutation<T, *, *>>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}
