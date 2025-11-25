package com.fleet.safety.data.remote.api;

import com.fleet.safety.data.remote.dto.OpenMeteoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoApi {

    @GET("v1/forecast")
    Call<OpenMeteoResponse> getCurrentWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String currentParams
    );
}

