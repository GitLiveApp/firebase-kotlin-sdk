/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.dataconnect.internal.stub
import kotlinx.coroutines.flow.MutableStateFlow

// Header stubs of the Android SDK's FirebaseDataConnectKt (see utils.stripHeaderStubs).

public actual fun FirebaseDataConnect.Companion.getInstance(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings): FirebaseDataConnect = stub()

public actual fun FirebaseDataConnect.Companion.getInstance(config: ConnectorConfig, settings: DataConnectSettings): FirebaseDataConnect = stub()

public actual val FirebaseDataConnect.Companion.logLevel: MutableStateFlow<LogLevel> get() = stub()
