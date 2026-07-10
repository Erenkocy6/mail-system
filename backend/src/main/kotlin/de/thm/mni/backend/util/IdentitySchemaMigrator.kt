package de.thm.mni.backend.util

import org.springframework.boot.CommandLineRunner
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class IdentitySchemaMigrator(
    private val jdbcTemplate: JdbcTemplate,
) : CommandLineRunner {
    override fun run(vararg args: String) {
        dropColumnIfExists("users", "password")
    }

    private fun dropColumnIfExists(
        tableName: String,
        columnName: String,
    ) {
        val columnCount =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE lower(table_name) = ?
                  AND lower(column_name) = ?
                """.trimIndent(),
                Long::class.java,
                tableName.lowercase(),
                columnName.lowercase(),
            ) ?: 0L

        if (columnCount > 0L) {
            jdbcTemplate.execute("ALTER TABLE $tableName DROP COLUMN $columnName")
        }
    }
}
