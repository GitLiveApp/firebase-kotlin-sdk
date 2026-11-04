/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/*
 * Header stubs of the Android SDK's mapping annotations (see buildSrc utils/HeaderStubs.kt), with the SDK's targets: its
 * reflective mapper reads them from getters and fields, so an annotation without a use-site target goes to the field as
 * it does against the SDK's own classes.
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
public actual annotation class Exclude

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class IgnoreExtraProperties

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class ThrowOnExtraProperties

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
public actual annotation class PropertyName(actual val value: String)
