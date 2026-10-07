plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.ledgr"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.ledgr"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.register("checkInternetPermission") {
    val manifestFile = layout.buildDirectory.file("intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml")
    dependsOn("processDebugMainManifest")
    doLast {
        val manifest = manifestFile.get().asFile
        if (manifest.exists() && manifest.readText().contains("android.permission.INTERNET")) {
            throw GradleException("INTERNET permission is forbidden in this app!")
        }
    }
}

afterEvaluate {
    tasks.named("assembleDebug") {
        dependsOn("checkInternetPermission")
    }
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":data"))
    implementation(project(":core-ui"))
    implementation(project(":ml"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
