package com.flightdelays.weatherprocessor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class WeatherProcessorApplication

fun main(args: Array<String>) {
    runApplication<WeatherProcessorApplication>(*args)
}
