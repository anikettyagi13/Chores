package com.example.chores.Api

import com.example.chores.Api.Json.HeaderJson
import com.example.chores.Api.Json.LoginApiJson
import com.example.chores.Api.Json.RegisterApiJson
import com.example.chores.Api.Json.RegisterApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {
    @POST("register")
    fun RegisterApi(@Body body: RegisterApiJson?) :Call<RegisterApiResponse>

    @POST("login")
    fun LoginApi(@Body body: LoginApiJson) : Call<RegisterApiResponse>

    @GET("checkLoggedIn")
    fun checkLoggedIn(@Header("Token")Body:String): Call<String>

    @GET("home")
    fun Home(@Header("Token")Body:String): Call<String>
}