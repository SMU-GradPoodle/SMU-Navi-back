plugins {
    java
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
//	id ("com.ewerk.gradle.plugins.querydsl") version "1.0.10"
}

group = "smu.poodle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-batch")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.1")
    implementation("org.springframework.cloud:spring-cloud-starter-aws-secrets-manager-config:2.2.6.RELEASE")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("junit:junit:4.13.2")
    implementation("org.json:json:20230227")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation ("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.11.5")

//	implementation ("com.querydsl:querydsl-jpa")
//	implementation ("com.querydsl:querydsl-apt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
