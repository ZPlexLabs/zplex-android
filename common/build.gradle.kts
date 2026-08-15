plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "zechs.zplex.common"
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    val hiltVersion = "2.60.1"
    val datastoreVersion = "1.2.1"
    val navigationVersion = "2.9.7"
    val gsonVersion = "2.14.0"
    val okhttpVersion = "5.4.0"
    val moshiVersion = "1.15.2"
    val retrofitVersion = "3.0.0"
    val composeBomVersion = "2025.08.01"
    val coroutinesVersion = "1.11.0"
    val lifecycleVersion = "2.10.0"
    val navigationComposeVersion = "2.9.7"

    // --- Networking ---
    implementation(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    // --- JSON Parsing ---
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    implementation("com.google.code.gson:gson:${gsonVersion}")

    // --- Persistence ---
    implementation("androidx.datastore:datastore-preferences:${datastoreVersion}")

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-fragment-ktx:${navigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${navigationVersion}")

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3-window-size-class")

    // --- Coroutines / Lifecycle / Navigation (Compose) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.navigation:navigation-compose:$navigationComposeVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}