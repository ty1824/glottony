plugins {
    idea
    java
    kotlin("jvm") apply false
    id("org.jetbrains.kotlinx.kover")
}

allprojects {
    repositories {
        mavenCentral()
    }
}


val installLocalServer by tasks.registering(Copy::class)