package dev.gitlive.firebase.firestore.internal

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("FieldValueBuilderImpl")
internal class FieldValueBuilder : dev.gitlive.firebase.firestore.FieldValueBuilder() {

    internal val fieldValues: MutableList<Any> = mutableListOf()

    override fun addEncoded(encodedValue: Any) {
        fieldValues += encodedValue
    }
}
