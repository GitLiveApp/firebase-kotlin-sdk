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

    /**
     * The header fields set through the [Builder] (by their JSON names), as the Android SDK tracks user-provided values: an
     * update sends only these, a `null` value as an explicit delete, and leaves the object's other fields untouched.
     */
    internal val providedFields = mutableSetOf<String>()

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

        /** Copies every field of [original], the identifying ones included, as the Android SDK's copying constructor does. */
        public actual constructor(original: StorageMetadata) {
            metadata.bucketValue = original.bucketValue
            metadata.cacheControlValue = original.cacheControlValue
            metadata.contentDispositionValue = original.contentDispositionValue
            metadata.contentEncodingValue = original.contentEncodingValue
            metadata.contentLanguageValue = original.contentLanguageValue
            metadata.contentTypeValue = original.contentTypeValue
            metadata.creationTimeMillisValue = original.creationTimeMillisValue
            metadata.customMetadata.putAll(original.customMetadata)
            metadata.providedFields.addAll(original.providedFields)
            metadata.generationValue = original.generationValue
            metadata.md5HashValue = original.md5HashValue
            metadata.metadataGenerationValue = original.metadataGenerationValue
            metadata.nameValue = original.nameValue
            metadata.pathValue = original.pathValue
            metadata.referenceValue = original.referenceValue
            metadata.sizeBytesValue = original.sizeBytesValue
            metadata.updatedTimeMillisValue = original.updatedTimeMillisValue
        }

        public actual var cacheControl: String?
            get() = metadata.cacheControlValue
            set(value) {
                metadata.cacheControlValue = value
                metadata.providedFields += "cacheControl"
            }

        public actual var contentDisposition: String?
            get() = metadata.contentDispositionValue
            set(value) {
                metadata.contentDispositionValue = value
                metadata.providedFields += "contentDisposition"
            }

        public actual var contentEncoding: String?
            get() = metadata.contentEncodingValue
            set(value) {
                metadata.contentEncodingValue = value
                metadata.providedFields += "contentEncoding"
            }

        public actual var contentLanguage: String?
            get() = metadata.contentLanguageValue
            set(value) {
                metadata.contentLanguageValue = value
                metadata.providedFields += "contentLanguage"
            }

        public actual var contentType: String?
            get() = metadata.contentTypeValue
            set(value) {
                metadata.contentTypeValue = value
                metadata.providedFields += "contentType"
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

/** The header fields an update sends: the ones set through the [StorageMetadata.Builder], a `null` value meaning delete. */
internal fun StorageMetadata.providedHeaders(): Map<String, String?> = buildMap {
    if ("cacheControl" in providedFields) put("cacheControl", cacheControl)
    if ("contentDisposition" in providedFields) put("contentDisposition", contentDisposition)
    if ("contentEncoding" in providedFields) put("contentEncoding", contentEncoding)
    if ("contentLanguage" in providedFields) put("contentLanguage", contentLanguage)
    if ("contentType" in providedFields) put("contentType", contentType)
}
