import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinAndroidKsp)
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

        val etherscanApiKey: String = getLocalProperty("ETHERSCAN_API_KEY")
        val alchemyNodeUrl: String = getLocalProperty("ALCHEMY_NODE_URL")
        val alchemyNodeUrlMainnet: String = getLocalProperty("ALCHEMY_NODE_URL_MAINNET")

        buildConfigField("String", "ETHERSCAN_API_KEY", "\"$etherscanApiKey\"")
        buildConfigField("String", "ALCHEMY_NODE_URL", "\"$alchemyNodeUrl\"")
        buildConfigField("String", "ALCHEMY_NODE_URL_MAINNET", "\"$alchemyNodeUrlMainnet\"")

    }
    buildFeatures {
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

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // ** ROOM DATABASE **
    // Room Runtime
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.abi)
    implementation(libs.utils)
    implementation(libs.parity)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.security.crypto)
    implementation(libs.crypto)
    implementation(libs.bcprov.jdk15on)
    implementation(libs.bcpkix.jdk15on)
    ksp(libs.androidx.room.compiler)





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