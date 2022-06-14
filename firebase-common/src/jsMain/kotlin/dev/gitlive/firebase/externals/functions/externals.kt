/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.externals.functions

import kotlin.js.Promise

operator fun HttpsCallable.invoke() = asDynamic()() as Promise<HttpsCallableResult>
operator fun HttpsCallable.invoke(data: Any?) = asDynamic()(data) as Promise<HttpsCallableResult>
