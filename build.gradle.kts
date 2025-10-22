import org.flywaydb.gradle.task.FlywayMigrateTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-mysql:10.21.0")
    }
}

plugins {
    java
    id("com.diffplug.spotless") version "6.25.0"
    id("org.flywaydb.flyway") version "10.21.0"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.esop"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    implementation("org.flywaydb:flyway-core:10.21.0")
    implementation("org.flywaydb:flyway-mysql:10.21.0")
    implementation("mysql:mysql-connector-java:8.0.33")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Plugins
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.vault:spring-vault-core:3.1.1")
    implementation("io.minio:minio:8.5.7")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

apply(from = "gradle/quality/spotless.gradle")

tasks.register<FlywayMigrateTask>("migrateDb") {
    url = System.getenv("DB_URL") ?: "jdbc:mysql://localhost:3306/esop"
    user = System.getenv("DB_USER") ?: "root"
    password = System.getenv("DB_PASSWORD") ?: "password"
}
