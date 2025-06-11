import java.io.FileInputStream
import java.util.Properties

plugins {
    // Android Application Plugin: Essential for Android projects.
    alias(libs.plugins.android.application)
    // Kotlin Android Plugin: Enables Kotlin features for Android.
    alias(libs.plugins.kotlin.android)
    // Kotlin Compose Plugin: Enables Jetpack Compose features.
    alias(libs.plugins.kotlin.compose)
    // Kotlin Symbol Processing (KSP) Plugin: Used for annotation processing (e.g., Room, Hilt).
    alias(libs.plugins.kotlinAndroidKsp)
    // ADDED FOR HILT: Apply the Hilt Android Gradle plugin to the app module.
//    alias(libs.plugins.hiltAndroid)
}

fun getLocalProperty(propertyName: String): String {
    val properties = Properties()
    val localPropertiesFile = File(project.rootProject.rootDir, "local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { input ->
            properties.load(input)
        }
    }
    return properties.getProperty(propertyName) ?: ""
}
android {
    namespace = "com.esm.esmwallet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.esm.esmwallet"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Retrieve API keys and URLs from local.properties for security.
        val etherscanApiKey: String = getLocalProperty("ETHERSCAN_API_KEY")
        val alchemyNodeUrl: String = getLocalProperty("ALCHEMY_NODE_URL")
        val alchemyNodeUrlMainnet: String = getLocalProperty("ALCHEMY_NODE_URL_MAINNET")

        // Define build config fields to make these properties accessible in app code.
        buildConfigField("String", "ETHERSCAN_API_KEY", "\"$etherscanApiKey\"")
        buildConfigField("String", "ALCHEMY_NODE_URL", "\"$alchemyNodeUrl\"")
        buildConfigField("String", "ALCHEMY_NODE_URL_MAINNET", "\"$alchemyNodeUrlMainnet\"")

    }
    buildFeatures {
        buildConfig = true
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            /*
            val etherscanApiKey: String = getLocalProperty("ETHERSCAN_API_KEY")
            val alchemyNodeUrl: String = getLocalProperty("ALCHEMY_NODE_URL")

            buildConfigField("String", "ETHERSCAN_API_KEY", "\"$etherscanApiKey\"")
            buildConfigField("String", "ALCHEMY_NODE_URL", "\"$alchemyNodeUrl\"")

             */
        }

    }
    // Configure Java compatibility for source and target bytecode.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Configure Kotlin compilation options.
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- Compose BOM (Bill of Materials) ---
    // Manages Compose library versions to ensure compatibility.
    implementation(platform(libs.androidx.compose.bom))

    // This will force all transitive lifecycle components to the version defined in lifecycleBom
//    implementation(platform(libs.androidx.lifecycle.bom))

    // --- AndroidX Core Libraries ---
    implementation(libs.androidx.core.ktx)
    // Lifecycle components - their versions are now managed by lifecycle-bom
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.lifecycle.livedata.ktx)
//    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // --- Jetpack Compose UI Libraries ---
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)


    // --- Compose Tooling (for Preview and Debugging) ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)


    // --- Kotlin Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // --- Web3j Libraries (Ethereum Blockchain Interaction) ---
    implementation(libs.web3j.core)
    implementation(libs.abi)
    implementation(libs.utils)
    implementation(libs.parity)
    implementation(libs.crypto)
    implementation(libs.bcprov.jdk15on)
    implementation(libs.bcpkix.jdk15on)

    // --- QR Code Generation ---
    implementation(libs.zxing.android.embedded)
    implementation(libs.core.zxing)

    // --- Networking (Retrofit) ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // --- Room Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- DataStore (Preferences) ---
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // --- Android Security (for Encrypted DataStore) ---
    implementation(libs.androidx.security.crypto)

    // --- Accompanist Libraries ---
    implementation(libs.accompanist.flowlayout)

    // --- Unit & Instrumentation Tests ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // --- Dagger Hilt Dependencies (ADDED FOR HILT) ---
    // Core Hilt library for Android
//    implementation(libs.hilt.android)
//    // Hilt annotation processor (ksp for Kotlin)
//    ksp(libs.hilt.android.compiler)
//    ksp(libs.hilt.compiler)
//    // Hilt integration with Navigation Compose for ViewModels
//    implementation(libs.hilt.navigation.compose)
//    // Hilt for Android test support
//    androidTestImplementation(libs.hilt.android.testing)
//    kspAndroidTest(libs.hilt.android.compiler)
//    kspAndroidTest(libs.hilt.compiler)


}