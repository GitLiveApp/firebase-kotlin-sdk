/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.minutes

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
    kotlinx.coroutines.test.runTest { action() }
}

actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any {
    val target = jsObject()
    for ((key, value) in pairs) {
        val stringKey = (key as? String) ?: jsonStringify(key.toJsValue())
        jsSet(target, stringKey, value.toJsValue())
    }
    return target
}

actual fun nativeListOf(vararg elements: Any?): Any {
    val array = JsArray<JsAny?>()
    elements.forEachIndexed { index, element -> array[index] = element.toJsValue() }
    return array
}

actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(jsonStringify(expected.toJsValue()), jsonStringify(actual.toJsValue()))
}

// Self-contained JS interop helpers (test-utils has no dependency on firebase-common).
private fun jsObject(): JsAny = js("({})")
private fun jsSet(target: JsAny, key: String, value: JsAny?) {
    js("target[key] = value")
}
private fun jsonStringify(value: JsAny?): String = js("JSON.stringify(value)")

private fun Any?.toJsValue(): JsAny? = when (this) {
    null -> null
    is Boolean -> toJsBoolean()
    is Byte -> toInt().toJsNumber()
    is Short -> toInt().toJsNumber()
    is Int -> toJsNumber()
    is Long -> toDouble().toJsNumber()
    is Float -> toDouble().toJsNumber()
    is Double -> toJsNumber()
    is Char -> toString().toJsString()
    is String -> toJsString()
    is Map<*, *> -> {
        val target = jsObject()
        for ((key, value) in this) {
            jsSet(target, (key as? String) ?: jsonStringify(key.toJsValue()), value.toJsValue())
        }
        target
    }
    is List<*> -> {
        val array = JsArray<JsAny?>()
        forEachIndexed { index, element -> array[index] = element.toJsValue() }
        array
    }
    else -> asJsValue()
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
private fun Any.asJsValue(): JsAny = this as JsAny
