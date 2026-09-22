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

internal actual fun UserInfo.photoUrlString(): String? = photoUrl?.toString()

internal actual fun UserProfileChangeRequest.Builder.setPhotoUrlString(url: String?): UserProfileChangeRequest.Builder = setPhotoUri(url?.let { Uri.parse(it) })

internal actual fun ActionCodeInfo.emailValue(): String = email

internal actual fun ActionCodeEmailInfo.previousEmailValue(): String = previousEmail

internal actual fun ActionCodeMultiFactorInfo.multiFactorInfoValue(): MultiFactorInfo = multiFactorInfo
