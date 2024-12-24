import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

val tmdbApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("TMDB_API_KEY")

android {
    namespace = "zechs.zplex"
    compileSdk = 35

    defaultConfig {
        applicationId = "zechs.zplex"
        minSdk = 28
        targetSdk = 35
        versionCode = 22
        versionName = "4.0.0"

        buildConfigField("String", "TMDB_API_KEY", "\"${tmdbApiKey}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = false
        }
    }

}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("extension-*.aar"))))
    implementation(project(":mpv"))

    // Dependency versions
    val hiltVersion = "2.50"
    val moshiVersion = "1.15.0"
    val retrofitVersion = "2.9.0"
    val roomVersion = "2.6.1"
    val lifecycleVersion = "2.8.7"
    val glideVersion = "4.16.0"
    val okhttpVersion = "4.12.0"

    // Core dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.core:core-ktx:1.15.0")

    // UI-related dependencies
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Dagger Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-common:1.2.0")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // Dagger Hilt for navigation
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // Networking dependencies
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    // JSON parsing with Moshi
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // Coil for image loading
    implementation("io.coil-kt:coil:2.5.0")

    // Glide for image loading
    // DO NOT UPGRADE Glide to versions beyond 4.13.2
    implementation("com.github.bumptech.glide:glide:$glideVersion")

    // Renderscript intrinsics replacement toolkit
    implementation("com.github.necatisozer:renderscript-intrinsics-replacement-toolkit:0.8-beta")

    // Room database
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Networking with OkHttp
    implementation(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Lifecycle and Navigational Components
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}