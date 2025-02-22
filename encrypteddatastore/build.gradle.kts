plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kapt)
}

android {
    namespace = "pl.lejdi.encrypteddatastore"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    //hilt
    implementation(libs.android.hilt)
    kapt(libs.android.hilt.compiler)

    //datastore
    implementation(libs.androidx.datastore)

    //gson
    implementation(libs.gson)

}