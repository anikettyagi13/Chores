package com.example.chores.utils.usefullFunctions

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.applyJson
import com.example.chores.Api.Json.disLikePostJson
import com.example.chores.Api.Json.likePostJson
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.R
import com.example.chores.WebView
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ResumeUtils
import com.example.chores.utils.notify
import com.example.chores.utils.postData
import com.example.chores.utils.userInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.apply
import kotlin.collections.ArrayList

val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
class PostRelated {
    public fun likePost(context: Context,post:postData,userInfo:UserInfoResponse,userInterface: userInterface){
        val sp = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postlike = likePostJson(post.post_id,post.user_id,userInfo.username,userInfo.profile_pic)
        val time = System.currentTimeMillis()
        val retrofitData = retrofitBuilder.likePost("$token id $id",postlike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Required",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(context,"ERROR: ${response.body()}", Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        userInterface.unauthorized()
                    }
                }else{
                    Toast.makeText(context,"${response.body()}", Toast.LENGTH_LONG).show()
                    if(id!=post.user_id) notify().notifyUser(context,post.user_id,post.post_id,userInfo.user_id,"like",time,"",post.url,userInterface)
                }
            }
        })

    }

    public fun disLikePost(context: Context,post:postData,userInfo:UserInfoResponse,userInterface: userInterface){
        val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postDislike = disLikePostJson(post.post_id,userInfo.user_id)
        val time = System.currentTimeMillis()
        val retrofitData = retrofitBuilder.dislikePost("$token id $id",postDislike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Required",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(context,"ERROR: ${response.body()}",Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        userInterface.unauthorized()
                    }
                }else{
                    Toast.makeText(context,"${response.body()}",Toast.LENGTH_LONG).show()
                    if(id!=post.user_id) notify().notifyUser(context,post.user_id,post.post_id,userInfo.user_id,"dislike",time,"",post.url,userInterface)
                }
            }
        })
    }

    public fun apply(context: Context,post: postData,userId:String,userInterface: userInterface,applied:(code:Int)->Unit,answers:ArrayList<String>,resume:String){
        Log.i("message","$resume")
        val sp = context.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","");
        val id = sp.getString("id","");
        val calendar = Calendar.getInstance()
        val date = DateFormat.getDateInstance().format(calendar.time)
        val applyJson = applyJson(post.post_id,userId,date,System.currentTimeMillis(),answers,resume)
        val retrofitData = retrofitBuilder.apply("$token id $id",applyJson)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Required",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                applied(response.code())
            }
        })

    }


    fun applyOnPost(context:Context, post:postData, userInfo:UserInfoResponse, userInterface: userInterface, layout: View, bottomSheetBehavior: BottomSheetBehavior<View>, postsAdapter:postAdapter, questionsView:(layout:View,post:postData)->Unit, getAnswers:()->ArrayList<String>, choose_resume:(layout:View)->Unit, getResume:()-> Uri,resumeChoosed:()->Int,setUploading:(uploading:Boolean)->Unit){
        bottomSheetBehavior.state =BottomSheetBehavior.STATE_EXPANDED
        layout.location.text = post.address
        layout.description.text = post.info
        layout.created.text = DateUtils.getRelativeTimeSpanString(post.time)
        Glide.with(context!!).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(layout.userimage)
        layout.username.text = userInfo.username
        if(post.status == "assigned"){
            layout.assigned.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.rejected.visibility = View.GONE
            layout.waiting.visibility = View.GONE
        }else if(post.status == "waiting"){
            layout.waiting.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.assigned.visibility = View.GONE
            layout.rejected.visibility = View.GONE
        }else if(post.status == "rejected"){
            layout.rejected.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.assigned.visibility = View.GONE
            layout.waiting.visibility = View.GONE
        }else{
            layout.apply.visibility = View.VISIBLE
            layout.rejected.visibility = View.GONE
            layout.assigned.visibility = View.GONE
            layout.waiting.visibility = View.GONE
            fun applied(i: Int) {
                if(i == 401){
                    userInterface.unauthorized()
                }else if(i == 500){
                    Toast.makeText(context,"Error: Cannot apply now. Try Later!",Toast.LENGTH_LONG).show()
                }else if(i == 404){
                    Toast.makeText(context,"Error: Cannot apply now. Try Later!",Toast.LENGTH_LONG).show()
                }else{
                    post.applied += 1;
                    post.status = "waiting";
                    val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
                    val id = sp.getString("id","")!!
                    if(id!=post.user_id) notify().notifyUser(context!!,post.user_id,post.post_id,id,"apply",System.currentTimeMillis(),"",post.url,userInterface)
                    postsAdapter.notifyDataSetChanged()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            layout.apply.setOnClickListener {
                val answers =  getAnswers()
                var k=0;

                for (i in answers){
                    if(i.length == 0 ) {
                        k=1;
                        break;
                    }
                }

                if(post.resume){
                    if(k==0){
                        if(resumeChoosed() ==1){
                            fun uploader(url:String,pb:ProgressDialog,useless:String){
                                PostRelated().apply(context!!,post,userInfo.user_id,userInterface,::applied,answers,url)
                                pb.dismiss()
                                setUploading(false)
                            }
                            var r = getResume()
                            var cr = context.contentResolver
                            var pb = ProgressDialog(context)
                            pb.setTitle("Uploading Resume")
                            pb.setCanceledOnTouchOutside(false)
                            pb.show()
                            setUploading(true)
                            ResumeUtils().uploadResume("${userInfo.user_id}/${post.post_id}",r,cr,pb,"",::uploader)
                        }else{
                            if(!userInfo.resume.isNotEmpty()){
                                Toast.makeText(context,"Resume must be selected!",Toast.LENGTH_LONG).show()
                            }else{
                                PostRelated().apply(context!!,post,userInfo.user_id,userInterface,::applied,answers,userInfo.resume)
                            }
                        }
                    }else {
                        Toast.makeText(
                            context,
                            "Every Question Must Be Answered!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }else{
                    if(k==0) PostRelated().apply(context!!,post,userInfo.user_id,userInterface,::applied,answers,"")
                    if(k==1) Toast.makeText(context,"Every Question Must Be Answered!",Toast.LENGTH_LONG).show()
                }

            }
        }
        questionsView(layout,post)
        layout._resume_.setOnClickListener { choose_resume(layout) }
        layout.closed.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
    }

}