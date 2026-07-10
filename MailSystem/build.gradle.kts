// Root-level orchestration for building client + server and packaging the client into the server jar

tasks.register("installDist") {
  description = "Builds mailclient and mailserver and packages client dist into server jar"
  group = "distribution"

  dependsOn(":mailclient:installDist")
  dependsOn(":mailserver:installDist")

  doLast {
    copy {
      from(project(":mailserver").layout.buildDirectory.dir("install"))
      into(project.rootDir.resolve("build/install"))
    }

    copy {
      from(project(":mailclient").layout.buildDirectory.dir("install/browser"))
      into(project.rootDir.resolve("build/install/mailclient"))
    }
  }
}

tasks.register<Delete>("clean") {
  description = "Cleans generated artifacts from both mailclient and mailserver"
  group = "build"

  dependsOn(":mailclient:clean")
  dependsOn(":mailserver:clean")

  delete(project.rootDir.resolve("build"))
}
