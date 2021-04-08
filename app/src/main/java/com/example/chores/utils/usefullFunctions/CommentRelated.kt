package com.example.chores.utils.usefullFunctions

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.chores.Api.Json.CommentAddJson
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.utils.postData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*

class CommentRelated {
    public fun likeComment(context:Context, post:postData, userInfo:UserInfoResponse,comment:String, comment_write: EditText, comment_view: LinearLayout
    ){
        val calendar = Calendar.getInstance()
        val date = DateFormat.getDateInstance().format(calendar.time)
        val millis:Long = System.currentTimeMillis()/1000
        val sp = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val comment_id = UUID.randomUUID()
        val commentAdd = CommentAddJson(post.post_id,comment_id,id!!,userInfo.username,userInfo.profile_pic,comment,millis,0,date)
        val retrofitData = retrofitBuilder.addComment("$token id $id",commentAdd)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error",true)){
                    Toast.makeText(context,"${response.body()}", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context,"Commented", Toast.LENGTH_LONG).show()
                    comment_write.setText("")
                    comment_view.visibility = View.GONE
                }
            }
        })
    }
}