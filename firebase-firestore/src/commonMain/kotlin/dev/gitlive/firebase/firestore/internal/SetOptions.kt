package dev.gitlive.firebase.firestore.internal

import com.google.firebase.firestore.setOptionsMerge
import com.google.firebase.firestore.setOptionsMergeFieldPaths
import com.google.firebase.firestore.setOptionsMergeFields
import dev.gitlive.firebase.firestore.FieldPath

@PublishedApi
internal sealed class SetOptions {
    data object Merge : SetOptions()
    data object Overwrite : SetOptions()
    data class MergeFields(val fields: List<String>) : SetOptions()
    data class MergeFieldPaths(val fieldPaths: List<FieldPath>) : SetOptions() {
        val encodedFieldPaths = fieldPaths.map { it.encoded }
    }

    /** The Android-SDK-shaped options, or null for a plain `set` that overwrites the document. */
    fun toCompat(): com.google.firebase.firestore.SetOptions? = when (this) {
        is Merge -> setOptionsMerge()
        is Overwrite -> null
        is MergeFields -> setOptionsMergeFields(fields)
        is MergeFieldPaths -> setOptionsMergeFieldPaths(encodedFieldPaths)
    }
}
