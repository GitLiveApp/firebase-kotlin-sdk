package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WasmStorageInteropTest {

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
        } finally {
            if (existingDefaultApp == null) app.delete()
        }
    }

    @Test
    fun absentNextPageTokenIsNull() {
        val result = ListResult(listResultWithoutPageToken())
        assertNull(result.pageToken)
    }
}

private fun listResultWithoutPageToken(): dev.gitlive.firebase.storage.externals.ListResult = js("({ items: [], prefixes: [] })")
