/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/**
 * The metadata of an object in Cloud Storage, as the Android SDK's `StorageMetadata`: the server-set fields are read
 * from a fetched object, the settable fields are built with [Builder] (chained setters or, from Kotlin, properties:
 * `storageMetadata { contentType = "text/plain" }`).
 */
public expect class StorageMetadata() {
    /** The bucket, once fetched. */
    public val bucket: String?

    /** The `Cache-Control` header. */
    public val cacheControl: String?

    /** The `Content-Disposition` header. */
    public val contentDisposition: String?

    /** The `Content-Encoding` header. */
    public val contentEncoding: String?

    /** The `Content-Language` header. */
    public val contentLanguage: String?

    /** The `Content-Type` header. */
    public val contentType: String?

    /** When the object was created, in milliseconds since the epoch; 0 until fetched. */
    public val creationTimeMillis: Long

    /** The custom metadata value of [key], or null if it is not set. */
    public fun getCustomMetadata(key: String): String?

    /** The keys of the custom metadata. */
    public val customMetadataKeys: Set<String>

    /** The object's generation, once fetched. */
    public val generation: String?

    /** The MD5 hash of the content, once fetched. */
    public val md5Hash: String?

    /** The metadata's generation, once fetched. */
    public val metadataGeneration: String?

    /** The object's name (the last path segment), once fetched. */
    public val name: String?

    /** The object's full path; empty until fetched. */
    public val path: String

    /** The reference of the object, once fetched. */
    public val reference: StorageReference?

    /** The size in bytes; 0 until fetched. */
    public val sizeBytes: Long

    /** When the object was last updated, in milliseconds since the epoch; 0 until fetched. */
    public val updatedTimeMillis: Long

    /** Builds the settable metadata, from scratch or from an existing [StorageMetadata]. */
    public class Builder {
        public constructor()
        public constructor(original: StorageMetadata)

        public var cacheControl: String?
        public var contentDisposition: String?
        public var contentEncoding: String?
        public var contentLanguage: String?
        public var contentType: String?

        public fun build(): StorageMetadata

        public fun setCacheControl(cacheControl: String?): Builder

        public fun setContentDisposition(contentDisposition: String?): Builder

        public fun setContentEncoding(contentEncoding: String?): Builder

        public fun setContentLanguage(contentLanguage: String?): Builder

        public fun setContentType(contentType: String?): Builder

        /** Sets the custom metadata [key] to [value]; a null [value] removes it. */
        public fun setCustomMetadata(key: String, value: String?): Builder
    }
}
