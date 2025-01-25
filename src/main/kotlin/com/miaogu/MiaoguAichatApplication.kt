package com.miaogu

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
@MapperScan("com.miaogu.mapper")
class MiaoguAichatApplication

fun main(args: Array<String>) {
    runApplication<MiaoguAichatApplication>(*args)
}
