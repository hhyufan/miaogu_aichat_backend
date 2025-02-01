package com.miaogu.controller

import com.miaogu.runner.GitHubRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/github")
class GitHubController(
    private val redisTemplate: RedisTemplate<String, String>,
    private val gitHubRunner: GitHubRunner) {
    @PostMapping("/stars")
    fun getRepoStarCount(): Int {
        if (redisTemplate.opsForValue().get("repoStarCount") == null) {
            gitHubRunner.run()
        }
        return redisTemplate.opsForValue().get("repoStarCount")?.toInt() ?: 0
    }
    @Scheduled(cron = "0 0 0 * * *")
    fun refreshRepoStarCount() {
        gitHubRunner.run()
    }
}
