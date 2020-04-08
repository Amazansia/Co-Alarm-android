package com.example.mapsactivity.data

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RestApi {
    private val maskService: MaskService
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        maskService= retrofit.create(MaskService::class.java)
    }
    fun getStoresListRetrofit(param: Map<String, Double>): Call<Stores> {
        return maskService.getTop(param)
    }
}