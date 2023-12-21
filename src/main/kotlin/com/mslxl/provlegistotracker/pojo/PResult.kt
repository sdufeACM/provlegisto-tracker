package com.mslxl.provlegistotracker.pojo

import com.fasterxml.jackson.annotation.JsonProperty

sealed class PResult<T> {
    companion object{
        const val ERROR_PROMPT = 1
        fun <V>ok(value: V): PResult<V>{
            return Ok(value)
        }
        fun <V>err(code: Int, message: String): PResult<V> {
            return Err(code, message)
        }
    }
    data class Ok<T>(
        @JsonProperty("data")
        val value: T,
        val status: Int = 0
    ): PResult<T>()

    data class Err<T>(
        val status: Int,
        val error: String
    ): PResult<T>()
}