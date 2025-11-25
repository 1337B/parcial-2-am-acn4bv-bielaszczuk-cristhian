package com.fleet.safety.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.fleet.safety.data.remote.api.OpenMeteoApi;
import com.fleet.safety.data.remote.dto.OpenMeteoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class OpenMeteoWeatherService implements WeatherService {

    private static final String BASE_URL = "https://api.open-meteo.com/";
    private static final String CURRENT_PARAMS = "temperature_2m,precipitation,weather_code";

    private static final double COMODORO_LATITUDE = -45.86;
    private static final double COMODORO_LONGITUDE = -67.48;

    private final OpenMeteoApi api;
    private final Handler mainHandler;

    public OpenMeteoWeatherService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        this.api = retrofit.create(OpenMeteoApi.class);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void getCurrentAsync(double latitude, double longitude, WeatherCallback callback) {
        Call<OpenMeteoResponse> call = api.getCurrentWeather(latitude, longitude, CURRENT_PARAMS);

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        OpenMeteoResponse apiResponse = response.body();
                        com.fleet.safety.domain.WeatherSnapshot snapshot = parseWeatherResponse(apiResponse);

                        mainHandler.post(() -> callback.onSuccess(snapshot));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError(e));
                    }
                } else {
                    mainHandler.post(() -> callback.onError(
                            new Exception("HTTP request failed with code: " + response.code())
                    ));
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                mainHandler.post(() -> callback.onError(new Exception(t)));
            }
        });
    }

    public void getCurrentAsyncForComodoro(WeatherCallback callback) {
        getCurrentAsync(COMODORO_LATITUDE, COMODORO_LONGITUDE, callback);
    }

    private com.fleet.safety.domain.WeatherSnapshot parseWeatherResponse(OpenMeteoResponse apiResponse) {
        OpenMeteoResponse.CurrentWeather current = apiResponse.current;

        double temperature = current.temperature2m;
        double precipitation = current.precipitation;
        int weatherCode = current.weatherCode;

        return WeatherMapper.mapFrom(temperature, precipitation, weatherCode);
    }
}
