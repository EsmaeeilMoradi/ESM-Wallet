plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
    }
}
// Keep this block for explicit version forcing, even if you exclude one.
// This ensures that if another library accidentally pulls in a conflicting version,
// Gradle knows which one to prioritize.
configurations.all {
    resolutionStrategy {
        // Force the specific bcprov version you want to use (e.g., 1.69)
        // This will ensure that only the "jdk15to18" version is used if it's the one we want to keep.
        // It's generally better to let Gradle resolve dependencies naturally,
        // but for deep conflicts like this, forcing helps.
        force("org.bouncycastle:bcprov-jdk15to18:1.69")
        force("org.bouncycastle:bcpkix-jdk15to18:1.69") // Make sure to force bcpkix-jdk15to18 as well if it causes issues
    }
}
dependencies {
    // Import the Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // AndroidX Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.web3j.core)

    //QR Code generation
    implementation(libs.zxing.android.embedded)
    // Core ZXing library
    implementation(libs.core.zxing)
    // BitcoinJ with more specific exclusions for Bouncy Castle
    implementation(libs.bitcoinj.core) {
        // Exclude problematic transitive dependencies
        exclude(group = "org.scrypt")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.checkerframework")

        // Exclude ALL bouncycastle related modules that might conflict.
        // The error shows 'bcprov-jdk15on' and 'bcprov-jdk15to18' conflicting.
        // We'll exclude the 'jdk15on' version as it's often the older/less compatible one for modern Android.
        // We'll also exclude bcpkix (related to bouncycastle) to be safe.
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on") // Exclude the corresponding bcpkix as well
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18") // Exclude this as well from bitcoinj if web3j provides it
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15to18") // Exclude this as well from bitcoinj if web3j provides it
    }

    // Debugging and Testing Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Unit & Instrumentation Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

}