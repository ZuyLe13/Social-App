buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.google.services);
        classpath("com.google.gms:google-services:4.3.0")
        classpath("com.android.tools.build:gradle")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false

    id("com.google.gms.google-services") version "4.4.1" apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

}
