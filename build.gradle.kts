plugins {
    kotlin("jvm") version "2.0.0"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

group = "com.ivanbar"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    implementation(project(":builder-lib-annotations"))
    implementation(project(":builder-lib-impl"))
    ksp(project(":builder-lib-impl"))
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }

spotless {
    kotlin {
        target("**/*.kt", "build/generated/ksp/main/kotlin/com/ivanb/internals/*.kt")
        ktfmt("0.51").kotlinlangStyle()
        ktlint("1.3.1")
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktfmt("0.51").kotlinlangStyle()
    }
}
