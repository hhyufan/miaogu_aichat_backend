package com.miaogu.service.impl

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.entity.DeepSeekMessage
import com.miaogu.enums.RestoreStatus
import com.miaogu.mapper.DeepSeekMessageMapper
import com.miaogu.service.DeepSeekService
import org.springframework.stereotype.Service

@Service
class DeepSeekServiceImpl : ServiceImpl<DeepSeekMessageMapper, DeepSeekMessage>(), DeepSeekService{

    override fun clearWithVersion(username: String): Boolean {
        // 获取当前最大版本号
        val maxVersion = baseMapper.selectMaxDeleteVersion(username)
        val newVersion = maxVersion + 1

        // 批量更新版本号（等效逻辑删除）
        return update(
            Wrappers.update< DeepSeekMessage>()
                .set("delete_version", newVersion)
                .eq("username", username)
                .eq("delete_version", 0) // 只操作未删除的记录
        )
    }

    // 版本回退恢复
    override fun restoreVersion(username: String): RestoreStatus {
        // 获取可恢复的最大版本号
        val maxVersion = baseMapper.selectMaxDeleteVersion(username)
        if (maxVersion <= 0) return RestoreStatus.NO_HISTORY
        // 恢复该版本记录
        return if (baseMapper.updateVersion(username, 0, maxVersion)) RestoreStatus.SUCCESS else RestoreStatus.UPDATE_FAILED
    }
}
