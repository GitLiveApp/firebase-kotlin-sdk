/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth.internal

import android.net.Uri
import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.MultiFactorInfo
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.UserProfileChangeRequest

/*
 * firebase-java-sdk's FirebaseUser returns its photo URL as a String rather than a Uri (and does not implement UserInfo),
 * and its action code details are interfaces rather than classes, so these members are reached by reflection: the
 * bytecode compiled against the Android SDK's shapes would not link.
 */

internal actual fun UserInfo.photoUrlString(): String? = javaClass.getMethod("getPhotoUrl").invoke(this)?.toString()

internal actual fun UserProfileChangeRequest.Builder.setPhotoUrlString(url: String?): UserProfileChangeRequest.Builder = setPhotoUri(url?.let { Uri.parse(it) })

internal actual fun ActionCodeInfo.emailValue(): String = javaClass.getMethod("getEmail").invoke(this) as String

internal actual fun ActionCodeEmailInfo.previousEmailValue(): String = javaClass.getMethod("getPreviousEmail").invoke(this) as String

internal actual fun ActionCodeMultiFactorInfo.multiFactorInfoValue(): MultiFactorInfo = javaClass.getMethod("getMultiFactorInfo").invoke(this) as MultiFactorInfo
