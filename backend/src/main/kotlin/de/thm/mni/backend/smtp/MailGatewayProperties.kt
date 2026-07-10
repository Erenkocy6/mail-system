package de.thm.mni.backend.smtp

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail.gateway")
data class MailGatewayProperties(
    var fromAddress: String = "",
    var smtp: SmtpProperties = SmtpProperties(),
    var imap: ImapProperties = ImapProperties(),
) {
    data class SmtpProperties(
        var enabled: Boolean = false,
        var host: String = "",
        var port: Int = 587,
        var username: String = "",
        var password: String = "",
        var auth: Boolean = true,
        var sslEnabled: Boolean = true,
        var starttlsEnabled: Boolean = false,
        var connectionTimeoutMillis: Int = 5000,
        var readTimeoutMillis: Int = 3000,
        var writeTimeoutMillis: Int = 5000,
    )

    data class ImapProperties(
        var enabled: Boolean = false,
        var host: String = "",
        var port: Int = 993,
        var username: String = "",
        var password: String = "",
        var folder: String = "INBOX",
        var sslEnabled: Boolean = true,
        var connectionTimeoutMillis: Int = 5000,
        var readTimeoutMillis: Int = 5000,
        var fetchLimit: Int = 20,
        var pollInitialDelayMillis: Long = 10000,
        var pollDelayMillis: Long = 60000,
    )
}
