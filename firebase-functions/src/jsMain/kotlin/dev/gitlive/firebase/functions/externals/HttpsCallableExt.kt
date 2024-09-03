/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions.externals

import kotlin.js.Promise

public operator fun HttpsCallable.invoke(): Promise<HttpsCallableResult> = asDynamic()() as Promise<HttpsCallableResult>
public operator fun HttpsCallable.invoke(data: Any?): Promise<HttpsCallableResult> = asDynamic()(data) as Promise<HttpsCallableResult>
