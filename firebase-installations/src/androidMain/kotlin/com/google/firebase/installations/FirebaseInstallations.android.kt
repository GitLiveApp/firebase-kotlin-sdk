/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.installations.stub

/*
 * Header stubs for com.google.firebase:firebase-installations (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime.
 */

public actual class FirebaseInstallations private constructor() {
    public actual fun getId(): Task<String> = stub()
    public actual fun getToken(forceRefresh: Boolean): Task<InstallationTokenResult> = stub()
    public actual fun delete(): Task<Nothing?> = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseInstallations = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseInstallations = stub()
    }
}

public actual abstract class InstallationTokenResult actual constructor() {
    public actual abstract val token: String
    public actual abstract val tokenExpirationTimestamp: Long
}

public actual class FirebaseInstallationsException : FirebaseException {
    public actual val status: Status

    public actual constructor(status: Status) : super(status.name) {
        this.status = status
    }

    public actual constructor(message: String, status: Status) : super(message) {
        this.status = status
    }

    public actual constructor(message: String, status: Status, cause: Throwable) : super(message, cause) {
        this.status = status
    }

    // Same order as the Android SDK: `when` over the enum compiles to ordinals.
    public actual enum class Status {
        BAD_CONFIG,
        UNAVAILABLE,
        TOO_MANY_REQUESTS,
    }
}
