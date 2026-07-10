package de.thm.mni.mailserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.system.ApplicationHome

@SpringBootApplication
class MailserverApplication

fun main(args: Array<String>) {
  if (System.getenv("APP_HOME") != null) {
    val appHome = ApplicationHome(MailserverApplication::class.java)
    System.setProperty("APP_HOME", appHome.source?.parentFile?.parentFile?.absolutePath ?: ".")
  }

  runApplication<MailserverApplication>(*args)
}
