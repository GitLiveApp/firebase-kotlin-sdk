/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.JsRemoteConfigValue
import dev.gitlive.firebase.remoteconfig.externals.RemoteConfig
import dev.gitlive.firebase.remoteconfig.externals.Value

/** The underlying Firebase JS SDK object. */
public val FirebaseRemoteConfig.js: RemoteConfig get() = compat.js

/** The underlying Firebase JS SDK object. */
public val FirebaseRemoteConfigValue.js: Value get() = (compat as JsRemoteConfigValue).js
