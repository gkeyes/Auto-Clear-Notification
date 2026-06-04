plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.auto.clear.notification"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.auto.clear.notification"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "3.1.1"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
}
