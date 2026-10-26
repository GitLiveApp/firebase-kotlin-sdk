/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions.externals

import kotlin.js.Promise

public operator fun HttpsCallable.invoke(): Promise<HttpsCallableResult> = callHttpsCallable(this)
public operator fun HttpsCallable.invoke(data: JsAny?): Promise<HttpsCallableResult> = callHttpsCallableWithData(this, data)

private fun callHttpsCallable(callable: HttpsCallable): Promise<HttpsCallableResult> = js("callable()")
private fun callHttpsCallableWithData(callable: HttpsCallable, data: JsAny?): Promise<HttpsCallableResult> = js("callable(data)")
