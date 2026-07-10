import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "7.1.0"
}

apply(plugin = "idea")

node {
  download = true
  version = "24.12.0"
  npmVersion = "11.6.2"
}

tasks.register<NpmTask>("build") {
  group = "build"

  dependsOn("npmSetup", "npmInstall")
  args.set(listOf("run", "build"))
  workingDir.set(project.rootDir.resolve("mailclient"))
}

tasks.register("installDist") {
  group = "distribution"
  dependsOn("build")

  doLast {
    val modulePath = project.rootDir.resolve("mailclient")
    val installDir = modulePath.resolve("build/install/browser")

    val packageJsonPath = modulePath.resolve("package.json")
    val packageLockPath = modulePath.resolve("package-lock.json")

    copy {
      from(packageJsonPath)
      from(packageLockPath)
      into(installDir)
    }

    val npmCi = ProcessBuilder("npm", "ci")
      .directory(installDir)
      .inheritIO()
      .start()

    val exitCode = npmCi.waitFor()
    check(exitCode == 0) { "npm ci failed with exit code $exitCode" }

    installDir.resolve("package-lock.json").delete()
    installDir.resolve("package.json").delete()
  }
}

// clean task
tasks.register<Delete>("clean") {
  val modulePath = project.rootDir.resolve("mailclient")

  delete(modulePath.resolve(".angular"))
  delete(modulePath.resolve(".gradle"))
  delete(modulePath.resolve("build"))
  delete(modulePath.resolve("dist"))
  delete(modulePath.resolve("node_modules"))
}
