package com.mslxl.provlegistotracker.pojo

import com.fasterxml.jackson.annotation.JsonIgnore

data class UserDatabase(
    @JsonIgnore
    val id: Int,
    val username: String,
    val displayName: String,
    @JsonIgnore
    val passwordMd5: String,
    val realName: String,
    val supervisor: Boolean
)