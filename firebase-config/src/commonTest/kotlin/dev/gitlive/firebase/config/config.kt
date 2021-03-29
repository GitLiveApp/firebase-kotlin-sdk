package dev.gitlive.firebase.config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlin.test.*

expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseConfigTest {

    companion object {
        val defaultValues = mapOf<Any?, Any?>(
            "string_key" to "first key",
            "int_key" to 9292,
            "boolean_key" to true
        )
    }

    private val config = Firebase.config

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(context).isEmpty() }
            ?.initialize(
                context,
                FirebaseOptions(
                    applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                    apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                    databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                    storageBucket = "fir-kotlin-sdk.appspot.com",
                    projectId = "fir-kotlin-sdk",
                    gcmSenderId = "ciao"
                )
            )
    }

    @Test
    fun testFetch() = runTest {
        config.fetch()
    }

    @Test
    fun testSetDefaults() = runTest {
        Firebase.config.setDefaults(mapOf(
                "string_key" to "first key",
                "int_key" to 9292,
                "boolean_key" to true
        ))

        Firebase.config.activate()
        val stringValue = config.getValue("string_key").stringValue
        val intValue = config.getValue("int_key").stringValue?.toInt()
        val booleanValue = config.getValue("boolean_key").booleanValue

        println("String is $stringValue    int is $intValue     boolean is $booleanValue")

        assertTrue { booleanValue }
        assertEquals("first key", stringValue)
        assertEquals(9292, intValue)
    }

    @Test
    fun testActivate() = runTest {
//        Firebase.config.setDefaults(defaultValues)

        val stringValue = config.getValue("string_key").stringValue
        val intValue = config.getValue("int_key").stringValue?.toInt()
        val booleanValue = config.getValue("boolean_key").booleanValue

        assertTrue { booleanValue }
        assertEquals("first key", stringValue)
        assertEquals(9292, intValue)
    }

    private suspend fun resetDefaults() = config.setDefaults(emptyMap())

}