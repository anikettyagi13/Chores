package com.example.chores.Api

import com.example.chores.Api.Json.*
import com.example.chores.utils.commentData
import com.example.chores.utils.notificationData
import com.example.chores.utils.postData
import com.example.chores.utils.userBasicInfo
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @POST("register")
    fun RegisterApi(@Body body: RegisterApiJson?) :Call<RegisterApiResponse>

    @POST("login")
    fun LoginApi(@Body body: LoginApiJson) : Call<RegisterApiResponse>

    @GET("checkLoggedIn")
    fun checkLoggedIn(@Header("Token")Body:String): Call<String>

    @POST("addPost")
    fun AddPost(@Header("Token")Body:String ,@Body body: AddPostJson):Call<String>

    //userInfo

    @POST("userInfo")
    fun userInfo(@Header("Token")Body:String, @Body body: UserInfoJson):Call<UserInfoResponse>

    @GET("getUserInfo")
    fun getUserInfo(@Header("Token")Body: String):Call<UserInfoResponse>

    @GET("getUserInfo/{id}")
    fun getUserInfoById(@Path("id")id:String):Call<UserInfoResponse>

    @GET("basicUserInfo/{id}")
    fun basicUserInfo(@Path("id")id:String):Call<userBasicInfo>

    @PUT("userInfo")
    fun putUserInfo(@Header("Token")Body:String, @Body body:UserInfoResponse) : Call<String>

    //posts urls

    @POST("getPosts")
    fun getPosts(@Header("Token")Body:String, @Body body:timeInfoAndUserId):Call<ArrayList<postData>>

    @POST("getGlobal")
    fun getGlobal(@Header("Token")token:String,@Body body:timeInfoAndUserId):Call<ArrayList<postData>>

//    @GET("getStatus/{id}")
//    fun getStatus(@Header("Token")token:String,@Path("id") id:String):Call<String>

    @GET("getIfLiked/{id}")
    fun getIfLiked(@Header("Token")token:String,@Path("id")id:String):Call<String>

    @GET("getPost/{id}")
    fun getPost(@Path("id")id:String):Call<postData>

    @POST("getUserPosts")
    fun getUserPosts(@Header("Token")Body:String, @Body body:timeInfoAndUserId):Call<ArrayList<postData>>

    @POST("getUserPosts/{id}")
    fun getUserPostsById( @Body body:timeInfoAndUserId,@Path("id")id:String):Call<ArrayList<postData>>

    @POST("apply")
    fun apply(@Header("Token")Body:String,@Body body:applyJson):Call<String>

    @POST("likePost")
    fun likePost(@Header("Token")Body:String,@Body body:likePostJson):Call<String>

    @POST("dislikePost")
    fun dislikePost(@Header("Token")Body:String,@Body body: disLikePostJson):Call<String>

    @GET("getPostStatus/{id}")
    fun getPostStatus(@Header("Token")Body:String,@Path("id")id:String):Call<String>

    @POST("getAppliedList/{id}")
    fun getAppliedList(@Header("Token")Body:String, @Path("id")id:String, @Body body:timeInfoAndUserId):Call<ArrayList<applyResponse>>

    @POST("assign")
    fun assign(@Header("Token")Body:String,@Body body:assignJson):Call<String>

    @POST("reject")
    fun reject(@Header("Token")Body:String,@Body body:assignJson):Call<String>

    @GET("getAnswers/{id}")
    fun getAnswers(@Header("Token")Body:String,@Path("id")body:String):Call<ArrayList<String>>

    @GET("getAnswersOfUser/{id}/{user}")
    fun getAnswersOfUser(@Header("Token")Body:String,@Path("id")id:String,@Path("user")user:String):Call<ArrayList<String>>

    // comments urls

    @POST("addComment")
    fun addComment(@Header("Token")Body: String,@Body body:CommentAddJson):Call<String>

    @POST("getComments")
    fun getComments(@Header("Token")Body:String,@Body body:timeInfoAndPostId):Call<ArrayList<commentData>>

    @POST("likeComment")
    fun likeComment(@Header("Token")Body:String,@Body body:commentId):Call<String>

    @POST("dislikeComment")
    fun dislikeComment(@Header("Token")Body:String,@Body body:commentId):Call<String>

    // notification

    @POST("notifications")
     fun notification(@Header("Token")Body:String,@Body body:timeInfoAndUserId):Call<ArrayList<notificationData>>

    @POST("getCount")
    fun getCount(@Header("Token")Body:String,@Body body:notificationCountPost): Call<notificationCount>

    @POST("notify")
    fun notifyUser(@Header("Token")Body:String,@Body body:notificationData):Call<String>
}