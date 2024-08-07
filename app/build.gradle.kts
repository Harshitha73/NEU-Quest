plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.northeastern.numad24su_group9"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.northeastern.numad24su_group9"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.coordinatorlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.activity)
    implementation(libs.firebase.analytics)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.database)
    implementation(libs.picasso)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.generativeai)
    implementation(libs.material.v130alpha02)
    implementation(libs.guava)
}

apply(plugin = "com.google.gms.google-services")
