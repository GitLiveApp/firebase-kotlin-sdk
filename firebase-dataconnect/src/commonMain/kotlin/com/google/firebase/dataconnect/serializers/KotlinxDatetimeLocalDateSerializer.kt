/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.serializers

import com.google.firebase.dataconnect.toDataConnectLocalDate
import com.google.firebase.dataconnect.toKotlinxLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** [LocalDateSerializer] for `kotlinx.datetime.LocalDate` fields. */
public object KotlinxDatetimeLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = LocalDateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: LocalDate) {
        LocalDateSerializer.serialize(encoder, value.toDataConnectLocalDate())
    }

    override fun deserialize(decoder: Decoder): LocalDate = LocalDateSerializer.deserialize(decoder).toKotlinxLocalDate()
}
