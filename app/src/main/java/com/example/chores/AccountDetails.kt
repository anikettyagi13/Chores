package com.example.chores

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.notify
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.ResumeUtils
import com.example.chores.utils.postData
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.example.chores.utils.userInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.*
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.accept
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.decline
import kotlinx.android.synthetic.main.posts.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountDetails : AppCompatActivity(),postClickListener,userInterface,questionAnswerClickListener {

    var postList =  ArrayList<postData>()
    lateinit var postsAdapter: postAdapter
    lateinit var userInfo :UserInfoResponse
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var selfInfo = UserInfoResponse("","","",ArrayList<String>(),0,0,0.0,"","","","","");
    var loading =false
    var uploading = false
    var new_resume = 0
    lateinit var uri_resume :Uri
    var millis :Long = System.currentTimeMillis()/1000
    lateinit var QuestionAnswerAdapter : QuestionAnswerAdapter
    lateinit var AnswersArray : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        val userId = this.intent.getStringExtra("userId")!!
        selfInfo = this.intent.getSerializableExtra("selfInfo") as UserInfoResponse
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

    override fun onBackPressed() {
        super.onBackPressed()
        if(uploading){
            Toast.makeText(applicationContext,"Wait for the Uploading to finish",Toast.LENGTH_LONG).show()
        }else{
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_CANCELED){
            when(requestCode){
                151->{
                    if(data!!.getSerializableExtra("postData")!=null){
                        val post = data!!.getSerializableExtra("postData")as postData
                        Log.i("message hii","${post}")
                        val position = data!!.getIntExtra("position",0)
                        postList[position] = post
                        postsAdapter.notifyDataSetChanged()
                    }
                }
                190->{
                    uri_resume = data!!.data!!
                    new_resume=1
                }

            }
        }
    }

    override fun showTags(position: Int) {
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
        if(userInfo.resume.isNotEmpty()) resume.visibility = View.VISIBLE
        else resume.visibility = View.GONE
        resume.setOnClickListener {

            val intent =Intent(this,WebView::class.java)
            intent.putExtra("url",userInfo.resume)
            startActivity(intent)
        }
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

        }
        userinfo_pincodes.setOnClickListener {
            OpenPincodesDialogBox()
        }

        val recyclerView :RecyclerView = findViewById(R.id.user_posts)
        postsAdapter = postAdapter(
            postList,
            this,
            selfInfo,
        false
        )
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
        AlertDialog.setView(dialogView);
        AlertDialog.show();
    }


    override fun userNameClick(position: Int) {
        Toast.makeText(this@AccountDetails,"The same user!",Toast.LENGTH_LONG).show()
    }

    override fun postClick(position: Int) {
        val intent  = Intent(this,Post_full_Screen::class.java)
        intent.putExtra("userInfo",selfInfo)
//        intent.putExtra("postInfo",postList[position])
        intent.putExtra("postId",postList[position].post_id)
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
        CommentRelated().AddComment(applicationContext,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun comment(position: Int, username: TextView) {
        username.text = userInfo.username
    }
    private fun sendAnswers():ArrayList<String>{
        return AnswersArray
    }

    private fun questionsView(layout: View,post:postData) {
        val recyclerView = layout.questionsAnswer_recycler_view!!
        if(post.status=="false"){
            QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,true,this,ArrayList<String>())
            if(this::AnswersArray.isInitialized) AnswersArray.clear()
            else AnswersArray = ArrayList<String>()
            for(i in 0..(post.questions.size-1)) AnswersArray.add("")
            recyclerView.adapter = QuestionAnswerAdapter
            val layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.layoutManager = layoutManager
            recyclerView.isNestedScrollingEnabled = false
        }else{
            layout.pBar_answers.visibility = View.VISIBLE
            layout.questionsAnswer_recycler_view.visibility =View.GONE
            val sp = getSharedPreferences("chores",Context.MODE_PRIVATE)
            val token = sp.getString("token","")!!
            val id = sp.getString("id","")!!
            val retrofitData = retrofitBuilder.getAnswers("${token} id ${id}",post.post_id)
            retrofitData.enqueue(object : Callback<ArrayList<String>?> {
                override fun onFailure(call: Call<ArrayList<String>?>, t: Throwable) {
                    Toast.makeText(applicationContext,"Internet Connection Required!",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ArrayList<String>?>,
                    response: Response<ArrayList<String>?>
                ) {
                    if(response.isSuccessful){
                        QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,false,this@AccountDetails,response.body()!!)
                        layout.pBar_answers.visibility = View.GONE
                        layout.questionsAnswer_recycler_view.visibility =View.VISIBLE
                        recyclerView.adapter = QuestionAnswerAdapter
                        val layoutManager = LinearLayoutManager(applicationContext)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.isNestedScrollingEnabled = false
                    }else{
                        if(response.code() == 401) unauthorized()
                        else Toast.makeText(applicationContext,"Error! Try Again Later",Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

    }

    override fun changeAnswer(position: Int, answer: String) {
        AnswersArray[position] = answer
    }
    override fun applyOnPost(position: Int) {
        val layout :View = findViewById(R.id.bottom_sheet_apply)
        val bottomSheetBehavior = BottomSheetBehavior.from(layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if(uploading) bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
        if(postList[position].resume){
            if(postList[position].status!="false"){
                layout.resume_choose.visibility = View.GONE
            }else{
                layout.resume_choose.visibility = View.VISIBLE
                if(userInfo.resume.isNotEmpty()){
                    layout.see_resume.visibility = View.VISIBLE
                    layout.see_resume.setOnClickListener {
                        seeResume()
                    }
                }
            }
        }else{
            layout.resume_choose.visibility = View.GONE
        }



        layout.location.text = postList[position].address
        layout.description.text = postList[position].info
        layout.created.text = DateUtils.getRelativeTimeSpanString(postList[position].time)
        Glide.with(applicationContext).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(layout.userimage)
        layout.username.text = userInfo.username
        if(postList[position].status == "assigned"){
            layout.assigned.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.rejected.visibility = View.GONE
            layout.waiting.visibility = View.GONE
        }else if(postList[position].status == "waiting"){
            layout.waiting.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.assigned.visibility = View.GONE
            layout.rejected.visibility = View.GONE
        }else if(postList[position].status == "rejected"){
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
                    unauthorized()
                }else if(i == 500){
                    Toast.makeText(applicationContext,"Error: Cannot apply now. Try Later!",Toast.LENGTH_LONG).show()
                }else if(i == 404){
                    Toast.makeText(applicationContext,"Error: Cannot apply now. Try Later!",Toast.LENGTH_LONG).show()
                }else{
                    postList[position].applied += 1;
                    postList[position].status = "waiting";
                    val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
                    val id = sp.getString("id","")!!
                    if(id!=postList[position].user_id) notify().notifyUser(applicationContext,postList[position].user_id,postList[position].post_id,id,"apply",System.currentTimeMillis(),"",postList[position].url,this)

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            layout.apply.setOnClickListener {
                val answers =  sendAnswers()
                var k=0;

                for (i in answers){
                    if(i.length == 0 ) {
                        k=1;
                        break;
                    }
                }

                if(postList[position].resume){
                    if(k==0){
                        if(resumeChoosed() ==1){
                            fun uploader(url:String, pb: ProgressDialog, useless:String){
                                PostRelated().apply(applicationContext,postList[position],userInfo.user_id,this,::applied,answers,url)
                                pb.dismiss()
                                set_uploading(false)
                            }
                            var r = getResume()
                            var cr = contentResolver
                            var pb = ProgressDialog(applicationContext)
                            pb.setTitle("Uploading Resume")
                            pb.setCanceledOnTouchOutside(false)
                            set_uploading(true)
                            pb.show()
                            ResumeUtils().uploadResume("${userInfo.user_id}/${postList[position].post_id}",r,cr,pb,"",::uploader)
                        }else {
                            if (!userInfo.resume.isNotEmpty()) {
                                Toast.makeText(applicationContext,"Resume must be selected!", Toast.LENGTH_LONG).show()
                            } else {
                                PostRelated().apply(applicationContext, postList[position], userInfo.user_id, this, ::applied, answers,userInfo.resume)
                            }
                        }
                    }else {
                        Toast.makeText(
                            applicationContext,
                            "Every Question Must Be Answered!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }else{
                    if(k==0) PostRelated().apply(applicationContext,postList[position],userInfo.user_id,this,::applied,answers,"")
                    if(k==1) Toast.makeText(applicationContext,"Every Question Must Be Answered!",Toast.LENGTH_LONG).show()
                }

            }
        }
        questionsView(layout,postList[position])
        layout._resume_.setOnClickListener { choose_resume(layout) }
        layout.closed.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
    }

    public fun choose_resume(layout:View){
        var intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("application/pdf")
        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent,190)
        layout.see_resume.visibility = View.VISIBLE
        layout.see_resume.setOnClickListener {
            seeResume()
        }
    }

    public fun seeResume(){
        if(new_resume==1){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setData(uri_resume)
            startActivity(intent)
        }else{
            val intent =Intent(applicationContext,WebView::class.java)
            intent.putExtra("url",userInfo.resume)
            startActivity(intent)
        }
    }

    fun getResume():Uri{
        return uri_resume
    }

    fun resumeChoosed():Int{
        return new_resume
    }
    fun set_uploading(up:Boolean){
        uploading = up
    }


    override fun showMenu(position: Int, post_more: View) {
        val popupMenu = PopupMenu(applicationContext,post_more)
        popupMenu.menuInflater.inflate(R.menu.post_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.edit_post->{
                    Toast.makeText(applicationContext,"edit",Toast.LENGTH_LONG).show()
                }
                R.id.delete->{
                    Toast.makeText(applicationContext,"edit",Toast.LENGTH_LONG).show()
                }
                R.id.applied_by->{
                    val intent  =Intent(this,AppliedByList::class.java)
                    intent.putExtra("post_id",postList[position].post_id)
                    intent.putExtra("postInfo",postList[position])
                    startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()
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