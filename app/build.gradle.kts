import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

val tmdbApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("TMDB_API_KEY")
val omdbApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("OMDB_API_KEY")

android {
    namespace = "zechs.zplex"
    compileSdk = 36

    defaultConfig {
        applicationId = "zechs.zplex"
        minSdk = 31
        targetSdk = 36
        versionCode = 22
        versionName = "4.0.0"

        buildConfigField("String", "TMDB_API_KEY", "\"${tmdbApiKey}\"")
        buildConfigField("String", "OMDB_API_KEY", "\"${omdbApiKey}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            manifestPlaceholders["icon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["roundIcon"] = "@mipmap/ic_launcher_debug_round"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["icon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["roundIcon"] = "@mipmap/ic_launcher_round"
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
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
    // --- Local Modules and AAR/JARs ---
    implementation(project(":mpv"))
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("extension-*.aar", "*.jar")
            )
        )
    )

    // --- Version Variables ---
    val appCompatVersion = "1.7.1"
    val coilVersion = "2.7.0"
    val constraintLayoutVersion = "2.2.1"
    val coroutinesVersion = "1.10.2"
    val datastoreVersion = "1.1.7"
    val espressoVersion = "3.7.0"
    val glideVersion = "4.16.0"
    val gsonVersion = "2.13.1"
    val hiltVersion = "2.57.1"
    val hiltExtVersion = "1.2.0"
    val junitVersion = "4.13.2"
    val kotlinCoreVersion = "1.17.0"
    val androidXActivity = "1.10.1"
    val lifecycleVersion = "2.9.3"
    val materialVersion = "1.12.0"
    val moshiVersion = "1.15.2"
    val navigationVersion = "2.9.3"
    val okhttpVersion = "5.1.0"
    val paletteVersion = "1.0.0"
    val renderscriptToolkitVersion = "b6363490c3"
    val retrofitVersion = "3.0.0"
    val roomVersion = "2.7.2"
    val testExtJunitVersion = "1.3.0"
    val workVersion = "2.10.3"
    val mediaVersion = "1.7.1"

    // Media Session
    implementation("androidx.media:media:$mediaVersion")

    // --- AndroidX Core ---
    implementation("androidx.core:core-ktx:$kotlinCoreVersion")
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.palette:palette-ktx:$paletteVersion")
    implementation("androidx.activity:activity-ktx:$androidXActivity")

    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-common:$hiltExtVersion")
    implementation("androidx.hilt:hilt-navigation-fragment:$hiltExtVersion")
    implementation("androidx.hilt:hilt-work:$hiltExtVersion")

    // --- Networking ---
    implementation(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    // --- JSON Parsing ---
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    // --- Image Loading ---
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("io.coil-kt:coil:$coilVersion")

    // --- Persistence ---
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // --- Lifecycle & Navigation ---
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // --- WorkManager ---
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // --- Renderscript Replacement ---
    //noinspection Aligned16KB
    implementation("com.github.android:renderscript-intrinsics-replacement-toolkit:$renderscriptToolkitVersion")

    // --- Testing ---
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$testExtJunitVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
}