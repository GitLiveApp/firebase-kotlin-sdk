package dev.gitlive.firebase.firestore.internal

import kotlin.js.Json
import kotlin.js.json

internal val SetOptions.js: Json
    get() = when (this) {
        is SetOptions.Merge -> json("merge" to true)
        is SetOptions.Overwrite -> json("merge" to false)
        is SetOptions.MergeFields -> json("mergeFields" to fields.toTypedArray())
        is SetOptions.MergeFieldPaths -> json("mergeFields" to encodedFieldPaths.toTypedArray())
    }
