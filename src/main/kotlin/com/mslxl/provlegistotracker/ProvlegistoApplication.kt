package com.mslxl.provlegistotracker

import com.mslxl.provlegistotracker.config.WebSocketConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProvlegistoApplication


fun main(args: Array<String>) {
    runApplication<ProvlegistoApplication>(*args)
}
