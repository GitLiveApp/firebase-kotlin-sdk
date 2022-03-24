package dev.gitlive.firebase

import kotlinx.serialization.InheritableSerialInfo

@InheritableSerialInfo
@Target(AnnotationTarget.CLASS)
annotation class FirebaseClassDiscriminator(val discriminator: String)