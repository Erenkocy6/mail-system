package de.thm.mni.mailserver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthCheck {

  @GetMapping("/health")
  fun health(): String {
    return "Healthy"
  }

  @PostMapping("/login")
  fun login(
    @RequestParam username: String
  ): String {
    return "Successfully processed login for user '$username'."
  }
}
