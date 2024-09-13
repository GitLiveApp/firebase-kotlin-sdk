package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FieldsAndValuesBuilder
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("FieldsAndValuesBuilderImpl")
internal class FieldsAndValuesBuilder : FieldsAndValuesBuilder() {

    internal val fieldAndValues: MutableList<FieldAndValue> = mutableListOf()

    override fun String.toEncoded(encodedValue: Any?) {
        fieldAndValues += FieldAndValue.WithStringField(this, encodedValue)
    }

    override fun FieldPath.toEncoded(encodedValue: Any?) {
        fieldAndValues += FieldAndValue.WithFieldPath(this, encodedValue)
    }
}
