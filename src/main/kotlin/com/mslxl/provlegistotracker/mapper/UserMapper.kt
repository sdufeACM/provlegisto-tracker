package com.mslxl.provlegistotracker.mapper

import com.mslxl.provlegistotracker.pojo.UserDatabase
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository

@Repository
@Mapper
interface UserMapper {
    @Select("SELECT id, username, display_name, password_md5, real_name, supervisor FROM user WHERE username = #{username}")
    fun selectUserByUsername(username: String): UserDatabase?
    @Select("SELECT id, username, display_name, password_md5, real_name, supervisor FROM user WHERE id = #{id}")
    fun selectUserById(id: Int): UserDatabase?
    @Insert("INSERT INTO user(username, display_name, password_md5, real_name, supervisor) VALUES (#{username}, #{displayName}, #{password}, #{realName}, #{supervisor})")
    fun insertUser(username: String, password: String, displayName: String, realName: String?, supervisor: Boolean)
}