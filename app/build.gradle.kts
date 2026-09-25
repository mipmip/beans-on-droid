plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*_Factory*.*",
    "**/*ComposableSingletons*.*",
    "**/*\$*\$*.*",
    "io/github/mipmip/beansondroid/ui/**",
    "io/github/mipmip/beansondroid/MainActivity*",
)

private val corePackages = listOf(
    "io/github/mipmip/beansondroid/bean/**",
    "io/github/mipmip/beansondroid/index/**",
)

android {
    namespace = "io.github.mipmip.beansondroid"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "io.github.mipmip.beansondroid"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.jgit)
    implementation(libs.snakeyaml)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.jgit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.jgit)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

private val kotlinClassesDir = "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
private val javaClassesDir = "intermediates/javac/debug/compileDebugJavaWithJavac/classes"

private fun Project.coverageClassTree() =
    fileTree(layout.buildDirectory.dir(kotlinClassesDir)) {
        exclude(coverageExclusions)
    } + fileTree(layout.buildDirectory.dir(javaClassesDir)) {
        exclude(coverageExclusions)
    }

private fun Project.coverageExecutionData() =
    fileTree(layout.buildDirectory.dir("jacoco")) { include("*.exec") } +
        fileTree(layout.buildDirectory.dir("outputs/unit_test_code_coverage")) {
            include("**/*.exec", "**/*.ec")
        }

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Coverage report for the debug unit tests."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
    classDirectories.setFrom(coverageClassTree())
    executionData.setFrom(coverageExecutionData())
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "verification"
    description = "Fails the build when coverage drops below the project floor."
    dependsOn("testDebugUnitTest")

    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
    classDirectories.setFrom(coverageClassTree())
    executionData.setFrom(coverageExecutionData())

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "BUNDLE"
            includes = listOf("*")
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            classDirectories.setFrom(
                fileTree(layout.buildDirectory.dir(kotlinClassesDir)) {
                    include(corePackages)
                    exclude(coverageExclusions)
                }
            )
        }
    }
}
