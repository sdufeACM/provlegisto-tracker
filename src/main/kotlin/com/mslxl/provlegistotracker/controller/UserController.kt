package com.mslxl.provlegistotracker.controller

import com.mslxl.provlegistotracker.mapper.UserMapper
import com.mslxl.provlegistotracker.pojo.PResult
import com.mslxl.provlegistotracker.pojo.UserDatabase
import com.mslxl.provlegistotracker.pojo.UserLoginRequest
import com.mslxl.provlegistotracker.pojo.UserRegister
import jakarta.servlet.http.HttpSession
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController("/user")
class UserController(val userMapper: UserMapper) {
    @PostMapping("/user/login")
    fun login(@RequestBody user: UserLoginRequest, session: HttpSession): PResult<String> {
        if (user.username.isNullOrBlank()) return PResult.err(PResult.ERROR_PROMPT, "Username can not be empty")
        if (user.password.isNullOrBlank()) return PResult.err(PResult.ERROR_PROMPT, "Password can not be empty")

        val userDb =
            userMapper.selectUserByUsername(user.username) ?: return PResult.err(PResult.ERROR_PROMPT, "User not found")

        val passwordMd5 = DigestUtils.md5DigestAsHex(user.password.toByteArray())

        if (userDb.passwordMd5 != passwordMd5) {
            return PResult.err(PResult.ERROR_PROMPT, "Username and password not match")
        }

        session.setAttribute("username", userDb.username)
        session.setAttribute("uid", userDb.id)

        return PResult.ok(userDb.displayName)
    }

    @GetMapping("/user/profile")
    fun profile(session: HttpSession): PResult<UserDatabase> {
        val id = session.getAttribute("uid") as Int? ?: return PResult.err(PResult.ERROR_PROMPT, "User not login")
        val user = userMapper.selectUserById(id) ?: return PResult.err(PResult.ERROR_PROMPT, "Database error")
        return PResult.ok(user)
    }

    @PostMapping("/user/register")
    fun register(
        @RequestBody
        user: UserRegister
    ): PResult<String> {
        val passwordMd5 = DigestUtils.md5DigestAsHex(user.password.toByteArray())
        val displayName = user.displayName ?: user.realName ?: user.username
        try {
            userMapper.insertUser(user.username, passwordMd5, displayName, user.realName, false)
            return PResult.ok("Register complete")
        } catch (e: Exception) {
            return PResult.err(PResult.ERROR_PROMPT, e.message ?: e.toString())
        }
    }
}