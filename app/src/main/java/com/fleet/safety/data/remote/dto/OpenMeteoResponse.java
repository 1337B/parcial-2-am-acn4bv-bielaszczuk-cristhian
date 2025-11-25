package com.fleet.safety.data.remote.dto;

import com.squareup.moshi.Json;

public class OpenMeteoResponse {
    @Json(name = "current")
    public CurrentWeather current;

    public static class CurrentWeather {
        @Json(name = "temperature_2m")
        public double temperature2m;

        @Json(name = "precipitation")
        public double precipitation;

        @Json(name = "weather_code")
        public int weatherCode;
    }
}

