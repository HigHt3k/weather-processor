package com.flightdelays.weatherprocessor.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Service
class WeatherService {

    private val logger: Logger = LoggerFactory.getLogger(WeatherService::class.java)
    @Value("\${openweather.api.key}")
    private lateinit var apiKey: String
    private val openWeatherApiUrl: String = "https://api.openweathermap.org/data/2.5/weather"
    private val restTemplate: RestTemplate = RestTemplate()


    @Serializable
    data class WeatherApiResponse(
        val coord: Coord,
        val weather: List<Weather>,
        val base: String,
        val main: Main,
        val visibility: Int,
        val wind: Wind,
        val clouds: Clouds,
        val dt: Long,
        val sys: Sys,
        val timezone: Int,
        val id: Long,
        val name: String,
        val cod: Int
    )

    @Serializable
    data class Coord(val lon: Float, val lat: Float)

    @Serializable
    data class Main(
        val temp: Float,
        val feels_like: Float,
        val temp_min: Float,
        val temp_max: Float,
        val pressure: Int,
        val humidity: Int
    )

    @Serializable
    data class Wind(val speed: Float, val deg: Int)

    @Serializable
    data class Weather(val id: Int, val main: String, val description: String, val icon: String)

    @Serializable
    data class Clouds(val all: Int)

    @Serializable
    data class Sys(
        val type: Int,
        val id: Int,
        val country: String,
        val sunrise: Long,
        val sunset: Long
    )

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun fetchWeatherData() {
        runBlocking {
            try {
                val responseDeferred = async(Dispatchers.IO) {
                    restTemplate.getForEntity<String>("$openWeatherApiUrl?q=London&appid=$apiKey")
                }

                val responseEntity: ResponseEntity<String> = responseDeferred.await()
                if(responseEntity.statusCode == HttpStatus.OK) {
                    val responseBody = responseEntity.body;
                    val weatherApiResponse = Json.decodeFromString(WeatherApiResponse.serializer(), responseBody!!)
                    processWeatherData(weatherApiResponse)
                    logger.info("Weather data fetched successfully.")
                } else {
                    logger.error("Failed to fetch weather data. Status code: ${responseEntity.statusCode}")
                }
            } catch (e: Exception) {
                logger.error("Error fetching feather data: ${e.message}", e)
            }
        }
    }

    private fun processWeatherData(weatherApiResponse: WeatherApiResponse) {
        val temperature = weatherApiResponse.main.temp
        val humidity = weatherApiResponse.main.humidity
        val windSpeed = weatherApiResponse.wind.speed
        val weatherDescription = weatherApiResponse.weather.firstOrNull()?.description

        logger.info("Temperature: ${temperature}K, Humidity: $humidity%, Wind Speed: $windSpeed m/s, Weather: $weatherDescription")
    }
}