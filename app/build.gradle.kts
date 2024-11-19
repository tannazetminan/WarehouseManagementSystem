plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.warehousemanagementsystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.warehousemanagementsystem"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit for making API calls
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter for JSON
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0") // Optional for logging
    implementation("com.squareup.picasso:picasso:2.71828") // For loading images
    implementation("com.google.code.gson:gson:2.8.8") // Gson library
    implementation ("com.github.bumptech.glide:glide:4.15.1")

}