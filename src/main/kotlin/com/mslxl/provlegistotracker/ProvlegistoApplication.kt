package com.mslxl.provlegistotracker

import com.mslxl.provlegistotracker.config.WebSocketConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
@Import(WebSocketConfig::class)
class ProvlegistoApplication{
    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/").allowedOrigins("*")
        }
    }
}


fun main(args: Array<String>) {
    runApplication<ProvlegistoApplication>(*args)
}
