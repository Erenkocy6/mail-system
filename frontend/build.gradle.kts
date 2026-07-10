import com.github.gradle.node.npm.task.NpmTask
import java.io.ByteArrayOutputStream

plugins {
    base
    idea
    id("com.github.node-gradle.node") version "7.1.0"
}

node {
    download.set(true)
    version.set("24.12.0")
    npmVersion.set("11.6.2")
    nodeProjectDir.set(projectDir)
}

val npmBuild by tasks.registering(NpmTask::class) {
    group = "build"
    description = "Builds the Angular frontend for production."
    dependsOn("npmInstall")
    workingDir.set(projectDir)
    args.set(listOf("run", "build"))
}

val npmLint by tasks.registering(NpmTask::class) {
    group = "verification"
    description = "Runs the frontend ESLint checks."
    dependsOn("npmInstall")
    workingDir.set(projectDir)
    args.set(listOf("run", "lint"))
}

val npmTest by tasks.registering(NpmTask::class) {
    group = "verification"
    description = "Runs the frontend unit tests once in CI mode."
    dependsOn("npmInstall")
    workingDir.set(projectDir)
    environment.set(mapOf("CI" to "1"))
    args.set(listOf("run", "test", "--", "--watch=false"))
}

val npmSbomOutput = layout.buildDirectory.file("reports/cyclonedx/bom.json")

val npmSbom by tasks.registering(NpmTask::class) {
    group = "verification"
    description = "Generates a CycloneDX SBOM for frontend npm dependencies."
    dependsOn("npmInstall")
    workingDir.set(projectDir)
    args.set(listOf("sbom", "--sbom-format", "cyclonedx", "--sbom-type", "application"))
    outputs.file(npmSbomOutput)

    val sbom = ByteArrayOutputStream()

    doFirst {
        sbom.reset()
    }

    execOverrides {
        standardOutput = sbom
    }

    doLast {
        val outputFile = npmSbomOutput.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeBytes(sbom.toByteArray())
    }
}

tasks.register<Sync>("installDist") {
    group = "distribution"
    description = "Copies the production-ready frontend bundle into a distribution folder."
    dependsOn(npmBuild)
    from(layout.projectDirectory.dir("dist/frontend"))
    into(layout.buildDirectory.dir("install/frontend"))
}

tasks.named("assemble") {
    dependsOn("installDist")
}

tasks.named("check") {
    dependsOn(npmLint)
    dependsOn(npmTest)
}

tasks.named<Delete>("clean") {
    delete(layout.buildDirectory)
    delete(projectDir.resolve(".angular"))
    delete(projectDir.resolve(".gradle"))
    delete(projectDir.resolve("dist"))
    delete(projectDir.resolve("node_modules"))
}
