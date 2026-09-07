package dev.gitlive.firebase.firestore.internal

internal val SetOptions.android: dev.gitlive.firebase.android.firestore.SetOptions? get() = when (this) {
    is SetOptions.Merge -> dev.gitlive.firebase.android.firestore.SetOptions.merge()
    is SetOptions.Overwrite -> null
    is SetOptions.MergeFields -> dev.gitlive.firebase.android.firestore.SetOptions.mergeFields(fields)
    is SetOptions.MergeFieldPaths -> dev.gitlive.firebase.android.firestore.SetOptions.mergeFieldPaths(encodedFieldPaths)
}
