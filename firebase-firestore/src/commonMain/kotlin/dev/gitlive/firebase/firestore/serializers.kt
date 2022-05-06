package dev.gitlive.firebase.firestore

import kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable

/**
 * Builder for a [SerialDescriptor] which fixes an nullability issue in [kotlinx.serialization.descriptors.buildClassSerialDescriptor]
 * @return a class [SerialDescriptor]. */
fun buildClassSerialDescriptor(
    serialName: String,
    vararg typeParameters: SerialDescriptor,
    isNullable: Boolean,
    builderAction: ClassSerialDescriptorBuilder.() -> Unit = {}
): SerialDescriptor {
    val descriptor = kotlinx.serialization.descriptors.buildClassSerialDescriptor(
        serialName = serialName,
        typeParameters = typeParameters,
        builderAction = builderAction
    )

    return if (isNullable && !descriptor.isNullable) {
        // bug https://youtrack.jetbrains.com/issue/KT-52300/Serialization-buildClassSerialDescriptor-ClassSerialDescriptorBu
        descriptor.nullable
    } else {
        descriptor
    }
}

