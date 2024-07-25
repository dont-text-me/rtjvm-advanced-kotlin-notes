plugins { kotlin("jvm") }

group = "com.ivanbar"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
    implementation(project(":builder-lib-annotations"))
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }
