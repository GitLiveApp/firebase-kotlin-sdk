import com.google.gson.GsonBuilder
import org.gradle.api.Project
import java.io.File
import java.util.Properties

class EmulatorJobsMatrix {

    private val gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }

    fun createMatrixJsonFiles(rootProject: Project) {
        mapOf(
            "emulator_jobs_matrix.json" to getEmulatorTaskList(rootProject = rootProject),
            "ios_test_jobs_matrix.json" to getIosTestTaskList(rootProject = rootProject),
            "js_test_jobs_matrix.json" to getJsTestTaskList(rootProject = rootProject),
            "jvm_test_jobs_matrix.json" to getJvmTestTaskList(rootProject = rootProject)
        )
            .mapValues { entry -> entry.value.map { it.joinToString(separator = " ") } }
            .forEach { (fileName: String, taskList: List<String>) ->
                val matrix = mapOf("gradle_tasks" to taskList)
                val jsonText = gson.toJson(matrix)
                rootProject.layout.buildDirectory.asFile.get().also { buildDir ->
                    buildDir.mkdirs()
                    File(buildDir, fileName).writeText(jsonText)
                }
            }
    }

    fun getIosTestTaskList(rootProject: Project): List<List<String>> =
        rootProject.subprojects.filter { subProject ->
            subProject.name == "test-utils" ||
                    (rootProject.property("${subProject.name}.skipIosTests") == "true").not()
        }.map { subProject ->
            when (val osArch = System.getProperty("os.arch")) {
                "x86", "i386", "ia-32", "i686" -> "${subProject.path}:iosX86Test"
                "x86_64", "amd64", "x64", "x86-64" -> "${subProject.path}:iosX64Test"
                "arm", "arm-v7", "armv7", "arm32",
                "arm64", "arm-v8", "aarch64" -> "${subProject.path}:iosSimulatorArm64Test"

                else -> throw Error("Unexpected System.getProperty(\"os.arch\") = $osArch")
            }
        }.map { listOf("cleanTest", it) }

    fun getJsTestTaskList(rootProject: Project): List<List<String>> =
        rootProject.subprojects.filter { subProject ->
            subProject.name == "test-utils" ||
                    (rootProject.property("${subProject.name}.skipJsTests") == "true").not()
        }.map { subProject ->
            "${subProject.path}:jsTest"
        }.map { listOf("cleanTest", it) }

    fun getJvmTestTaskList(rootProject: Project): List<List<String>> =
        rootProject.subprojects.filter { subProject ->
            subProject.name == "test-utils" ||
                    (rootProject.property("${subProject.name}.skipJvmTests") == "true").not()
        }.map { subProject ->
            "${subProject.path}:jvmTest"
        }.map { listOf("cleanTest", it) }

    fun getEmulatorTaskList(rootProject: Project): List<List<String>> =
        rootProject.subprojects.filter { subProject ->
            File(subProject.projectDir, "src${File.separator}commonTest").exists() ||
                    File(
                        subProject.projectDir,
                        "src${File.separator}androidInstrumentedTest"
                    ).exists()
        }.map { subProject ->
            "${subProject.path}:gradleManagedDeviceDebugAndroidTest"
        }.map { taskName ->
            mutableListOf(taskName).also {
                it.add("--no-parallel")
                it.add("--max-workers=1")
                it.add("-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect")
                it.add("-Pandroid.experimental.testOptions.managedDevices.emulator.showKernelLogging=true")
            }.also {
                if (!true.toString().equals(other = System.getenv("CI"), ignoreCase = true)) {
                    it.add("--enable-display")
                }
            }
        }
}

fun getAndroidSdkPath(rootDir: File): String? =
    Properties().apply {
        val propertiesFile = File(rootDir, "local.properties")
        if (propertiesFile.exists()) {
            load(propertiesFile.inputStream())
        }
    }.getProperty("sdk.dir").let { propertiesSdkDirPath ->
        (propertiesSdkDirPath ?: System.getenv("ANDROID_HOME"))
    }

fun getSdkmanagerFile(rootDir: File): File? =
    getAndroidSdkPath(rootDir = rootDir)?.let { sdkDirPath ->
        println("sdkDirPath: $sdkDirPath")
        val files = File(sdkDirPath).walk().filter { file ->
            file.path.contains("cmdline-tools") && file.path.endsWith("sdkmanager")
        }
        files.forEach { println("walk: ${it.absolutePath}") }
        val sdkmanagerFile = files.firstOrNull()
        println("sdkmanagerFile: $sdkmanagerFile")
        sdkmanagerFile
    }
