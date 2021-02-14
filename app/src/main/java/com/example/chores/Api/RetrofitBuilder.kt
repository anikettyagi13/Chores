package com.example.chores.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {
    public fun retrofitBuilder():ApiInterface{
        return Retrofit.Builder()
            .baseUrl("http://192.168.43.92:3000/")  // ####################################### ADD YOUR IP ADDRESS
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}