package dev.gitlive.firebase

actual fun Timestamp.asNative() : Any {
    return com.google.firebase.Timestamp(seconds, nanoseconds)
}