package dev.gitlive.firebase.iossdk.gradle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppleSliceTest {

    @Test
    fun `maps every Kotlin Native Apple target to the slice it needs`() {
        assertEquals(AppleSlice.IosDevice, appleSliceForTarget("iosArm64"))
        assertEquals(AppleSlice.IosSimulator, appleSliceForTarget("iosX64"))
        assertEquals(AppleSlice.IosSimulator, appleSliceForTarget("iosSimulatorArm64"))
        assertEquals(AppleSlice.TvosDevice, appleSliceForTarget("tvosArm64"))
        assertEquals(AppleSlice.TvosSimulator, appleSliceForTarget("tvosX64"))
        assertEquals(AppleSlice.TvosSimulator, appleSliceForTarget("tvosSimulatorArm64"))
        assertEquals(AppleSlice.MacosUniversal, appleSliceForTarget("macosArm64"))
        assertEquals(AppleSlice.MacosUniversal, appleSliceForTarget("macosX64"))
    }

    @Test
    fun `returns null for targets this plugin has nothing to offer`() {
        assertNull(appleSliceForTarget("linuxX64"))
        assertNull(appleSliceForTarget("mingwX64"))
        assertNull(appleSliceForTarget("jvm"))
    }

    @Test
    fun `library identifiers match the real Firebase 13 1 1 release zip`() {
        // Verified directly against Firebase/FirebaseAnalytics/FirebaseCore.xcframework/Info.plist in the
        // real 13.1.1 release zip.
        assertEquals("ios-arm64", AppleSlice.IosDevice.libraryIdentifier)
        assertEquals("ios-arm64_x86_64-simulator", AppleSlice.IosSimulator.libraryIdentifier)
        assertEquals("tvos-arm64", AppleSlice.TvosDevice.libraryIdentifier)
        assertEquals("tvos-arm64_x86_64-simulator", AppleSlice.TvosSimulator.libraryIdentifier)
        assertEquals("macos-arm64_x86_64", AppleSlice.MacosUniversal.libraryIdentifier)
    }
}
