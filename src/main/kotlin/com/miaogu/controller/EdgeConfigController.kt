package com.miaogu.controller

import com.miaogu.controller.EdgeConfigController.EdgeConfigItem
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

// 完整的API响应类型（数组包裹）
typealias EdgeConfigResponse = List<EdgeConfigItem>

@RequestMapping("/edge-config")
@RestController
class EdgeConfigController {
    data class EdgeConfigItem(
        val createdAt: Long,        // 时间戳
        val key: String,            // 配置项键
        val value: String,          // 配置项值（根据实际类型可能需要调整）
        val edgeConfigId: String,   // 配置ID
        val updatedAt: Long         // 更新时间戳
    )
    private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    private val restTemplate = RestTemplate()
    @Value("\${vercel.edge-config-id}")
    private val edgeConfigId: String? = null
    @Value("\${vercel.access-token}")
    private val accessToken: String? = null
    @GetMapping("/api/config")
    fun getEdgeConfig(): ResponseEntity<Any> {
        val url = "https://api.vercel.com/v1/edge-config/${edgeConfigId}/items"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val requestEntity = HttpEntity<Any>(headers)

        return try {
            // 使用Array<EdgeConfigItem>作为响应类型
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Array<EdgeConfigItem>::class.java
            )
            ResponseEntity.ok(response.body?.toList()) // 转换为List更符合Kotlin习惯
        } catch (ex: HttpClientErrorException) {
            // 打印详细的错误信息
            logger?.error("""
                            Vercel API请求失败！
                            URL: $url
                            状态码: ${ex.statusCode}
                            错误响应: ${ex.responseBodyAsString}
                        """.trimIndent())
            ResponseEntity.status(ex.statusCode).body(mapOf(
                "error" to "Vercel API请求失败",
                "details" to ex.message
            ))
        }
    }
}
