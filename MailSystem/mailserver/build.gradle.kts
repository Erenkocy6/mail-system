import org.gradle.kotlin.dsl.withType

val javaJdk = 21
val springBootVersion = "4.0.6"

plugins {
  kotlin("jvm") version "2.2.20"
  kotlin("plugin.spring") version "2.2.20"
  id("org.springframework.boot") version "4.0.1"
  id("io.spring.dependency-management") version "1.1.7"
  application
}

apply(plugin = "idea")
apply(plugin = "java")

group = "de.thm.mni"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(javaJdk)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("tools.jackson.module:jackson-module-kotlin")

  implementation("org.springframework.boot:spring-boot-h2console")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

application {
  // Note: the main class in Kotlin has a "Kt" suffix when compiled,
  // so we need to specify it here for the application plugin to work correctly
  mainClass = "de.thm.mni.mailserver.MailserverApplicationKt"
}
