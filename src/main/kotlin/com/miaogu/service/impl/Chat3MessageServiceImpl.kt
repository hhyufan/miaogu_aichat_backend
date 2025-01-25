package com.miaogu.service.impl

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.entity.Chat3Message
import com.miaogu.mapper.Chat3MessageMapper
import com.miaogu.service.Chat3MessageService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class Chat3MessageServiceImpl : ServiceImpl<Chat3MessageMapper, Chat3Message>(), Chat3MessageService {
    @CacheEvict(value = ["chat3Messages", "chat3MessagesCount"], allEntries = true) // 清空缓存
    override fun remove(queryWrapper: Wrapper<Chat3Message>): Boolean {
        return super<ServiceImpl>.remove(queryWrapper)
    }

    @Cacheable(value = ["chat3Messages"]) // 缓存聊天消息列表
    override fun list(): MutableList<Chat3Message> {
        return super<ServiceImpl>.list()
    }

    @CacheEvict(value = ["chat3Messages"], allEntries = true) // 清空缓存
    override fun save(entity: Chat3Message): Boolean {
        return super<ServiceImpl>.save(entity)
    }

    @Cacheable(value = ["chat3MessagesCount"], key = "'chat3MessagesCount'") // 缓存计数结果
    override fun count(queryWrapper: Wrapper<Chat3Message>): Long {
        return super<ServiceImpl>.count(queryWrapper)
    }
}
