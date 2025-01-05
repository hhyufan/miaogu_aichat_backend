package com.miaogu.mapper


import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miaogu.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper : BaseMapper<User>
