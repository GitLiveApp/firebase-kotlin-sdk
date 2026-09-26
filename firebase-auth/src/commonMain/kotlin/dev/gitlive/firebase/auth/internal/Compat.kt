/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth.internal

import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ActionCodeInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.MultiFactorInfo
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.UserProfileChangeRequest

/*
 * The seams between the dev.gitlive layer and the com.google.firebase.auth layer that differ per platform: the profile
 * photo (an android.net.Uri in the Android SDK, a String elsewhere) and the action code details, which firebase-java-sdk
 * models as interfaces rather than the Android SDK's classes.
 */

internal expect fun ActionCodeInfo.emailValue(): String

internal expect fun ActionCodeEmailInfo.previousEmailValue(): String

internal expect fun ActionCodeMultiFactorInfo.multiFactorInfoValue(): MultiFactorInfo

/** The profile photo URL as a string. */
internal expect fun UserInfo.photoUrlString(): String?

/** Sets the profile photo from [url], or clears it when null. */
internal expect fun UserProfileChangeRequest.Builder.setPhotoUrlString(url: String?): UserProfileChangeRequest.Builder

/** Signs out, waiting for the JS SDK (which clears the current user asynchronously) to finish. */
internal expect suspend fun FirebaseAuth.signOutAwaiting()
