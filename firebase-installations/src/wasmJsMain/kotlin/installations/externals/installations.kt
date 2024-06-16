@file:JsModule("firebase/installations")
@file:JsNonModule

package dev.gitlive.firebase.installations.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

external fun delete(installations: Installations): Promise<Unit>

external fun getId(installations: Installations): Promise<String>

external fun getInstallations(app: FirebaseApp? = definedExternally): Installations

external fun getToken(installations: Installations, forceRefresh: Boolean): Promise<String>

external interface Installations
