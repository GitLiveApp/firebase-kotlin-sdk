/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JsStorageInteropTest {
    /** `getStorage(undefined, url)` takes the default app; an explicit null would not. */
    @Test
    fun storageBucketOverloadOmitsTheOptionalApp() = runTest {
        val existingDefaultApp = Firebase.apps(Unit).firstOrNull { it.name == "[DEFAULT]" }
        val app = existingDefaultApp ?: Firebase.initialize(
            Unit,
            FirebaseOptions(
                applicationId = "test-app-id",
                apiKey = "test-api-key",
                projectId = "test-project",
            ),
        )
        try {
            assertEquals("test-bucket", Firebase.storage("gs://test-bucket").reference.bucket)
            assertEquals("test-bucket", com.google.firebase.storage.FirebaseStorage.getInstance("gs://test-bucket").reference.bucket)
        } finally {
            if (existingDefaultApp == null) app.delete()
        }
    }
}
