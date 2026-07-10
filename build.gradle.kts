import org.gradle.api.GradleException

plugins {
    base
    idea
}

group = "de.thm.mni"
version = "0.0.1-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks.register<Sync>("installDist") {
    group = "distribution"
    description = "Builds installable frontend and backend artifacts for the whole system."
    dependsOn(":frontend:installDist", ":backend:installDist")

    from(project(":backend").layout.buildDirectory.dir("install/backend")) {
        into("backend")
    }
    from(project(":frontend").layout.buildDirectory.dir("install/frontend")) {
        into("frontend")
    }

    into(layout.buildDirectory.dir("install"))
}

tasks.register("verifyDistribution") {
    group = "verification"
    description = "Verifies that the install distribution does not contain development-only directories."
    dependsOn("installDist")

    doLast {
        val installDir = layout.buildDirectory.dir("install").get().asFile
        if (!installDir.exists()) {
            throw GradleException("Distribution directory was not created: $installDir")
        }

        val forbiddenDirectories =
            installDir
                .walkTopDown()
                .filter { it.isDirectory && it.name == "node_modules" }
                .toList()

        if (forbiddenDirectories.isNotEmpty()) {
            throw GradleException(
                "Distribution must not contain node_modules directories: " +
                    forbiddenDirectories.joinToString(", ") { it.relativeTo(projectDir).path },
            )
        }
    }
}

tasks.register("lint") {
    group = "verification"
    description = "Runs static code quality checks for the frontend and backend."
    dependsOn(":backend:ktlintCheck", ":frontend:npmLint")
}

tasks.register("sbom") {
    group = "verification"
    description = "Generates CycloneDX SBOMs for the Gradle backend and npm frontend dependencies."
    dependsOn(":backend:cyclonedxDirectBom", ":frontend:npmSbom")
}

tasks.named("assemble") {
    dependsOn("installDist")
}

tasks.named("check") {
    dependsOn("lint")
    dependsOn("verifyDistribution")
}

tasks.named<Delete>("clean") {
    dependsOn(":frontend:clean", ":backend:clean")
    delete(layout.buildDirectory)
}
