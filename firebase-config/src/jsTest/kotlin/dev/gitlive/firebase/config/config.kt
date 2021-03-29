package dev.gitlive.firebase.config

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = GlobalScope.promise { test() }.unsafeCast<Unit>()