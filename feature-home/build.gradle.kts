plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "zechs.zplex.feature_home"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFiles("consumer-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}


dependencies {
    implementation(project(":zplex-api"))

    val lifecycleVersion = "2.10.0"
    val hiltVersion = "2.59.2"
    val navigationVersion = "2.9.7"

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

// --- Lifecycle & Navigation ---
    implementation("androidx.lifecycle:lifecycle-service:${lifecycleVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:${navigationVersion}")

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}