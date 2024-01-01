plugins {
    java
}

group = "dev.foxikle"
version = "0.1"
description = "testing!"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri ("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri ("https://oss.sonatype.org/content/groups/public/")
    }
    maven ("https://jitpack.io")
}

dependencies {
    compileOnly ("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    jar{
        destinationDirectory.set(file("C:/Users/tscal/Desktop/testserver/plugins"))
    }
}
