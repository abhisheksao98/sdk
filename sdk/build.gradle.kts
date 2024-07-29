
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("maven-publish")

}

android {
    namespace = "com.fstac.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
afterEvaluate { ->
    publishing {
        publications {
            create<MavenPublication>("aar") {
                groupId = "com.fstac"
                artifactId = "deviceutil"
                version = "0.0.2"
                // Specify the AAR file to publish
                artifact(layout.buildDirectory.file("outputs/aar/sdk-debug.aar"))
            }
        }
        repositories {
            maven {

                name = "Package"
                url = uri("https://maven.pkg.github.com/abhisheksao98/sdk")
               credentials {
                     credentials.username = "abhisheksao98"
                   password = "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"
                }
            }


        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}