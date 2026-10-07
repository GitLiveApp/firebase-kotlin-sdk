/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

public actual class StorageMetadata actual constructor() {
    internal var bucketValue: String? = null
    internal var cacheControlValue: String? = null
    internal var contentDispositionValue: String? = null
    internal var contentEncodingValue: String? = null
    internal var contentLanguageValue: String? = null
    internal var contentTypeValue: String? = null
    internal var creationTimeMillisValue: Long = 0

    /** A `null` value marks a key removed by [Builder.setCustomMetadata], which an update sends as an explicit delete. */
    internal val customMetadata = mutableMapOf<String, String?>()

    internal var generationValue: String? = null
    internal var md5HashValue: String? = null
    internal var metadataGenerationValue: String? = null
    internal var nameValue: String? = null
    internal var pathValue: String = ""
    internal var referenceValue: StorageReference? = null
    internal var sizeBytesValue: Long = 0
    internal var updatedTimeMillisValue: Long = 0

    public actual val bucket: String? get() = bucketValue
    public actual val cacheControl: String? get() = cacheControlValue
    public actual val contentDisposition: String? get() = contentDispositionValue
    public actual val contentEncoding: String? get() = contentEncodingValue
    public actual val contentLanguage: String? get() = contentLanguageValue
    public actual val contentType: String? get() = contentTypeValue
    public actual val creationTimeMillis: Long get() = creationTimeMillisValue

    public actual fun getCustomMetadata(key: String): String? = customMetadata[key]

    public actual val customMetadataKeys: Set<String> get() = customMetadata.filterValues { it != null }.keys
    public actual val generation: String? get() = generationValue
    public actual val md5Hash: String? get() = md5HashValue
    public actual val metadataGeneration: String? get() = metadataGenerationValue
    public actual val name: String? get() = nameValue
    public actual val path: String get() = pathValue
    public actual val reference: StorageReference? get() = referenceValue
    public actual val sizeBytes: Long get() = sizeBytesValue
    public actual val updatedTimeMillis: Long get() = updatedTimeMillisValue

    override fun toString(): String = "StorageMetadata(path=$path, contentType=$contentType, sizeBytes=$sizeBytes)"

    public actual class Builder {
        private val metadata = StorageMetadata()

        public actual constructor()

        public actual constructor(original: StorageMetadata) {
            metadata.cacheControlValue = original.cacheControlValue
            metadata.contentDispositionValue = original.contentDispositionValue
            metadata.contentEncodingValue = original.contentEncodingValue
            metadata.contentLanguageValue = original.contentLanguageValue
            metadata.contentTypeValue = original.contentTypeValue
            metadata.customMetadata.putAll(original.customMetadata)
        }

        public actual var cacheControl: String?
            get() = metadata.cacheControlValue
            set(value) {
                metadata.cacheControlValue = value
            }

        public actual var contentDisposition: String?
            get() = metadata.contentDispositionValue
            set(value) {
                metadata.contentDispositionValue = value
            }

        public actual var contentEncoding: String?
            get() = metadata.contentEncodingValue
            set(value) {
                metadata.contentEncodingValue = value
            }

        public actual var contentLanguage: String?
            get() = metadata.contentLanguageValue
            set(value) {
                metadata.contentLanguageValue = value
            }

        public actual var contentType: String?
            get() = metadata.contentTypeValue
            set(value) {
                metadata.contentTypeValue = value
            }

        public actual fun build(): StorageMetadata = Builder(metadata).metadata

        public actual fun setCacheControl(cacheControl: String?): Builder = apply { this.cacheControl = cacheControl }

        public actual fun setContentDisposition(contentDisposition: String?): Builder = apply { this.contentDisposition = contentDisposition }

        public actual fun setContentEncoding(contentEncoding: String?): Builder = apply { this.contentEncoding = contentEncoding }

        public actual fun setContentLanguage(contentLanguage: String?): Builder = apply { this.contentLanguage = contentLanguage }

        public actual fun setContentType(contentType: String?): Builder = apply { this.contentType = contentType }

        public actual fun setCustomMetadata(key: String, value: String?): Builder = apply {
            metadata.customMetadata[key] = value
        }
    }
}
