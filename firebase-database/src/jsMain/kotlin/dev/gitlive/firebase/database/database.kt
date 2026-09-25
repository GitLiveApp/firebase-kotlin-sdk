/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import com.google.firebase.database.objectKeys
import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import com.google.firebase.database.DataSnapshot as CompatDataSnapshot
import com.google.firebase.database.MutableData as CompatMutableData
import dev.gitlive.firebase.database.externals.DataSnapshot as JsDataSnapshot
import dev.gitlive.firebase.database.externals.OnDisconnect as JsOnDisconnect
import dev.gitlive.firebase.database.externals.Query as JsQuery

/** The underlying Firebase JS SDK object. */
public val FirebaseDatabase.js: Database get() = compat.js

/** The underlying Firebase JS SDK object. */
public val Query.js: JsQuery get() = compat.js

/** The underlying Firebase JS SDK object. */
public val DataSnapshot.js: JsDataSnapshot get() = compat.js

/** The underlying Firebase JS SDK object. */
public val OnDisconnect.js: JsOnDisconnect get() = compat.js

/** The underlying Firebase JS SDK database. */
public val Query.database: Database get() = compat.jsDatabase

/** The underlying Firebase JS SDK database. */
public val DataSnapshot.database: Database get() = compat.database

/** The underlying Firebase JS SDK database. */
public val OnDisconnect.database: Database get() = compat.ref.jsDatabase

internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = js.let { json -> objectKeys(json).associateWith { json[it] } }

internal actual val CompatDataSnapshot.nativeValue: Any? get() = js.`val`()

internal actual var CompatMutableData.nativeValue: Any?
    get() = jsValue
    set(value) {
        jsValue = value
    }
