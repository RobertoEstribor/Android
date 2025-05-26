package com.example.userapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Import GsonConverterFactory
// import retrofit2.converter.scalars.ScalarsConverterFactory // Can be removed if only JSON is expected

object RetrofitInstance {
    private const val BASE_URL = "https://www.todoestribor.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
            // .addConverterFactory(ScalarsConverterFactory.create()) // Ensure this is commented or removed
            .build()
            .create(ApiService::class.java)
    }
}
