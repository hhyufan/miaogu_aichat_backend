package com.miaogu.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.entity.Friend
import com.miaogu.mapper.FriendMapper
import com.miaogu.service.FriendService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class FriendServiceImpl : ServiceImpl<FriendMapper, Friend>(), FriendService {
    @Cacheable(value = ["friend"]) // 缓存好友列表
    override fun list(): List<Friend> {
        // 从数据库获取好友列表
        return super<ServiceImpl>.list()
    }
}

