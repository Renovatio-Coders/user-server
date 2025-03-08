plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.8"
	id("io.spring.dependency-management") version "1.1.7"
	id("maven-publish") // GitHub Packages 및 Maven 배포용 추가
}

group = "coders.renovatio.donghang"

// 환경별 gradle.properties 자동 로드
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

publishing {
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/Renovatio-Coders/toolkit")
			credentials {
				username = System.getenv("GITHUB_USERNAME") ?: ""
				password = System.getenv("GITHUB_TOKEN") ?: ""
			}
		}
	}
	publications {
		create<MavenPublication>("gpr") {
			from(components["java"])
			groupId = "coders.renovatio"
			artifactId = "donghang"
			version = project.version.toString()
		}
	}
}
