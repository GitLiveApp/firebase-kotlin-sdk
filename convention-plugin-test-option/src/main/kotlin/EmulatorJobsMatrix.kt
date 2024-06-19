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

    fun createMatrixJsonFile(rootProject: Project) {
        val taskList = getTaskList(rootProject = rootProject).map { it.joinToString(separator = " ") }
        val matrix = mapOf("gradle_tasks" to taskList)
        val jsonText = gson.toJson(matrix)
        rootProject.layout.buildDirectory.asFile.get().also { buildDir ->
            buildDir.mkdirs()
            File(buildDir, "emulator_jobs_matrix.json").writeText(jsonText)
        }
    }

    fun getTaskList(rootProject: Project): List<List<String>> =
        rootProject.subprojects.filter { subProject ->
            File(subProject.projectDir, "src${File.separator}androidInstrumentedTest").exists()
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
