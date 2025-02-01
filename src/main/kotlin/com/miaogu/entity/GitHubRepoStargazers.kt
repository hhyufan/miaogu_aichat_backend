package com.miaogu.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubRepoStargazers (
    @JsonProperty("stargazers_count")
    val stargazersCount: Int
)


