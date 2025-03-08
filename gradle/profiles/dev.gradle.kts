repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/Renovatio-Coders/toolkit")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.renovatio.toolkit:jpa-common:0.0.1-DEV-SNAPSHOT") // `dev` 브랜치 패키지 참조
}
