package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.externals.jsArrayOf
import dev.gitlive.firebase.externals.json

internal val SetOptions.js: JsAny
    get() = when (this) {
        is SetOptions.Merge -> json("merge" to true)
        is SetOptions.Overwrite -> json("merge" to false)
        is SetOptions.MergeFields -> json("mergeFields" to jsArrayOf(fields))
        is SetOptions.MergeFieldPaths -> json("mergeFields" to jsArrayOf(encodedFieldPaths))
    }
