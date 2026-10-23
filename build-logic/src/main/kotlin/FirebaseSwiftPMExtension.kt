import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.SwiftPMImportExtension
import org.jetbrains.kotlin.konan.target.Family

/** Configuration of the `dev.gitlive.firebase-swiftpm` convention plugin. */
abstract class FirebaseSwiftPMExtension(private val project: Project) {

    private val versions = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    private fun version(alias: String) = versions.findVersion(alias).get().requiredVersion

    /**
     * Adds the given products of the Firebase Apple SDK package, and sets the deployment target of each Apple platform
     * the module declares a target for. Call it after declaring the targets; without Apple targets it does nothing.
     */
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    fun products(vararg names: String) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        val families = kotlin.targets.withType(KotlinNativeTarget::class.java).map { it.konanTarget.family }.toSet()
        if (families.none { it.isAppleFamily }) return

        (kotlin as ExtensionAware).extensions.getByType(SwiftPMImportExtension::class.java).apply {
            if (Family.IOS in families) iosMinimumDeploymentTarget.set(version("ios-deploymentTarget"))
            if (Family.TVOS in families) tvosMinimumDeploymentTarget.set(version("tvos-deploymentTarget"))
            if (Family.OSX in families) macosMinimumDeploymentTarget.set(version("macos-deploymentTarget"))
            swiftPackage(
                url = url("https://github.com/firebase/firebase-ios-sdk.git"),
                version = from(version("firebase-spm")),
                products = names.map { product(it) },
            )
        }
    }
}
