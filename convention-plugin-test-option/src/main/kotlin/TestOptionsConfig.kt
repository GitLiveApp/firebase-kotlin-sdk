import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.provideDelegate

fun TestOptions.configureTestOptions(project: Project) {
    val targetSdkVersion: Int by project
    targetSdk = targetSdkVersion
    unitTests {
        isIncludeAndroidResources = true
        all { test: org.gradle.api.tasks.testing.Test ->
            test.testLogging {
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
                )
            }
        }
    }
    animationsDisabled = true
    emulatorSnapshots {
        enableForTestFailures = false
    }
    managedDevices.devices.create<ManagedVirtualDevice>("gradleManagedDevice") {
        device = "Pixel 2"
        apiLevel = 33
        systemImageSource = "google-atd"
        require64Bit = true
    }
}
