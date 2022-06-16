import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.setupHierarchicalSourceSets() {
    with(sourceSets) {
//        maybeCreate("nativeMain").apply {
//            dependsOn(getByName("commonMain"))
//        }

        maybeCreate("nativeDarwinMain").apply {
            dependsOn(getByName("commonMain"))

            listOf(
                "iosMain",
                "iosSimulatorArm64Main",
                "macosArm64Main",
                "macosX64Main",
                "tvosMain",
                "tvosSimulatorArm64Main",
//                "watchosArm32Main",
//                "watchosArm64Main",
//                "watchosX64Main",
//                "watchosSimulatorArm64Main"
            ).forEach {
                getByName(it).dependsOn(this)
            }
        }

//        maybeCreate("nativeTest").apply {
//            dependsOn(getByName("commonTest"))
//        }

        maybeCreate("nativeDarwinTest").apply {
            dependsOn(getByName("commonTest"))

            listOf(
                "iosTest",
                "iosSimulatorArm64Test",
                "macosArm64Test",
                "macosX64Test",
                "tvosTest",
                "tvosSimulatorArm64Test",
//                "watchosArm32Test",
//                "watchosArm64Test",
//                "watchosX64Test",
//                "watchosSimulatorArm64Test"
            ).forEach {
                getByName(it).dependsOn(this)
            }
        }
    }
}