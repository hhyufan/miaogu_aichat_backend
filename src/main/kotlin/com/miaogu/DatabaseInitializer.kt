package com.miaogu

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.AbstractResource
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.support.EncodedResource
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val jdbcTemplate: JdbcTemplate) : CommandLineRunner {
    @Value("\${database.init-script-file:init_database.sql}")
    private lateinit var databaseInitScriptFile: String

    override fun run(vararg args: String?) {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        var scriptResource: AbstractResource = FileSystemResource(databaseInitScriptFile)
        if (!scriptResource.exists()) {
            scriptResource = ClassPathResource(databaseInitScriptFile)
            if (!scriptResource.exists()) {
                return
            }
        }
        jdbcTemplate.execute(ConnectionCallback {
            ScriptUtils.executeSqlScript(it, EncodedResource(scriptResource, "utf-8"))
        })
    }
}
