package utils

enum class TargetPlatform {
    Android, Ios, Macos, Tvos, Jvm, Js, WasmJs
}

fun String.toTargetPlatforms(): List<TargetPlatform> =
    split(",").map {
        when (it.lowercase().trim()) {
            "android" -> TargetPlatform.Android
            "ios" -> TargetPlatform.Ios
            "macos" -> TargetPlatform.Macos
            "tvos" -> TargetPlatform.Tvos
            "jvm" -> TargetPlatform.Jvm
            "js" -> TargetPlatform.Js
            "wasmjs" -> TargetPlatform.WasmJs
            else -> throw IllegalArgumentException("Unknown target platform: $it")
        }
    }

fun List<TargetPlatform>.supportsApple() = this.any {
    it == TargetPlatform.Ios || it == TargetPlatform.Macos || it == TargetPlatform.Tvos
}
