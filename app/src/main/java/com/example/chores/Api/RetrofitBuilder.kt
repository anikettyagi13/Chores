package com.example.chores.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {
    public fun retrofitBuilder():ApiInterface{
        return Retrofit.Builder()
            .baseUrl("")  // ####################################### ADD YOUR IP ADDRESS
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}