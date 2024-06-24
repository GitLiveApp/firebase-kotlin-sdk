package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import kotlin.test.Test
import kotlin.test.assertNotNull

abstract class FirebaseMessagingTest {

    @Test
    fun initialization() {
        assertNotNull(Firebase.messaging)
    }
}
