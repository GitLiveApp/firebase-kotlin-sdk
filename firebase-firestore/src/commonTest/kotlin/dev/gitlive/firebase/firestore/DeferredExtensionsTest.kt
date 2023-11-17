package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeferredExtensionsTest {

    @Test
    fun testConvert() = runTest {
        val original = CompletableDeferred<Int>()
        val converted = original.convert { it.toString() }

        original.complete(1)
        assertEquals("1", converted.await())
    }

    @Test
    fun testConvertCompleted() = runTest {
        val original = CompletableDeferred(1)
        val converted = original.convert { it.toString() }

        assertEquals("1", converted.await())
    }

    @Test
    fun testCanceled() = runTest {
        val original = CompletableDeferred<Int>()
        val converted = original.convert { it.toString() }

        val result = runCatching {
            original.cancel()

            converted.await()
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CancellationException)
    }
}
