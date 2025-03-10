package com.miaogu.mapper


import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miaogu.entity.Chat4Message
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface Chat4MessageMapper : BaseMapper<Chat4Message> {
    @Select("SELECT COALESCE(MAX(delete_version), 0) FROM chat4_message WHERE username = #{username}")
    fun selectMaxDeleteVersion(@Param("username") username: String): Int

    @Update("UPDATE chat4_message SET delete_version = #{deleteVersion} WHERE username = #{username} AND delete_version = #{maxVersion}")
    fun updateVersion(
        @Param("username") username: String,
        @Param("deleteVersion") deleteVersion: Int,
        @Param("maxVersion") maxVersion: Int
    ): Boolean
}
