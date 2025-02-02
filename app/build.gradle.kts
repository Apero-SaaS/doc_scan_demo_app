plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.apero.app.poc_ml_docscan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.apero.app.poc_ml_docscan1"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val cameraxVersion = "1.3.0"
    // implementation("androidx.camera:camera-mlkit-vision:$cameraxVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.3.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.3.0")
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))

    testImplementation("junit:junit:4.13.2")
}
