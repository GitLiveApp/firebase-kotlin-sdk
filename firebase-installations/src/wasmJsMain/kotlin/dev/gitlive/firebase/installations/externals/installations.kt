@file:JsModule("firebase/installations")

package dev.gitlive.firebase.installations.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun delete(installations: Installations): Promise<JsAny?>

public external fun getId(installations: Installations): Promise<JsString>

public external fun getInstallations(app: FirebaseApp? = definedExternally): Installations

public external fun getToken(installations: Installations, forceRefresh: Boolean): Promise<JsString>

public external interface Installations : JsAny
