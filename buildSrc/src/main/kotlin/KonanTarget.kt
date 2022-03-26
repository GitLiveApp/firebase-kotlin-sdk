import java.io.File
import org.jetbrains.kotlin.konan.target.KonanTarget

val KonanTarget.archVariant: String
    get() = when (this) {
        is KonanTarget.IOS_ARM64 -> {
            "ios-arm64_armv7"
        }
        is KonanTarget.IOS_X64,
        is KonanTarget.IOS_SIMULATOR_ARM64 -> {
            "ios-arm64_i386_x86_64-simulator"
        }
        is KonanTarget.MACOS_X64,
        is KonanTarget.MACOS_ARM64 -> {
            "macos-arm64_x86_64"
        }
        is KonanTarget.TVOS_ARM64 -> {
            "tvos-arm64"
        }
        is KonanTarget.TVOS_X64,
        is KonanTarget.TVOS_SIMULATOR_ARM64 -> {
            "tvos-arm64_x86_64-simulator"
        }
        else -> "unknown"
    }

fun KonanTarget.carthageFrameworksBasePath(projectDir: File) =
    projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build")

fun KonanTarget.carthageFrameworksPaths(projectDir: File, paths: List<String>) = paths.map {
    carthageFrameworksBasePath(projectDir).resolve("iOS")
}

fun KonanTarget.carthageXcFrameworksPaths(projectDir: File, names: List<String>) = names.map {
    carthageFrameworksBasePath(projectDir).resolve("$it.xcframework/$archVariant")
}

fun KonanTarget.firebaseCoreFrameworksPaths(projectDir: File) =
    carthageFrameworksPaths(projectDir, listOf("iOS")).plus(
        carthageXcFrameworksPaths(
            projectDir,
            listOf(
                "FirebaseAnalytics",
                "FirebaseCore",
                "FirebaseCoreDiagnostics",
                "FirebaseInstallations",
                "GoogleAppMeasurement",
                "GoogleAppMeasurementIdentitySupport",
                "GoogleDataTransport",
                "GoogleUtilities",
                "nanopb",
                "PromisesObjC"
            )
        )
    )
