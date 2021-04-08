package com.example.chores.utils.usefullFunctions

import android.content.Context
import android.widget.Toast
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.disLikePostJson
import com.example.chores.Api.Json.likePostJson
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.postData
import com.example.chores.utils.userInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
class PostRelated {
    public fun likePost(context: Context,post:postData,userInfo:UserInfoResponse,userInterface: userInterface){
        val sp = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postlike = likePostJson(post.post_id,post.user_id,userInfo.username,userInfo.profile_pic)
        val retrofitData = retrofitBuilder.likePost("$token id $id",postlike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"ERROR: ${t.message}", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(context,"ERROR: ${response.body()}", Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        userInterface.unauthorized()
                    }
                }else{
                    Toast.makeText(context,"${response.body()}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    public fun disLikePost(context: Context,post:postData,userInfo:UserInfoResponse,userInterface: userInterface){
        val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postDislike = disLikePostJson(post.post_id,userInfo.user_id)
        val retrofitData = retrofitBuilder.dislikePost("$token id $id",postDislike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Error: ${t.message}",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(context,"ERROR: ${response.body()}",Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        userInterface.unauthorized()
                    }
                }else{
                    Toast.makeText(context,"${response.body()}",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

}