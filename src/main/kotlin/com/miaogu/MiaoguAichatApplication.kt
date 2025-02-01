package com.miaogu

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@EnableCaching
@SpringBootApplication
@MapperScan("com.miaogu.mapper")
class MiaoguAichatApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

fun main(args: Array<String>) {
    runApplication<MiaoguAichatApplication>(*args)
}
