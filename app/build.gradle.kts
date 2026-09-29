import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
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
    "io/github/mipmip/beansondroid/BeansOnDroidApplication*",
    "io/github/mipmip/beansondroid/repo/AndroidGit*",
    "io/github/mipmip/beansondroid/repo/AndroidSystemReader*",
    "io/github/mipmip/beansondroid/store/RepoRegistry*",
    "io/github/mipmip/beansondroid/store/KeystoreTokenVault*",
)

/**
 * The single source of truth for the app's version. Shell reads it with `cat`,
 * Gradle reads it here, and `scripts/release.sh` rewrites it.
 */
private fun readVersionName(): String {
    val file = rootProject.file("VERSION")
    if (!file.isFile) {
        throw GradleException("VERSION is missing at ${file.path}. It must hold major.minor.patch.")
    }
    val text = file.readText().trim()
    if (!Regex("""^\d+\.\d+\.\d+$""").matches(text)) {
        throw GradleException("VERSION must be major.minor.patch, found \"$text\" in ${file.path}.")
    }
    return text
}

/**
 * F-Droid orders releases by versionCode and it can never decrease. The
 * rebuild slot exists for republishing the same version after a bad artefact,
 * which a purely derived code could not otherwise survive.
 */
private fun versionCodeFor(name: String): Int {
    val (major, minor, patch) = name.split('.').map(String::toInt)
    val rebuild = (System.getenv("BEANS_VERSION_REBUILD") ?: "0").toIntOrNull()
        ?: throw GradleException("BEANS_VERSION_REBUILD must be a whole number.")
    if (rebuild !in 0..99) {
        throw GradleException("BEANS_VERSION_REBUILD must be between 0 and 99, found $rebuild.")
    }
    return major * 1_000_000 + minor * 10_000 + patch * 100 + rebuild
}

private class ReleaseSigning(
    val store: File,
    val storePassword: String,
    val alias: String,
    val keyPassword: String,
)

/**
 * A local `keystore.properties` first, then the environment.
 *
 * The file comes first deliberately. The Gradle daemon caches the environment
 * it started with, so a long-running daemon cannot see variables exported for
 * a later invocation, and the build would quietly produce an unsigned APK.
 * A file is read every time. CI always has a fresh daemon, so the environment
 * path is the one it uses.
 */
private fun releaseSigning(propertiesFile: File): ReleaseSigning? {
    val fromFile: Properties? = propertiesFile.takeIf { it.isFile }?.let { file ->
        val loaded = Properties()
        file.inputStream().use { loaded.load(it) }
        loaded
    }

    fun value(fileKey: String, env: String): String? =
        fromFile?.getProperty(fileKey)?.takeIf { it.isNotBlank() }
            ?: System.getenv(env)?.takeIf { it.isNotBlank() }

    val storePath = value("storeFile", "BEANS_KEYSTORE") ?: return null
    val store = File(storePath).let { if (it.isAbsolute) it else File(propertiesFile.parentFile, storePath) }

    if (!store.isFile) {
        throw GradleException(
            "Release signing was configured but the keystore is not at ${store.path}. " +
                "Fix the path or remove the setting; a half-configured key must not " +
                "silently produce an unsigned APK.",
        )
    }

    val storePassword = value("storePassword", "BEANS_KEYSTORE_PASSWORD")
    val alias = value("keyAlias", "BEANS_KEY_ALIAS")
    val keyPassword = value("keyPassword", "BEANS_KEY_PASSWORD") ?: storePassword

    val missing = buildList {
        if (storePassword.isNullOrBlank()) add("storePassword / BEANS_KEYSTORE_PASSWORD")
        if (alias.isNullOrBlank()) add("keyAlias / BEANS_KEY_ALIAS")
    }
    if (missing.isNotEmpty()) {
        throw GradleException(
            "Release signing is half configured. The keystore at ${store.path} was found " +
                "but these are missing: ${missing.joinToString(", ")}.",
        )
    }

    return ReleaseSigning(store, storePassword!!, alias!!, keyPassword!!)
}

private val appVersionName = readVersionName()
private val appVersionCode = versionCodeFor(appVersionName)

private val corePackages = listOf(
    "io.github.mipmip.beansondroid.bean",
    "io.github.mipmip.beansondroid.index",
)

android {
    namespace = "io.github.mipmip.beansondroid"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "io.github.mipmip.beansondroid"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val release = releaseSigning(rootProject.file("keystore.properties"))

    signingConfigs {
        if (release != null) {
            create("release") {
                storeFile = release.store
                storePassword = release.storePassword
                keyAlias = release.alias
                keyPassword = release.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Absent key means an unsigned build, so a contributor without the
            // key can still build the release variant.
            signingConfig = release?.let { signingConfigs.getByName("release") }
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
        buildConfig = true
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.jgit)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.zxing.core)
    implementation(libs.markwon.core)
    implementation(libs.markwon.tables)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.tasklist)
    implementation(libs.snakeyaml)

    testImplementation(libs.junit)
    testImplementation(libs.zxing.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.jgit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.lifecycle.runtime.testing)
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
            element = "PACKAGE"
            includes = corePackages
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
