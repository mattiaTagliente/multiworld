import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency


plugins {
    id ("fabric-loom") version "1.10-SNAPSHOT"
    id ("maven-publish")
	id ("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

base {
    archivesBaseName = "Multiworld-Fabric"
    version = "1.21.8"
    group = "me.isaiah.mods"
}

repositories {
	// Fantasy 1.21
    maven("https://maven.nucleoid.xyz/")
	mavenLocal()
}

dependencies {
    annotationProcessor("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")
    compileOnly("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")

    minecraft("com.mojang:minecraft:1.21.8")
    mappings("net.fabricmc:yarn:1.21.8+build.1")
    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    // Use the latest compatible Fantasy library
    include("xyz.nucleoid:fantasy:0.6.7+1.21.5")
    modImplementation("xyz.nucleoid:fantasy:0.6.7+1.21.5")

    modImplementation("curse.maven:cyber-permissions-407695:4640544")
    modImplementation("me.lucko:fabric-permissions-api:0.3.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:0.131.0+1.21.8")

    // This is the updated list including the command API
    setOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
        "fabric-events-interaction-v0",
        "fabric-command-api-v2" // <-- THE ADDED LINE
    ).forEach {
        modImplementation(fabricApi.module(it, "0.131.0+1.21.8"))
    }

    val ic = DefaultExternalModuleDependency(
        "com.javazilla.mods",
        "icommon-fabric-1.21.4",
        "1.21.4",
        null
    ).apply {
        isChanging = true
    }

    modImplementation(ic)
}

// Note: dimapi is not needed for 1.21
sourceSets {
    main {
        java {
            srcDir("${rootProject.projectDir}/Multiworld-Common/src/main/java/com")
            srcDir("src/main/java")
			exclude("**/dimapi/*.java")
			exclude("**/dimapi/*.class")
			exclude("**/dimapi/mixin/*.java")
			exclude("**/dimapi/mixin/*.class")
        }
        resources {
            srcDir("${rootProject.projectDir}/Multiworld-Common/src/main/resources")
			exclude("**/dimapi/*.java")
			exclude("**/dimapi/*.class")
			exclude("**/dimapi/mixin/*.java")
			exclude("**/dimapi/mixin/*.class")
        }
    }
}

// Jabel
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString() // for the IDE support
    options.release.set(17)

    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}

/*configure([tasks.compileJava]) {
    sourceCompatibility = 16 // for the IDE support
    options.release = 8

    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(16)
    }
}*/

//tasks.getByName("compileJava") {
    //sourceCompatibility = 16
    //options.release = 8
//}


tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.INHERIT }

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

tasks.named("build") { finalizedBy("copyReport2") }

tasks.register<Copy>("copyReport2") {
    from(remapJar)
    into("${project.rootDir}/output")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()
            
            pom {
                name.set(project.name.lowercase())
                description.set("A concise description of my library")
                url.set("http://www.example.com/")
            }

            artifact(remapJar)
        }
    }

    repositories {
        val mavenUsername: String? by project
        val mavenPassword: String? by project
        mavenPassword?.let {
            maven(url = "https://repo.codemc.io/repository/maven-releases/") {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
}