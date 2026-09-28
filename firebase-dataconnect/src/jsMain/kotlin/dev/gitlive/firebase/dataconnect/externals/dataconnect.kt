/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("firebase/data-connect")
@file:JsNonModule

package dev.gitlive.firebase.dataconnect.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external interface DataConnect

public external interface QueryRef

public external interface MutationRef

/** The result of `executeQuery`/`executeMutation`: the data, and for a query `"CACHE"` or `"SERVER"`. */
public external interface OperationResult {
    public val data: Any?
    public val source: String
}

/** [connectorConfig] is `{ location, connector, service }`; [settings] is `{ cacheSettings?: { cacheProvider, maxAgeSeconds? } }`. */
public external fun getDataConnect(app: FirebaseApp, connectorConfig: Any, settings: Any): DataConnect

public external fun connectDataConnectEmulator(dc: DataConnect, host: String, port: Int, sslEnabled: Boolean)

public external fun queryRef(dc: DataConnect, queryName: String, variables: Any?): QueryRef

public external fun mutationRef(dc: DataConnect, mutationName: String, variables: Any?): MutationRef

/** [options] is `{ fetchPolicy: "PREFER_CACHE" | "CACHE_ONLY" | "SERVER_ONLY" }`. */
public external fun executeQuery(queryRef: QueryRef, options: Any): Promise<OperationResult>

public external fun executeMutation(mutationRef: MutationRef): Promise<OperationResult>

public external fun makeMemoryCacheProvider(): Any

public external fun terminate(dataConnect: DataConnect): Promise<Unit>

public external fun setLogLevel(logLevel: String)
