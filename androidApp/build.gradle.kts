plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

android {
    namespace = "com.debanshu777.flash"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.debanshu777.flash"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.uiTooling)
}
