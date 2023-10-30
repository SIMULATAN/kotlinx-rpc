import org.jetbrains.krpc.buildutils.kmp

plugins {
    kotlin("multiplatform")
}

kmp {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":krpc-runtime:krpc-runtime-serialization"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
    }
}
