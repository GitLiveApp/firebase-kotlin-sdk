/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/*
 * The mapping annotations that FirebaseFormat reads, the counterparts of the Firebase Android SDK's
 * (com.google.firebase.firestore.IgnoreExtraProperties and so on). kotlinx.serialization records an annotation in a
 * class's descriptor only when the annotation class carries @SerialInfo and is applied to the property itself (not with a
 * @get:/@field: use-site target) or to the class; the SDK's own annotations cannot be recorded on Android, where they are
 * the SDK's classes, so the products' modules deprecate them towards these. Names and exclusion are kotlinx.serialization's
 * own @SerialName and @Transient.
 */

/** Makes the mapper ignore properties of the data that the class does not have (they are logged otherwise). */
@SerialInfo
@Target(AnnotationTarget.CLASS)
public annotation class IgnoreExtraProperties

/** Makes the mapper fail on properties of the data that the class does not have. */
@SerialInfo
@Target(AnnotationTarget.CLASS)
public annotation class ThrowOnExtraProperties

/** Marks a Firestore `Timestamp` or `Instant` property written as the server timestamp when it is null. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class ServerTimestamp

/** Marks a `String` or `DocumentReference` property that receives the document id when a Firestore document is mapped to a class. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class DocumentId
