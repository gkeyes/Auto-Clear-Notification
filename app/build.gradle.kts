plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.auto.clear.notification"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_KEYSTORE_PATH") ?: "missing-release-keystore.p12")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: ""
            keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: ""
            storeType = "pkcs12"
        }
    }

    defaultConfig {
        applicationId = "com.auto.clear.notification"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "3.1.2"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
