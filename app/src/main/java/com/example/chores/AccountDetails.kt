package com.example.chores

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.postAdapter
import com.example.chores.utils.postData
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.example.chores.utils.userInterface
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_account_details.account_scroll_view
import kotlinx.android.synthetic.main.activity_account_details.info_userimage_button
import kotlinx.android.synthetic.main.activity_account_details.info_username2
import kotlinx.android.synthetic.main.activity_account_details.pBar
import kotlinx.android.synthetic.main.activity_account_details.refresh_user_account
import kotlinx.android.synthetic.main.activity_account_details.userinfo_bio
import kotlinx.android.synthetic.main.activity_account_details.userinfo_name
import kotlinx.android.synthetic.main.activity_account_details.userinfo_pincodes
import kotlinx.android.synthetic.main.activity_account_details.userinfo_website
import kotlinx.android.synthetic.main.activity_account_details.username_heading
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.*
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.accept
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.decline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountDetails : AppCompatActivity(),postClickListener,userInterface {

    var postList =  ArrayList<postData>()
    lateinit var postsAdapter: postAdapter
    lateinit var userInfo :UserInfoResponse
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var loading =false
    var millis :Long = System.currentTimeMillis()/1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        val userId = this.intent.getStringExtra("userId")!!

        val retrofitData = retrofitBuilder.getUserInfoById(id = userId)
        retrofitData.enqueue(object : Callback<UserInfoResponse?> {
            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                Toast.makeText(applicationContext,"Check Internet Connection",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                Log.i("message"," jioajds ${response.code()}")
                if(response.code() == 500){
                    Toast.makeText(applicationContext,"Server Error Try Again!",Toast.LENGTH_LONG).show()
                }else if(response.code()==404){
                    Toast.makeText(applicationContext,"We are not able to retrieve the user",Toast.LENGTH_LONG).show()
                    pBar.visibility = View.GONE
                    error.visibility = View.VISIBLE
                }
                else{
                    pBar.visibility = View.GONE
                    refresh_user_account.visibility = View.VISIBLE
                    userInfo = response.body()!!
                    showUserInfo()
                }
            }
        })
        if(this::userInfo.isInitialized) showUserInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_CANCELED){
            when(requestCode){
                151->{
                    if(data!!.getParcelableExtra<postData>("postData")!=null){
                        val post = data!!.getParcelableExtra<postData>("postData")!!
                        Log.i("message hii","${post}")
                        val position = data!!.getIntExtra("position",0)
                        postList[position] = post
                        postsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun showUserInfo() {
        val sp = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sp.getString("id","")
        Log.i("message","${userInfo}")
        username_heading.text = userInfo.username
        username_heading.text = userInfo.username
        userinfo_name.text = userInfo.name
        info_username2.text = userInfo.username
        userinfo_bio.text = userInfo.bio
        back.setOnClickListener { finish() }
        Log.i("message","etxt ${userInfo.website.isNullOrBlank()}")
        if(userInfo.website.isNullOrBlank()){
            userinfo_website.visibility = View.GONE
        }else{
            if(userInfo.website.indexOf("https")!=-1) userinfo_website.text = "${userInfo.website.substringAfter("https://","chores.com/").substringBefore("/")}"
            else userinfo_website.text = "${userInfo.website.substringAfter("http://","chores.com/").substringBefore("/")}"
            userinfo_website.setOnClickListener {
                Log.i("message","open")
                var url = userInfo.website
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                Log.i("message","${userInfo.website}")
                startActivity(browserIntent)
            }
        }

        Glide.with(this).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(findViewById(R.id.info_userImage) )
        if(userInfo.user_id == id){
            info_userimage_button.visibility = View.VISIBLE
//            info_userimage_button.setOnClickListener{
//                launchImageCropper()
//            }

        }
        userinfo_pincodes.setOnClickListener {
            OpenPincodesDialogBox()
        }

        val recyclerView :RecyclerView = findViewById(R.id.user_posts)
        postsAdapter = postAdapter(postList,this,userInfo)
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = postsAdapter
        recyclerView.setItemViewCacheSize(5)
        recyclerView.setNestedScrollingEnabled(true)
        if(postList.size == 0){
            getPosts()
            pBar_posts.visibility = View.VISIBLE
        }

        account_scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val displayMetrics = DisplayMetrics()
            this.windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val totalHeight: Int = account_scroll_view.getChildAt(0).getHeight()
            if(scrollY+height - totalHeight  >= -1000){
                if (!loading) {
                    loading=true
                    getPosts()
                    refresh_user_account.isRefreshing=true
                    pBar_posts.visibility = View.VISIBLE
                }
            }
        }

    }
    private fun getPosts() {
//        refresh_user_account.isRefreshing = true
        val sharedPreferences = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token"," ")
        val id = sharedPreferences.getString("id"," ")

        lateinit var timeInfoAndUserId : timeInfoAndUserId

        if(postList.size == 0){
            timeInfoAndUserId  = timeInfoAndUserId(0,millis)
        }else {
            timeInfoAndUserId = timeInfoAndUserId(millis, postList[postList.size - 1].time)
        }
        val retrofitData = retrofitBuilder.getUserPostsById(timeInfoAndUserId,userInfo.user_id)

        retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
            override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable) {
                Toast.makeText(applicationContext,"this is an error ${t.message}",Toast.LENGTH_LONG)
            }
            override fun onResponse(
                call: Call<ArrayList<postData>?>,
                response: Response<ArrayList<postData>?>
            ) {
                if(response.code() == 500){

                }else{
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
                    pBar_posts.visibility = View.GONE
                }
            }
        })
    }

    private fun OpenPincodesDialogBox() {
        val AlertDialog = AlertDialog.Builder(this).create();
        val layoutInflater = this.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.pincodes_dialog_box, null)

        dialogView.show_pincode1.visibility = View.VISIBLE
        dialogView.show_pincode2.visibility = View.VISIBLE
        dialogView.show_pincode3.visibility = View.VISIBLE

        dialogView.info_pincode1.visibility = View.GONE
        dialogView.info_pincode2.visibility = View.GONE
        dialogView.info_pincode3.visibility = View.GONE

        dialogView.decline.visibility =View.GONE
        dialogView.accept.visibility =View.GONE

        dialogView.close.visibility =View.VISIBLE

        dialogView.close.setOnClickListener { AlertDialog.dismiss() }


        if(userInfo.pincodes.size>0){
            for( i in (0 .. userInfo.pincodes.size-1)){
                if(i==0) dialogView.show_pincode1.setText(userInfo.pincodes[i])
                if(i==1) dialogView.show_pincode2.setText(userInfo.pincodes[i])
                if(i==2) dialogView.show_pincode3.setText(userInfo.pincodes[i])
            }
        }
//        dialogView.accept.setOnClickListener{
//            if(dialogView.info_pincode1.text.length>=3 && dialogView.info_pincode1.text.length<=16 && dialogView.info_pincode2.text.length>=3 && dialogView.info_pincode2.text.length<=16 && dialogView.info_pincode3.text.length>=3 && dialogView.info_pincode3.text.length<=16){
//                userInfo.pincodes[0]= dialogView.info_pincode1.text.toString()
//                userInfo.pincodes[1]=dialogView.info_pincode2.text.toString()
//                userInfo.pincodes[2] = dialogView.info_pincode3.text.toString()
//                AlertDialog.dismiss()
//                updateFunction()
//                Log.i("message","${userInfo.pincodes}")
//            }else{
//                if(dialogView.info_pincode1.text.length<3 || dialogView.info_pincode1.text.length>16 || dialogView.info_pincode2.text.length<3 && dialogView.info_pincode2.text.length>16 || dialogView.info_pincode3.text.length<3 || dialogView.info_pincode3.text.length>16){
//                    dialogView.info_error.setText("Pincodes are allowed to be of length between 3 to 16")
//                }else{
//                    dialogView.info_error.setText("Every field is required*")
//                }
//            }
//        }
        AlertDialog.setView(dialogView);
        AlertDialog.show();
    }


    override fun userNameClick(position: Int) {
        Toast.makeText(this@AccountDetails,"The same user!",Toast.LENGTH_LONG).show()
    }

    override fun postClick(position: Int) {
        val intent  = Intent(this,Post_full_Screen::class.java)
        intent.putExtra("userInfo",userInfo)
        intent.putExtra("postInfo",postList[position])
        intent.putExtra("position",position)
        Log.i("message","${postList[position]}")
        startActivityForResult(intent,151)
    }

    override fun likeClick(position: Int) {
        PostRelated().likePost(applicationContext,postList[position],userInfo,this)
    }

    override fun disLikeCLick(position: Int) {
        PostRelated().disLikePost(applicationContext,postList[position],userInfo,this)
    }

    override fun addCommentClick(
        position: Int,
        comment: String,
        comment_write: EditText,
        comment_view: LinearLayout
    ) {
        CommentRelated().likeComment(applicationContext,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun comment(position: Int, username: TextView) {
        username.text = userInfo.username
    }

    override fun unauthorized() {
        val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  =Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
}