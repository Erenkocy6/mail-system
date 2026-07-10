package de.thm.mni.backend.smtp

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.nio.charset.StandardCharsets
import java.util.Properties

@Configuration
@EnableConfigurationProperties(MailGatewayProperties::class)
class MailGatewayConfiguration {
    @Bean
    @ConditionalOnProperty(
        prefix = "mail.gateway.smtp",
        name = ["enabled"],
        havingValue = "true",
    )
    fun javaMailSender(properties: MailGatewayProperties): JavaMailSender {
        val sender = JavaMailSenderImpl()
        sender.host = properties.smtp.host
        sender.port = properties.smtp.port
        sender.username = properties.smtp.username
        sender.password = properties.smtp.password
        sender.protocol = "smtp"
        sender.defaultEncoding = StandardCharsets.UTF_8.name()
        sender.javaMailProperties =
            Properties().apply {
                put("mail.smtp.auth", properties.smtp.auth.toString())
                put("mail.smtp.ssl.enable", properties.smtp.sslEnabled.toString())
                put("mail.smtp.starttls.enable", properties.smtp.starttlsEnabled.toString())
                put("mail.smtp.connectiontimeout", properties.smtp.connectionTimeoutMillis.toString())
                put("mail.smtp.timeout", properties.smtp.readTimeoutMillis.toString())
                put("mail.smtp.writetimeout", properties.smtp.writeTimeoutMillis.toString())
            }
        return sender
    }
}
