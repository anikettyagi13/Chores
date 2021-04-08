package com.example.chores.Fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.Api.Json.*
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.LoginActivity
import com.example.chores.Post_full_Screen
import com.example.chores.R
import com.example.chores.utils.postAdapter
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.postData
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.example.chores.utils.userInterface
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


class AccountFragment: Fragment(),
    postClickListener, userInterface {
    var postList = ArrayList<postData>()
    lateinit var postsAdapter: postAdapter
    private var userInfo:UserInfoResponse = UserInfoResponse("",
    "",
    "",
    ArrayList<String>(),
    0,
    0,
    0.0,
    "",
    "")
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var millis:Long = System.currentTimeMillis()/1000
    var loading = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_account, container, false)

        fragment.refresh_user_account.setOnRefreshListener{
            getPosts()
        }
        if(activity!!.intent.getSerializableExtra("userInfo") != null)
        userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse

        var recyclerView :RecyclerView = fragment.findViewById(R.id.user_posts)
        postsAdapter = postAdapter(postList,this,userInfo)
        recyclerView.adapter = postsAdapter
        val layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.setItemViewCacheSize(5)
        recyclerView.setNestedScrollingEnabled(false)

        
        fragment.account_scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val displayMetrics = DisplayMetrics()
                (context as Activity?)!!.windowManager
                    .defaultDisplay
                    .getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val totalHeight: Int = fragment.account_scroll_view.getChildAt(0).getHeight()
            if(scrollY+height - totalHeight  >= -1000){
                    if (!loading) {
                        loading=true
                        getPosts()
                        Log.i("message","scrolling loading $loading")
                        refresh_user_account.isRefreshing=true
                    }
                }
            Log.i("message","${scrollY+height - totalHeight}  $oldScrollY $totalHeight $loading ${scrollY+height - totalHeight  >= 0}")
        }
        if(postList.size==0){
            getPosts()
            fragment.refresh_user_account.isRefreshing = true
        }
        return fragment
    }

    private fun getPosts() {
//        refresh_user_account.isRefreshing = true
        val sharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token"," ")
        val id = sharedPreferences.getString("id"," ")

        lateinit var timeInfoAndUserId :timeInfoAndUserId

        if(postList.size == 0){
            timeInfoAndUserId  = timeInfoAndUserId(0,millis)
        }else {
            timeInfoAndUserId = timeInfoAndUserId(millis, postList[postList.size - 1].time)
        }
        val retrofitData = retrofitBuilder.getUserPosts("$token id $id", timeInfoAndUserId)

        retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
            override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable) {
                Toast.makeText(context,"this is an error ${t.message}",Toast.LENGTH_LONG)
            }
            override fun onResponse(
                call: Call<ArrayList<postData>?>,
                response: Response<ArrayList<postData>?>
            ) {
                if(response.body()!!.size>0){
                    postList.addAll(response.body()!!)
                    for(i in 0..response.body()!!.size){
                        if(millis>response.body()!![i].time){
                            break
                        }else{
                            millis=response.body()!![i].time
                        }
                    }
                    postsAdapter.notifyDataSetChanged()
                    loading=false
                }
                refresh_user_account.isRefreshing=false
            }
        })
    }

    override fun userNameClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun postClick(position: Int) {
        val intent  = Intent(activity,Post_full_Screen::class.java)
        intent.putExtra("userInfo",userInfo)
        intent.putExtra("postInfo",postList[position])
        startActivity(intent)
    }

    override fun likeClick(position: Int) {
        PostRelated().likePost(context!!,postList[position],userInfo,this)
    }

    override fun disLikeCLick(position: Int) {
        PostRelated().disLikePost(context!!,postList[position],userInfo,this)
    }

    override fun addCommentClick(
        position: Int,
        comment: String,
        comment_write: EditText,
        comment_view: LinearLayout
    ) {
        CommentRelated().likeComment(context!!,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun comment(position: Int, username: TextView) {
        username.text = userInfo.username
    }

    override fun unauthorized() {
        val sharedPref: SharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  =Intent(activity,LoginActivity::class.java)
        startActivity(intent)
    }
}