// Named apart from the main FirestoreKt facade, which this file would otherwise shadow on the test classpath.
@file:JvmName("tests")

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.testContext

actual val emulatorHost: String = "localhost"

actual val context: Any = testContext

@Suppress("UNCHECKED_CAST")
actual fun encodedAsMap(encoded: Any?): Map<String, Any?> = encoded as Map<String, Any?>
actual fun Map<String, Any?>.asEncoded(): Any = this
