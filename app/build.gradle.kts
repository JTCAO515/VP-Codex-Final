plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
    .orElse(providers.environmentVariable("SUPABASE_URL"))
    .orElse("")
    .get()

val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY")
    .orElse(providers.environmentVariable("SUPABASE_ANON_KEY"))
    .orElse("")
    .get()

val supabaseRedirectScheme = providers.gradleProperty("SUPABASE_REDIRECT_SCHEME")
    .orElse(providers.environmentVariable("SUPABASE_REDIRECT_SCHEME"))
    .orElse("space.jtcao.visepanda")
    .get()

val supabaseRedirectHost = providers.gradleProperty("SUPABASE_REDIRECT_HOST")
    .orElse(providers.environmentVariable("SUPABASE_REDIRECT_HOST"))
    .orElse("auth-callback")
    .get()

android {
    namespace = "space.jtcao.visepanda"
    compileSdk = 34

    defaultConfig {
        applicationId = "space.jtcao.visepanda"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "3.0.8"

        buildConfigField("String", "SUPABASE_URL", supabaseUrl.asBuildConfigString())
        buildConfigField("String", "SUPABASE_ANON_KEY", supabaseAnonKey.asBuildConfigString())
        buildConfigField("String", "SUPABASE_REDIRECT_SCHEME", supabaseRedirectScheme.asBuildConfigString())
        buildConfigField("String", "SUPABASE_REDIRECT_HOST", supabaseRedirectHost.asBuildConfigString())

        manifestPlaceholders["supabaseRedirectScheme"] = supabaseRedirectScheme
        manifestPlaceholders["supabaseRedirectHost"] = supabaseRedirectHost
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Local storage
    implementation(libs.androidx.datastore.preferences)

    // Map
    implementation(libs.osmdroid.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Supabase Auth
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.4"))
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:compose-auth")
    implementation("io.ktor:ktor-client-android:2.3.12")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
