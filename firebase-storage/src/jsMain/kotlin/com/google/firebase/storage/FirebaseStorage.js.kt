/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.storage.externals.connectStorageEmulator
import dev.gitlive.firebase.storage.externals.getStorage
import dev.gitlive.firebase.storage.externals.ref
import dev.gitlive.firebase.storage.externals.FirebaseStorage as JsFirebaseStorage

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseStorage internal constructor(public val js: JsFirebaseStorage) {
    public actual val app: FirebaseApp get() = FirebaseApp.getInstance(js.app.name)

    public actual var maxOperationRetryTimeMillis: Long
        get() = js.maxOperationRetryTime.toLong()
        set(value) {
            js.maxOperationRetryTime = value.toDouble()
        }

    public actual var maxUploadRetryTimeMillis: Long
        get() = js.maxUploadRetryTime.toLong()
        set(value) {
            js.maxUploadRetryTime = value.toDouble()
        }

    public actual val reference: StorageReference get() = rethrow { StorageReference(ref(js)) }

    public actual fun getReference(location: String): StorageReference = rethrow { StorageReference(ref(js, location)) }

    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference = rethrow { StorageReference(ref(js, fullUrl)) }

    public actual fun useEmulator(host: String, port: Int) {
        rethrow { connectStorageEmulator(js, host, port.toDouble()) }
    }

    override fun equals(other: Any?): Boolean = other is FirebaseStorage && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseStorage($js)"

    public actual companion object {
        public actual fun getInstance(): FirebaseStorage = rethrow { FirebaseStorage(getStorage()) }

        public actual fun getInstance(url: String): FirebaseStorage = rethrow { FirebaseStorage(getStorage(null, url)) }

        public actual fun getInstance(app: FirebaseApp): FirebaseStorage = rethrow { FirebaseStorage(getStorage(app.js)) }

        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseStorage = rethrow { FirebaseStorage(getStorage(app.js, url)) }
    }
}
