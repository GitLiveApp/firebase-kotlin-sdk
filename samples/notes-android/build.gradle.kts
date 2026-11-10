plugins {
    id("com.android.application") version "8.12.0" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    // KMP: add the plugins the shared module needs, all `apply false`:
    //   id("org.jetbrains.kotlin.multiplatform") version "2.4.20"
    //   id("com.android.library") version "8.12.0"
    //   id("org.jetbrains.kotlin.plugin.serialization") version "2.4.20"   (maps the model classes on every platform)
}
