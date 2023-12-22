package com.mslxl.provlegistotracker.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZonedDateTime

data class Room(
    val id: Int,
    val name: String,
    val owner: String,
    val max: Int,
    val allowAnonymous: Boolean,
    val createTime: ZonedDateTime,
    @JsonIgnore
    val password: String?,
) {
    val passwordRequired
        get() = password != null
}


