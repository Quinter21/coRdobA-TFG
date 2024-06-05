package com.example.coRdobA.adapters

import com.example.coRdobA.data.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsAPI {
    @GET("directions/json")
    fun getDirections(
        @Query("mode") mode : String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionsResponse>
}
