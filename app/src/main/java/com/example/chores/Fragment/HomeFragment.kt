package com.example.chores.Fragment

import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.AccountDetails
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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment: Fragment(),
    postClickListener,userInterface {
    var postList = ArrayList<postData>()
    lateinit var postsAdapter :postAdapter
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var millis:Long = System.currentTimeMillis()/1000
    var loadMore:Boolean = true
    private var userInfo:UserInfoResponse = UserInfoResponse("",
        "",
        "",
        ArrayList<String>(),
        0,
        0,
        0.0,
        "",
        "",
        "",
        "")
    var noMoreChores =false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val yo =inflater.inflate(R.layout.fragment_home,container,false)
        if(activity!!.intent.getSerializableExtra("userInfo")!=null){
            userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
        }else{
            getUser()
        }

        yo.refresh_home.setOnRefreshListener{
            getPosts()
        }

        var recyclerView :RecyclerView = yo.findViewById(R.id.home_recycler_view)
        postsAdapter = postAdapter(postList,this,userInfo)
        val layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = postsAdapter
        recyclerView.setItemViewCacheSize(5)
        recyclerView.setNestedScrollingEnabled(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // super.onScrolled(recyclerView, dx, dy);
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisiblePosition == postList.size -1 || lastVisiblePosition == 0) {
                    Log.i("message","scrolling")
                    if (loadMore) {
                        loadMore = false
                        Log.i("message","scrolling")
                        getPosts()
                        refresh_home.isRefreshing=true
                        pBar_posts.visibility = View.VISIBLE
                    }
                }
            }
        })
        if(postList.size==0 &&userInfo.name.isNotEmpty()){
            getPosts()
        }
        return yo
    }

    private fun getUser() {
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val sp = activity!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitData = retrofitBuilder.getUserInfo("$token id $id")
        retrofitData.enqueue(object : Callback<UserInfoResponse?> {
            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                if(t.message!!.contains("Failed to connect to",true)) {
                    Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_LONG).show()
                    error.visibility = View.VISIBLE
                    refresh_home.visibility = View.GONE
                }
            }
            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                userInfo = response.body()!!
                Log.i("message","${response.body()} hiiii")
                getPosts()
                pBar_posts.visibility = View.VISIBLE
            }
        })
    }


    private fun getPosts() {
        val sharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        lateinit var timeInfo :timeInfoAndUserId
        if(postList.size==0){
            timeInfo = timeInfoAndUserId(0,millis)
        }else{
            timeInfo = timeInfoAndUserId(millis,postList[(postList.size)-1].time)
        }
        val retrofitData = retrofitBuilder.getPosts("$token id $id",timeInfo)
        retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
            override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable){
                Toast.makeText(context,"${t.message}",Toast.LENGTH_LONG).show()
                error.visibility = View.VISIBLE
                refresh_home.visibility = View.GONE
            }

            override fun onResponse(
                call: Call<ArrayList<postData>?>,
                response: Response<ArrayList<postData>?>
            ) {
                if(response.body()!!.size>0&&response.body()!![0].profile_pic.contains("Error",true)){
                    Toast.makeText(context,"ERROR:",Toast.LENGTH_LONG).show()
                }else{
                    postList.addAll(response.body()!!)
                    if(response.body()!!.size==0) noMoreChores = true
                    else
                    for(i in 0..response.body()!!.size){
                        if(millis>response.body()!![i].time){
                            break
                        }else{
                            millis=response.body()!![i].time
                        }
                    }
                    loadMore = true
                    postsAdapter.notifyDataSetChanged()
                    refresh_home.isRefreshing=false
                    pBar_posts.visibility = View.GONE
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != RESULT_CANCELED){
            when(requestCode){
                151->{
                    if(data!!.getParcelableExtra<postData>("postData")!=null){
                        val post = data!!.getParcelableExtra<postData>("postData")!!
                        val position = data!!.getIntExtra("position",0)
                        postList[position] = post
                        postsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // post click listeners
    override fun userNameClick(position: Int) {
        val intent = Intent(activity!!, AccountDetails::class.java)
        intent.putExtra("userId",postList[position].user_id)
        startActivity(intent)
    }

    override fun addCommentClick(position: Int, comment: String,comment_write:EditText,comment_view:LinearLayout) {
        CommentRelated().likeComment(context!!,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun likeClick(position: Int) {
        PostRelated().likePost(context!!,postList[position],userInfo,this)
    }

    override fun disLikeCLick(position: Int) {
        PostRelated().disLikePost(context!!,postList[position],userInfo,this)
    }
    override fun postClick(position: Int) {
        val intent = Intent(activity,Post_full_Screen::class.java)
        intent.putExtra("userInfo",userInfo)
        intent.putExtra("postInfo",postList[position])
        intent.putExtra("position",position)
        startActivityForResult(intent,151)
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