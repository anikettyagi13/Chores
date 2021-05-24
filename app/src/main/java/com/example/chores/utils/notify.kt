package com.example.chores.utils

import android.content.Context
import android.widget.Toast
import com.example.chores.Api.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class notify {
    public fun notifyUser(context: Context,u_id:String,post_id:String,user_id:String,type:String,time:Long,element:String,post_pic:String,userInterface: userInterface){
        val sp = context.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token =sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitBuilder =RetrofitBuilder().retrofitBuilder()
        lateinit var noti : notificationData
        if(type == "like")  noti = notificationData(u_id,post_id,user_id,"","",post_pic," Liked your chore. Total likes ",time,0,type,element)
        else if(type == "dislike") noti = notificationData(u_id,post_id,user_id,"","",post_pic," Disliked your chore. Total likes ",time,0,type,element)
        else if(type == "apply") noti = notificationData(u_id,post_id,user_id,"","",post_pic," Applied for your chore. Total applies ",time,0,type,element)
        else if(type == "assign") noti = notificationData(u_id,post_id,user_id,"","",post_pic," Congratulations!!. Do a great job, We trust you! ",time,0,type,element)
        val retrofitData = retrofitBuilder.notifyUser("$token id $id",noti)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Required",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.isSuccessful){
                   Toast.makeText(context,"Notified",Toast.LENGTH_LONG).show()
                }else{
                    if(response.code() ==401) userInterface.unauthorized()
                    else Toast.makeText(context,"Error! try again later",Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}