@file:JsModule("firebase/installations")
@file:JsNonModule

package dev.gitlive.firebase.installations.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun delete(installations: Installations): Promise<Unit>

public external fun getId(installations: Installations): Promise<String>

public external fun getInstallations(app: FirebaseApp? = definedExternally): Installations

public external fun getToken(installations: Installations, forceRefresh: Boolean): Promise<String>

public external interface Installations
