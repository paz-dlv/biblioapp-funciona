plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.biblioapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.biblioapp"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures { // Activamos features del módulo
        viewBinding = true // Generación de clases de binding por layout
        buildConfig = true // Generación de BuildConfig con campos custom
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.recyclerview)


    // Retrofit para consumir API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    // OkHttp (dependency for Retrofit, provides MediaType)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Glide para cargar imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.okhttp) // Cliente HTTP subyacente
    implementation(libs.okhttp.logging) // Interceptor de logging para depuración

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")


    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Material Design
    implementation("com.google.android.material:material:1.10.0")
}