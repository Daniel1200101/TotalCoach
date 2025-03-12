plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.totalcoach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.totalcoach"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true // Or vectorDrawables.useSupportLibrary = true if using the AndroidX library.
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
    implementation(libs.places)


    // Firebase AuthUI
    implementation (libs.firebase.ui.auth)

    // Firebase:
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)


    //Realtime DB:
    implementation(libs.firebase.database)

//    //Glide
    implementation(libs.glide)

    //Firestore:
    implementation(libs.firebase.firestore)

    // Storage
    implementation(libs.firebase.storage)

    /// gson
    implementation (libs.gson)

    /// for main app fragment
    implementation(libs.google.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)

    // CalendarView
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)

    //implementation (libs.mpandroidchart)
    implementation (libs.material)






}