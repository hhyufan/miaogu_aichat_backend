package com.miaogu.service

import com.miaogu.entity.GitHubRepoStargazers
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.RestClientException

@Service
class GitHubService(private val restTemplate: RestTemplate) {

    private val githubApiUrl = "https://api.github.com"

    @Value("\${github.PAT}") // 从配置文件中注入 GitHub PAT
    private val githubPAT: String? = null // 允许为 null

    fun getStargazersCount(owner: String, repo: String): Int? {
        val url = "$githubApiUrl/repos/$owner/$repo"

        // 创建请求头
        val headers = HttpHeaders()
        if (!githubPAT.isNullOrEmpty()) {
            // 如果 githubPAT 不为空，添加 Authorization 头
            headers.set("Authorization", "token $githubPAT")
        }

        val entity = HttpEntity<String>(headers)

        return try {
            // 发送 GET 请求
            val response: ResponseEntity<GitHubRepoStargazers> = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GitHubRepoStargazers::class.java
            )

            response.body?.stargazersCount
        } catch (e: RestClientException) {
            // 处理请求失败的情况
            // 可以记录日志或返回默认值
            println("Error fetching stargazers count: ${e.message}")
            null // 返回 null 或者可以返回其他默认值
        }
    }
}
