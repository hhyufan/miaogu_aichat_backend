package com.miaogu.runner

import com.miaogu.service.GitHubService
import org.springframework.boot.CommandLineRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class GitHubRunner(private val githubService: GitHubService,
                   private val redisTemplate: RedisTemplate<String, String>) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val stargazersCount = githubService.getStargazersCount("hhyufan", "miaogu_aichat_frontend")
        redisTemplate.opsForValue().set("repoStarCount", stargazersCount.toString())
        println("Stargazers count: $stargazersCount")

    }
}
