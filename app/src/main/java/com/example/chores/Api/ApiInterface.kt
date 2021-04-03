package com.example.chores.Api

import com.example.chores.Api.Json.*
import com.example.chores.utils.commentData
import com.example.chores.utils.postData
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

    @POST("addPost")
    fun AddPost(@Header("Token")Body:String ,@Body body: AddPostJson):Call<String>

    @GET("image")
    fun getImage():Call<ByteArray>

    @POST("userInfo")
    fun userInfo(@Header("Token")Body:String, @Body body: UserInfoJson):Call<UserInfoResponse>

    @GET("getUserInfo")
    fun getUserInfo(@Header("Token")Body: String):Call<UserInfoResponse>

    @POST("getPosts")
    fun getPosts(@Header("Token")Body:String, @Body body:timeInfoAndUserId):Call<ArrayList<postData>>

    @POST("likePost")
    fun likePost(@Header("Token")Body:String,@Body body:likePostJson):Call<String>

    @POST("dislikePost")
    fun dislikePost(@Header("Token")Body:String,@Body body: disLikePostJson):Call<String>

    @POST("addComment")
    fun addComment(@Header("Token")Body: String,@Body body:CommentAddJson):Call<String>

    @POST("getComments")
    fun getComments(@Header("Token")Body:String,@Body body:timeInfoAndPostId):Call<ArrayList<commentData>>

    @POST("likeComment")
    fun likeComment(@Header("Token")Body:String,@Body body:commentId):Call<String>

    @POST("dislikeComment")
    fun dislikeComment(@Header("Token")Body:String,@Body body:commentId):Call<String>
}