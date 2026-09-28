/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import dev.gitlive.firebase.dataconnect.internal.DataConnectInstances
import kotlinx.coroutines.flow.MutableStateFlow

public actual fun FirebaseDataConnect.Companion.getInstance(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings): FirebaseDataConnect = DataConnectInstances.get(app, config, settings)

public actual fun FirebaseDataConnect.Companion.getInstance(config: ConnectorConfig, settings: DataConnectSettings): FirebaseDataConnect = getInstance(Firebase.app, config, settings)

private val globalLogLevel = MutableStateFlow(LogLevel.WARN)

public actual val FirebaseDataConnect.Companion.logLevel: MutableStateFlow<LogLevel> get() = globalLogLevel
