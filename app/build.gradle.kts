import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Load properties from local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "org.ilerna.song_swipe_frontend"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.ilerna.song_swipe_frontend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose properties as BuildConfig fields
        // DEV environment
        buildConfigField("String", "SUPABASE_URL_DEV", "\"${localProperties.getProperty("SUPABASE_URL_DEV", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY_DEV", "\"${localProperties.getProperty("SUPABASE_ANON_KEY_DEV", "")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_ID_DEV", "\"${localProperties.getProperty("SPOTIFY_CLIENT_ID_DEV", "")}\"")

        // TEST environment
        buildConfigField("String", "SUPABASE_URL_TEST", "\"${localProperties.getProperty("SUPABASE_URL_TEST", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY_TEST", "\"${localProperties.getProperty("SUPABASE_ANON_KEY_TEST", "")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_ID_TEST", "\"${localProperties.getProperty("SPOTIFY_CLIENT_ID_TEST", "")}\"")

        // Current active environment (default to DEV)
        buildConfigField("String", "ACTIVE_ENVIRONMENT", "\"${localProperties.getProperty("ACTIVE_ENVIRONMENT", "DEV")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Authentication (Supabase handles Spotify OAuth via browser)
    implementation(libs.postgrest.kt)
    implementation(libs.auth.kt)

    // Serialization (required for Supabase Postgrest DTOs)
    implementation(libs.kotlinx.serialization.json)

    // Networking
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)

    // Retrofit for REST API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp for HTTP client
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Gson for JSON parsing
    implementation(libs.gson)

    // Coil for image loading (Compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Material Icons Extended (for Icons.Filled.*, Icons.Outlined.*)
    implementation(libs.androidx.compose.material.icons.extended)


    // Testing - Unit Tests
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    // Testing - Android Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Lifecycle ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

}