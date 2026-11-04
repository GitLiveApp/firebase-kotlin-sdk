package dev.gitlive.firebase.iossdk.gradle

/**
 * The Firebase iOS SDK frameworks this plugin knows how to vendor, each paired with the `Firebase/<productFolder>/`
 * directory it actually lives under in the official release zip - verified directly against the real zip's
 * contents, since most (but not all - see [Core] and [Installations]) frameworks are bundled under a product
 * folder matching their own name.
 */
enum class FirebaseIosFramework(val frameworkName: String, val productFolder: String = frameworkName) {
    Core("FirebaseCore", productFolder = "FirebaseAnalytics"),
    Installations("FirebaseInstallations", productFolder = "FirebaseAnalytics"),
    Auth("FirebaseAuth"),
    Database("FirebaseDatabase"),
    FirestoreInternal("FirebaseFirestoreInternal", productFolder = "FirebaseFirestore"),
    Firestore("FirebaseFirestore"),
    Functions("FirebaseFunctions"),
    Storage("FirebaseStorage"),
    Messaging("FirebaseMessaging"),
    RemoteConfig("FirebaseRemoteConfig"),
    Performance("FirebasePerformance"),
    Crashlytics("FirebaseCrashlytics"),
    Analytics("FirebaseAnalytics"),
}
