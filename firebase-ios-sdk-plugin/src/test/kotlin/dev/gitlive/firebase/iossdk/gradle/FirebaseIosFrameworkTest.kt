package dev.gitlive.firebase.iossdk.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseIosFrameworkTest {

    @Test
    fun `defaults foundational frameworks to the product folder they are actually bundled under`() {
        // Verified directly against the real 13.1.1 release zip's contents - these three are bundled as
        // dependencies of other products rather than shipping their own top-level product folder.
        assertEquals("FirebaseAnalytics", FirebaseIosFramework.Core.productFolder)
        assertEquals("FirebaseAnalytics", FirebaseIosFramework.Installations.productFolder)
        assertEquals("FirebaseFirestore", FirebaseIosFramework.FirestoreInternal.productFolder)
    }

    @Test
    fun `defaults self named frameworks to their own product folder`() {
        for (framework in FirebaseIosFramework.values().toSet() - setOf(
            FirebaseIosFramework.Core,
            FirebaseIosFramework.Installations,
            FirebaseIosFramework.FirestoreInternal,
        )) {
            assertEquals(framework.frameworkName, framework.productFolder)
        }
    }
}
