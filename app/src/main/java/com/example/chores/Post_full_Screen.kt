package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.*
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.ClickListeners.commentClickListener
import com.example.chores.utils.CommentAdapter
import com.example.chores.utils.commentData
import com.example.chores.utils.postData
import kotlinx.android.synthetic.main.activity_post_full__screen.*
import kotlinx.android.synthetic.main.posts.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class Post_full_Screen : AppCompatActivity(),
    commentClickListener {
    var commentList =ArrayList<commentData>()
    lateinit var commentAdapter: CommentAdapter
    var isLoading = false
    var hasMore = true
    val millis:Long = System.currentTimeMillis()/1000
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    lateinit var postInfo :postData
    lateinit var userInfo :UserInfoResponse
    var position :Int = 0

    @RequiresApi(21)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_full__screen)

        postInfo = this.intent.getParcelableExtra<postData>("postInfo")!!
        userInfo =this.intent.getSerializableExtra("userInfo") as UserInfoResponse
        position = this.intent.getIntExtra("position",0)
        showPost()
        Log.i("message","$postInfo postInfo")
        Log.i("message","$userInfo postInfo")
        val recyclerView :RecyclerView = findViewById(R.id.post_show_comment)
        commentAdapter = CommentAdapter(commentList,this)
        val layoutManager = LinearLayoutManager(this.applicationContext)
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.setNestedScrollingEnabled(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if(lastVisiblePosition >= commentList.size-1 && hasMore && !isLoading){
                    refresh_full_post.setRefreshing(true)
                    getComments()
                }
            }
        })
        getComments()
        post_back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("postData",postInfo)
            intent.putExtra("position",position)
            setResult(151,intent)
            finish()
        }
    }
    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("postData",postInfo )
        intent.putExtra("position",position)
        setResult(151, intent)
        finish()
    }

    @RequiresApi(21)
    private fun showPost() {
        Glide.with(this).load(postInfo.profile_pic).placeholder(R.drawable.account).into(findViewById(R.id.posts_userImage))
        Glide.with(this).load(userInfo.profile_pic).placeholder(R.drawable.account).into(findViewById(R.id.applyImage))
        Glide.with(this).load(postInfo.url).placeholder(R.drawable.ic_outline_image_24).into(findViewById(R.id.posts_image))
        posts_created.setText(postInfo.created)
        posts_username.setText(userInfo.username)
        username_top.setText(userInfo.username)
        posts_username2.setText(userInfo.username)
        posts_exact_location.setText(postInfo.address)
        posts_pincode.setText(postInfo.pincode)
        posts_price_tag.setText(postInfo.price_tag)
        posts_info.setText(postInfo.info)
        post_likes.setText(postInfo.likes.toString())
        post_comments.setText(postInfo.comments.toString())
        posts_image.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimaryDark))
        if(postInfo.info.length>20){
            posts_info.text = postInfo.info.substring(0,20) + " ..."
            post_info_show_more.visibility = View.VISIBLE
        }else{
            posts_info.text = postInfo.info
        }
        if(postInfo.liked){
            post_like.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.liked));
        }
        post_info_show_more.setOnClickListener{
            showMore()
        }
        post_like.setOnClickListener {
            if(postInfo.liked){
                postInfo.liked = false
                post_like.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.like));
                postInfo.likes-=1
                post_likes.setText(postInfo.likes.toString())
//                postInterface.postDataChanged(position,postInfo)
                disLikePost()
            }else{
                post_like.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.liked));
                postInfo.likes+=1
                post_likes.setText(postInfo.likes.toString())
                postInfo.liked=true
//                postInterface.postDataChanged(position,postInfo)
                likePost()
            }
        }
        post_comment.setOnClickListener {
            comment_view.visibility = View.VISIBLE
            post_comment_username.text = userInfo.username
        }
        add_comment.setOnClickListener{
            addComment()
            postInfo.comments+=1
            post_comments.setText(postInfo.comments.toString())
        }
    }

    private fun addComment() {
        val calendar = Calendar.getInstance()
        val date = DateFormat.getDateInstance().format(calendar.time)
        val millis:Long = System.currentTimeMillis()/1000
        val sp = this.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val comment_id = UUID.randomUUID()
        val commentAdd = CommentAddJson(postInfo.post_id,comment_id,id!!,userInfo.username,userInfo.profile_pic,comment_write.text.toString(),millis,0,date)
        val new_comment = commentData(postInfo.post_id,comment_id.toString(),id!!,userInfo.username,userInfo.profile_pic,comment_write.text.toString(),millis,0,date,false )
        commentList.add(0,new_comment)
        commentAdapter.notifyDataSetChanged()
        val retrofitData = retrofitBuilder.addComment("$token id $id",commentAdd)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"Error: ${t.message}",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error",true)){
                    Toast.makeText(this@Post_full_Screen,"${response.body()}", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@Post_full_Screen,"Commented",Toast.LENGTH_LONG).show()
                    comment_write.setText("")
                    comment_view.visibility = View.GONE
                }
            }
        })
    }

    private fun likePost() {
        val sp = this.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postlike = likePostJson(postInfo.post_id,postInfo.user_id,userInfo.username,userInfo.profile_pic)
        val retrofitData = retrofitBuilder.likePost("$token id $id",postlike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"ERROR: ${t.message}",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(this@Post_full_Screen,"ERROR: ${response.body()}",Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        val sharedPref: SharedPreferences = this@Post_full_Screen.getSharedPreferences("chores", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("id", "")
                        editor.putString("token", "")
                        editor.putString("username","")
                        val intent  =Intent(this@Post_full_Screen,LoginActivity::class.java)
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this@Post_full_Screen,"${response.body()}",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun disLikePost() {
        val sp = this.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val postDislike = disLikePostJson(postInfo.post_id,userInfo.user_id)
        val retrofitData = retrofitBuilder.dislikePost("$token id $id",postDislike)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"Error: ${t.message}",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.body()!!.contains("Error:",true)){
                    Toast.makeText(this@Post_full_Screen,"ERROR: ${response.body()}",Toast.LENGTH_LONG).show()
                    if(response.body()!!.contains("unauthorized",true) || response.body()!!.contains("Cannot Retrieve user",true)){
                        val sharedPref: SharedPreferences = this@Post_full_Screen.getSharedPreferences("chores", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("id", "")
                        editor.putString("token", "")
                        editor.putString("username","")
                        val intent  =Intent(this@Post_full_Screen,LoginActivity::class.java)
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this@Post_full_Screen,"${response.body()}",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun showMore() {
        posts_info.text = postInfo.info
        post_info_show_more.visibility = View.GONE
    }

    private fun getComments() {
        val sharedPreferences = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        lateinit var timeInfo : timeInfoAndPostId
        if(commentList.size==0){
            timeInfo = timeInfoAndPostId(0,millis,postInfo.post_id)
        }else{
            timeInfo = timeInfoAndPostId(millis,commentList[(commentList.size)-1].time,postInfo.post_id)
        }
        val retrofitData = retrofitBuilder.getComments("$token id $id",timeInfo)
        retrofitData.enqueue(object : Callback<ArrayList<commentData>?> {
            override fun onFailure(call: Call<ArrayList<commentData>?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"${t.message}",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<commentData>?>,
                response: Response<ArrayList<commentData>?>
            ) {
                if(response.body()!!.size==0) hasMore =false
                else{
                    commentList.addAll(response.body()!!)
                    commentAdapter.notifyDataSetChanged()
                    Log.i("response.body()","${response.body()} response")
                }
                isLoading = false
                refresh_full_post.setRefreshing(false)
                }
        })
    }

    //comment click listener
    override fun likeClickComment(position:Int) {
        val sharedPreferences = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        val retrofitData = retrofitBuilder.likeComment("$token id $id", commentId(commentList[position].comment_id))

        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"${t.message}",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.code()==400){
                    Toast.makeText(this@Post_full_Screen,"Error:${response.body()}",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@Post_full_Screen,"${response.body()}",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun dislikeClickComment(position: Int) {
        val sharedPreferences = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        val retrofitData = retrofitBuilder.dislikeComment("$token id $id", commentId(commentList[position].comment_id))
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"${t.message}",Toast.LENGTH_LONG).show()

            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.code()==400){
                    Toast.makeText(this@Post_full_Screen,"Error:${response.body()}",Toast.LENGTH_LONG).show()
                }else if (response.code()==401){
                    val intent= Intent(this@Post_full_Screen,LoginActivity::class.java)
                    finishAffinity()
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this@Post_full_Screen,"${response.body()}",Toast.LENGTH_LONG).show()
                }

            }
        })
    }

}