plugins {
    idea
    java
    kotlin("jvm") apply false
    id("org.jetbrains.kotlinx.kover")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}


val installLocalServer by tasks.registering(Copy::class)