plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.1.2")
    implementation(kotlin("compiler-embeddable", "1.6.10"))
    implementation(kotlin("gradle-plugin", "1.6.10"))
}