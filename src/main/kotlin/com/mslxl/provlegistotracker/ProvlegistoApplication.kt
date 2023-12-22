package com.mslxl.provlegistotracker

import com.mslxl.provlegistotracker.config.WebCorsConfig
import com.mslxl.provlegistotracker.config.WebSocketConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(WebSocketConfig::class, WebCorsConfig::class)
class ProvlegistoApplication


fun main(args: Array<String>) {
    runApplication<ProvlegistoApplication>(*args)
}
