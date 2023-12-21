package com.mslxl.provlegistotracker.pojo

data class RoomCreateRequest(
    val name: String,
    val max: Int,
    val password: String?,
    val allowAnonymous: Boolean,
)