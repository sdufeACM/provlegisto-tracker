package com.mslxl.provlegistotracker.pojo

data class UserDatabase(
    val id: Int,
    val username: String,
    val displayName: String,
    val passwordMd5: String,
    val realName: String,
    val supervisor: Boolean
)