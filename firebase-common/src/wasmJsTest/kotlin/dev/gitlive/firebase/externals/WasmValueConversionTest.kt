package dev.gitlive.firebase.externals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WasmValueConversionTest {

    @Test
    fun convertsNestedKotlinContainersRecursively() {
        val value = mapOf(
            "nested" to mapOf("x" to 1),
            "items" to listOf(1, 2),
            "array" to arrayOf("a", "b"),
        ).toJs()

        assertNotNull(value)
        assertEquals(
            """{"nested":{"x":1},"items":[1,2],"array":["a","b"]}""",
            jsonStringify(value),
        )
    }

    @Test
    fun convertsPrimitiveArrays() {
        assertEquals("[1,2,3]", jsonStringify(intArrayOf(1, 2, 3).toJs()))
        assertEquals("[true,false]", jsonStringify(booleanArrayOf(true, false).toJs()))
    }
}
