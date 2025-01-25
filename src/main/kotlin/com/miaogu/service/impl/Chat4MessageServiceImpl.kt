package com.miaogu.service.impl

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.entity.Chat4Message
import com.miaogu.mapper.Chat4MessageMapper
import com.miaogu.service.Chat4MessageService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service


@Service
class Chat4MessageServiceImpl : ServiceImpl<Chat4MessageMapper, Chat4Message>(), Chat4MessageService {

    @CacheEvict(value = ["chat4Messages", "chat4MessagesCount"], allEntries = true) // 清空缓存
    override fun remove(queryWrapper: Wrapper<Chat4Message>): Boolean {
        return super<ServiceImpl>.remove(queryWrapper)
    }

    @Cacheable(value = ["chat4Messages"]) // 缓存聊天消息列表
    override fun list(): MutableList<Chat4Message> {
        return super<ServiceImpl>.list()
    }

    @CacheEvict(value = ["chat4Messages"], allEntries = true) // 清空缓存
    override fun save(entity: Chat4Message): Boolean {
        return super<ServiceImpl>.save(entity)
    }

    @Cacheable(value = ["chat4MessagesCount"], key = "'chat4MessagesCount'") // 缓存计数结果
    override fun count(queryWrapper: Wrapper<Chat4Message>): Long {
        return super<ServiceImpl>.count(queryWrapper)
    }
}
