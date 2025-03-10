plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.8"
	id("io.spring.dependency-management") version "1.1.7"
	id("maven-publish") // GitHub Packages 및 Maven 배포용 추가
}

group = "coders.renovatio.donghang"

val env = System.getenv("ENVIRONMENT") ?: findProperty("ENVIRONMENT")?.toString() ?: "local"
val branch = System.getenv("GITHUB_REF_NAME") ?: "dev"

// `jpa-common` 브랜치별 버전 설정
val jpaCommonVersion = when (branch) {
	"main" -> "0.0.1-RELEASE"
	"dev" -> "0.0.1-SNAPSHOT"
	else -> "0.0.1-SNAPSHOT"
}
println("Using jpa-common version: $jpaCommonVersion")

val finalVersion = findProperty("VERSION")?.toString() ?: "0.0.1-SNAPSHOT"
version = finalVersion

repositories {
	mavenCentral()
	mavenLocal()
	maven {
		url = uri("https://maven.pkg.github.com/Renovatio-Coders/toolkit")
		credentials {
			username = System.getenv("GH_USERNAME") ?: findProperty("GH_USERNAME")?.toString()
			password = System.getenv("GH_PAT") ?: findProperty("GH_PAT")?.toString()
		}
	}
}

dependencies {
	implementation("com.renovatio.toolkit:jpa-common:$jpaCommonVersion")
}
