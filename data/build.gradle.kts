plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":domain"))

    //hilt
    implementation("com.google.dagger:hilt-android:2.60.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")
    ksp("com.google.dagger:hilt-compiler:2.60.1")

    //media3
    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-ui-compose:1.11.1")
    implementation("androidx.media3:media3-ui-compose-material3:1.11.1")
    implementation("androidx.media3:media3-session:1.11.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.11.0")

    //retrofit
    val retrofitVersion = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.5.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}