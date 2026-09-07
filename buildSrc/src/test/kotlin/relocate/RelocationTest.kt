package relocate

import kotlin.test.Test
import kotlin.test.assertEquals

class RelocationTest {

    private val relocation = PackageRelocation(mapOf("com.google.firebase" to "dev.gitlive.firebase.android"))

    @Test
    fun mapsInternalNames() {
        assertEquals("dev/gitlive/firebase/android/FirebaseApp", relocation.mapInternalName("com/google/firebase/FirebaseApp"))
        assertEquals("dev/gitlive/firebase/android/installations/FirebaseInstallations\$Companion", relocation.mapInternalName("com/google/firebase/installations/FirebaseInstallations\$Companion"))
        assertEquals("com/google/android/gms/tasks/Task", relocation.mapInternalName("com/google/android/gms/tasks/Task"))
        // Dynamite descriptors embed the module id in the class name
        assertEquals(
            "com/google/android/gms/dynamite/descriptors/dev/gitlive/firebase/android/auth/ModuleDescriptor",
            relocation.mapInternalName("com/google/android/gms/dynamite/descriptors/com/google/firebase/auth/ModuleDescriptor"),
        )
    }

    @Test
    fun mapsDottedNamesAndStrings() {
        assertEquals("dev.gitlive.firebase.android", relocation.mapDotted("com.google.firebase"))
        assertEquals("dev.gitlive.firebase.android.installations", relocation.mapDotted("com.google.firebase.installations"))
        assertEquals("firebase.com.protolitewrapper", relocation.mapDotted("firebase.com.protolitewrapper"))
        assertEquals(
            "dev.gitlive.firebase.android.components:dev.gitlive.firebase.android.installations.FirebaseInstallationsRegistrar",
            relocation.mapString("com.google.firebase.components:com.google.firebase.installations.FirebaseInstallationsRegistrar"),
        )
        assertEquals("Ldev/gitlive/firebase/android/FirebaseApp;", relocation.mapString("Lcom/google/firebase/FirebaseApp;"))
        assertEquals("https://firebase.googleapis.com/", relocation.mapString("https://firebase.googleapis.com/"))
    }

    @Test
    fun mapsManifests() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.google.firebase" >
                <application>
                    <provider
                        android:name="com.google.firebase.provider.FirebaseInitProvider"
                        android:authorities="${'$'}{applicationId}.firebaseinitprovider" />
                    <service android:name="com.google.firebase.components.ComponentDiscoveryService" >
                        <meta-data
                            android:name="com.google.firebase.components:com.google.firebase.FirebaseCommonKtxRegistrar"
                            android:value="com.google.firebase.components.ComponentRegistrar" />
                    </service>
                </application>
            </manifest>
        """.trimIndent()
        val mapped = relocation.mapManifest(manifest)
        assertEquals(false, mapped.contains("package="))
        assertEquals(false, mapped.contains("com.google.firebase"))
        assertEquals(true, mapped.contains("android:name=\"dev.gitlive.firebase.android.provider.FirebaseInitProvider\""))
        assertEquals(true, mapped.contains("android:name=\"dev.gitlive.firebase.android.components:dev.gitlive.firebase.android.FirebaseCommonKtxRegistrar\""))
        assertEquals(true, mapped.contains("android:authorities=\"${'$'}{applicationId}.firebaseinitprovider\""))
    }
}
