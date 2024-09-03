package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.externals.CollectionReference
import dev.gitlive.firebase.firestore.externals.addDoc
import dev.gitlive.firebase.firestore.externals.doc
import dev.gitlive.firebase.firestore.rethrow
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import kotlinx.coroutines.await

internal actual class NativeCollectionReferenceWrapper internal actual constructor(actual override val native: NativeCollectionReference) : NativeQueryWrapper(native) {

    constructor(js: CollectionReference) : this(NativeCollectionReference(js))

    override val js: CollectionReference = native.js

    actual val path: String
        get() = rethrow { js.path }

    actual val document get() = rethrow { NativeDocumentReference(doc(js)) }

    actual val parent get() = rethrow { js.parent?.let { NativeDocumentReference(it) } }

    actual fun document(documentPath: String) = rethrow {
        NativeDocumentReference(
            doc(
                js,
                documentPath,
            ),
        )
    }

    actual suspend fun addEncoded(data: EncodedObject) = rethrow {
        NativeDocumentReference(addDoc(js, data.js).await())
    }
}
