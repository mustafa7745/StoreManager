plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.8.10"
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.fekraplatform.storemanger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fekraplatform.storemanger"
        minSdk = 23
        targetSdk = 35
        versionCode = 24
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:/Persons/Mustafa/key1.jks")
            storePassword = "Mu771212577"
            keyAlias = "key0"
            keyPassword = "Mu771212577"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation ("io.coil-kt:coil-svg:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.firebase:firebase-config")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation (libs.android.maps.utils)

    val room_version = "2.6.1"
//    implementation("com.github.arpitkatiyar1999:Country-Picker:1.3.0")

//    implementation("com.eygraber:compose-country-code-picker:0.5.1")
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
//    implementation(libs.firebase.admin)
    implementation("androidx.room:room-ktx:$room_version")

    val billing_version = "7.1.1"

    implementation("com.android.billingclient:billing:$billing_version")

    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")


    implementation ("com.google.dagger:hilt-android:2.56.2")
    ksp ("com.google.dagger:hilt-compiler:2.56.2")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.security:security-crypto:1.1.0-beta01")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("com.google.crypto.tink:tink-android:1.8.0")
    implementation ("androidx.compose.material:material-icons-extended")



}