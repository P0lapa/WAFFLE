package com.example.waffle_front.OthersActivity

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://193.124.15.94:8000"
    private val json = Json { ignoreUnknownKeys = true }

    val instance: ApiService by lazy {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        retrofit.create(ApiService::class.java)
    }
}
