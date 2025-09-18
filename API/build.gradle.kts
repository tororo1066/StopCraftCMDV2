plugins {
    java
    `maven-publish`
}

group = "tororo1066"

val pluginVersion: String by project.ext

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations-java5:24.1.0")
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = project.group.toString()
            artifactId = "stop-craft-cmd"
            version = System.getenv("VERSION")
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/tororo1066/StopCraftCMDV2")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}