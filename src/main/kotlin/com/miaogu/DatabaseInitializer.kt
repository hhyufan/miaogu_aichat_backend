package com.miaogu

import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.EncodedResource
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val jdbcTemplate: JdbcTemplate) : CommandLineRunner {

    override fun run(vararg args: String?) {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        jdbcTemplate.execute(ConnectionCallback {
            ScriptUtils.executeSqlScript(it, EncodedResource(ClassPathResource("sql/init_data.sql"), "utf-8"))
        })
    }
}
