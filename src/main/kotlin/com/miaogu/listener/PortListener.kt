package com.miaogu.listener

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class PortListener : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        // 获取服务器端口，默认为8080
        val port = event.applicationContext.environment.getProperty("server.port", "8080")

        // 确保在最后一行打印端口信息
        println("Server is running on port: $port")
    }
}
