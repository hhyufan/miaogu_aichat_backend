package com.miaogu.service
import com.miaogu.entity.GitHubRepoStargazers
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import kotlin.jvm.java

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

        // 发送 GET 请求
        val response: ResponseEntity<GitHubRepoStargazers> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            GitHubRepoStargazers::class.java
        )

        return response.body?.stargazersCount
    }
}
