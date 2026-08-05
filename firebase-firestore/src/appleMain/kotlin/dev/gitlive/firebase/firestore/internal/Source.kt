package dev.gitlive.firebase.firestore.internal

import swiftPMImport.dev.gitlive.firebase.firestore.FIRFirestoreSource
import dev.gitlive.firebase.firestore.Source

internal fun Source.toIosSource() = when (this) {
    Source.CACHE -> FIRFirestoreSource.FIRFirestoreSourceCache
    Source.SERVER -> FIRFirestoreSource.FIRFirestoreSourceServer
    Source.DEFAULT -> FIRFirestoreSource.FIRFirestoreSourceDefault
}
