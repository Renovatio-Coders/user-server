plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.8"
	id("io.spring.dependency-management") version "1.1.7"
	id("maven-publish") // GitHub Packages 및 Maven 배포용 추가
}

group = "coders.renovatio.donghang"

// 환경 변수 또는 `gradle.properties`에서 ENVIRONMENT 로드
val env = System.getenv("ENVIRONMENT") ?: findProperty("ENVIRONMENT")?.toString() ?: "local"
val propertiesFile = rootProject.file("gradle/profiles/${env}.properties")

if (propertiesFile.exists()) {
	propertiesFile.forEachLine { line ->
		val (key, value) = line.split("=").map { it.trim() }
		project.extensions.extraProperties[key] = value
	}
	println("Loaded properties from: gradle/profiles/${env}.properties")
} else {
	println("No specific properties file found for environment: $env. Using default settings.")
}

// 브랜치별 `jpa-common` 버전 동적 설정
val branch = System.getenv("GITHUB_REF_NAME") ?: "dev"
val jpaCommonVersion = when (branch) {
	"main" -> "0.0.1-RELEASE"
	"dev" -> "0.0.1-SNAPSHOT"
	else -> "0.0.1-SNAPSHOT"
}
println("Using jpa-common version: $jpaCommonVersion")

// `VERSION` 기본값 설정
val finalVersion = findProperty("VERSION")?.toString() ?: "0.0.1-SNAPSHOT"
version = finalVersion

// 최종 환경 설정 로그
println("Using Gradle Profile: $env, Version: $finalVersion")

// 환경별 Gradle 설정 적용
apply(from = "gradle/profiles/${env}.gradle.kts")

extra["springCloudVersion"] = "2023.0.5"

repositories {
	mavenCentral()
	mavenLocal()
	maven {
		url = uri("https://maven.pkg.github.com/Renovatio-Coders/toolkit")
		credentials {
			username = System.getenv("GITHUB_ACTOR") ?: System.getenv("GH_USERNAME") ?: ""
			password = System.getenv("GITHUB_TOKEN") ?: System.getenv("GH_PAT") ?: ""
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// 브랜치별 동적 `jpa-common` 의존성 추가
	implementation("com.renovatio.toolkit:jpa-common:$jpaCommonVersion")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
