package com.example.chores

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.*
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.*
import com.example.chores.utils.Adapters.CommentAdapter
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ClickListeners.commentClickListener
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.usefullFunctions.PostRelated
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_post_full__screen.*
import kotlinx.android.synthetic.main.apply_on_post.*
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.posts.*
import kotlinx.android.synthetic.main.posts.assigned
import kotlinx.android.synthetic.main.posts.rejected
import kotlinx.android.synthetic.main.posts.waiting
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class Post_full_Screen : AppCompatActivity(),
    commentClickListener,userInterface,questionAnswerClickListener {
    var commentList =ArrayList<commentData>()
    lateinit var commentAdapter: CommentAdapter
    var isLoading = false
    var hasMore = true
    var uploading = false
    var new_resume = 0
    lateinit var uri_resume : Uri
    val millis:Long = System.currentTimeMillis()
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var postInfo :postData = postData("","","","","","","","","",0,0,"",0,false,0,"",false,ArrayList<String>(),"null","null","null","null","null")
    var userInfo :UserInfoResponse = UserInfoResponse("","","",ArrayList<String>(),0,0,0.0,"","","","","")
    var position :Int = 0
    lateinit var AnswersArray :ArrayList<String>
    lateinit var QuestionAnswerAdapter:QuestionAnswerAdapter

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_full__screen)

        Log.i("message","")
        try{
            Log.i("message","${this.intent.getStringExtra("postId")} hiiiiiiiii")
            if(this.intent.getStringExtra("postId") !=null){
                Log.i("message","${this.intent.getStringExtra("postId")!!}")
                postInfo = postData("","","","","",this.intent.getStringExtra("postId")!!,"","","",0,0,"",0,false,0,"",false,ArrayList<String>(),"null","null","null","null","null")
                getPostInfo()
                userInfo = this.intent.getSerializableExtra("userInfo") as UserInfoResponse
            }else{
                Log.i("message","shits hell no")
                postInfo = this.intent.getSerializableExtra("postInfo") as postData
                userInfo =this.intent.getSerializableExtra("userInfo") as UserInfoResponse
                position = this.intent.getIntExtra("position",0)
            }
        }catch(e:Exception){
            Log.i("message","Exception $e")
        }

        val layout :RelativeLayout = findViewById(R.id.bottom_sheet_apply)
        val bottomSheetBehavior = BottomSheetBehavior.from(layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        showPost()
        val recyclerView :RecyclerView = findViewById(R.id.post_show_comment)
        commentAdapter =
            CommentAdapter(commentList, this)
        val layoutManager = LinearLayoutManager(this.applicationContext)
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.setNestedScrollingEnabled(true)

        post_screen.setOnScrollChangeListener{v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val displayMetrics = DisplayMetrics()
            (this as Activity?)!!.windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val totalHeight: Int = post_screen.getChildAt(0).getHeight()
            if(scrollY+height - totalHeight  >= -1000){
                if (!isLoading && hasMore) {
                    isLoading=true
                    getComments()
                    refresh_full_post.isRefreshing=true
                }
            }
        }
        if(commentList.size == 0 && postInfo.user_id!="") getComments()
        post_back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("postData",postInfo)
            intent.putExtra("position",position)
            setResult(151,intent)
            finish()
        }
    }


    @RequiresApi(21)
    private fun getPostInfo() {
        val retrofitData = retrofitBuilder.getPost(postInfo.post_id)
        retrofitData.enqueue(object : Callback<postData?> {
            override fun onFailure(call: Call<postData?>, t: Throwable) {
                Toast.makeText(applicationContext,"Internet Connection Required",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<postData?>, response: Response<postData?>) {
                if(response.isSuccessful){
                    Log.i("message","${response.body()!!} post")
                    postInfo = response.body()!!
                    val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
                    val token = sp.getString("token","")
                    val id =sp.getString("id","")
                    val retro = retrofitBuilder.getPostStatus("$token id $id",postInfo.post_id)
                    retro.enqueue(object : Callback<String?> {
                        override fun onFailure(call: Call<String?>, t: Throwable) {
                            Toast.makeText(applicationContext,"Internet Connection Required",Toast.LENGTH_LONG).show()
                        }
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if(response.isSuccessful){
                                postInfo.status = response.body()!!
                                Log.i("message","hiii got till herreererer")
                                showPost()
                                getComments()
                            }else{
                                Toast.makeText(applicationContext,"Sever Error! Try Again Later!",Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }else{
                    Toast.makeText(applicationContext,"Error! Try Again Later!",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onBackPressed() {
        if(uploading){
            Toast.makeText(applicationContext,"Wait for the Uploading to finish",Toast.LENGTH_LONG).show()
        }else{
            val intent = Intent()
            intent.putExtra("postData",postInfo )
            intent.putExtra("position",position)
            setResult(151, intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_CANCELED){
            when(requestCode){
                190->{
                    uri_resume = data!!.data!!
                    new_resume=1
                }

            }
        }
    }
     private fun showTags(){
         Log.i("message","${postInfo.tag1.length} tag1")
         if(postInfo.tag1 !="null"&& postInfo.tag1 != ""){
             tags.visibility =View.VISIBLE
             tag1.visibility = View.VISIBLE
             tag1.text = postInfo.tag1
         }
         if(postInfo.tag2 !="null"&& postInfo.tag2 != ""){
             tag2.visibility = View.VISIBLE
             tag2.text = postInfo.tag2
         }
         if(postInfo.tag3 !="null" && postInfo.tag3 != ""){
             tag3.visibility = View.VISIBLE
             tag3.text = postInfo.tag3
         }
         if(postInfo.tag4 !="null"&& postInfo.tag4 != ""){
             tag4.visibility = View.VISIBLE
             tag4.text = postInfo.tag4
         }
         if(postInfo.tag5 !="null"&& postInfo.tag5 != ""){
             tag5.visibility = View.VISIBLE
             tag5.text = postInfo.tag5
         }
     }

    @RequiresApi(21)
    private fun showPost() {
        Glide.with(this).load(postInfo.profile_pic).placeholder(R.drawable.account).into(findViewById(R.id.posts_userImage))
        showTags()

        if(postInfo.address == "GLOBAL"){
            global.visibility =View.VISIBLE
            posts_pincode.visibility = View.GONE
        }
        else{
            global.visibility =View.GONE
            posts_pincode.visibility = View.VISIBLE
            posts_pincode.text = postInfo.pincode
        }

        if(postInfo.user_id == userInfo.user_id){
            post_more.visibility = View.VISIBLE
            applyImage1.visibility = View.GONE
            post_more.setOnClickListener {
                postMenu()
            }
        }else{
            if(postInfo.status == "rejected"){
                rejected.visibility = View.VISIBLE
                applyImage1.visibility = View.GONE
                rejected.setOnClickListener {
                    apply_on_post()
                }
            }else if(postInfo.status == "waiting"){
                waiting.visibility = View.VISIBLE
                applyImage1.visibility = View.GONE
                waiting.setOnClickListener {
                    apply_on_post()
                }
            }else if(postInfo.status == "assigned"){
                assigned.visibility = View.VISIBLE
                applyImage1.visibility = View.GONE
                assigned.setOnClickListener {
                    apply_on_post()
                }
            }else{
                Log.i("message","got in else")
                Glide.with(this).load(userInfo.profile_pic).placeholder(R.drawable.account).into(findViewById(R.id.applyImage))
                applyImage.setOnClickListener {
                    apply_on_post()
                }
            }
        }

        applied.setText(postInfo.applied.toString())
        Glide.with(this).load(postInfo.url).placeholder(R.drawable.ic_outline_image_24).into(findViewById(R.id.posts_image))
        posts_created.setText(DateUtils.getRelativeTimeSpanString(postInfo.time))
        posts_username.setText(postInfo.username)
        username_top.setText(postInfo.username)
        posts_username2.setText(postInfo.username)
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
        username_top.setOnClickListener{
            userNameClick()
        }
        posts_username2.setOnClickListener{
            userNameClick()
        }
        posts_username.setOnClickListener{
            userNameClick()
        }
        posts_userImage.setOnClickListener {
            userNameClick()
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

    private fun userNameClick() {
        val intent = Intent(this,AccountDetails::class.java)
        intent.putExtra("userId",postInfo.user_id)
        intent.putExtra("selfInfo",userInfo)
        startActivity(intent)
    }

    private fun postMenu() {
        val popupMenu = PopupMenu(applicationContext,post_more)
        popupMenu.menuInflater.inflate(R.menu.post_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.applied_by->{
                    val intent  =Intent(this@Post_full_Screen,AppliedByList::class.java)
                    intent.putExtra("post_id",postInfo.post_id)
                    intent.putExtra("postInfo",postInfo)
                    startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()
    }
    private fun sendAnswers():ArrayList<String>{
        return AnswersArray
    }

    private fun questionsView(layout: View,post:postData) {
        val recyclerView = layout.questionsAnswer_recycler_view!!
        if(post.status =="false"){
            layout.pBar_answers.visibility = View.GONE
            layout.questionsAnswer_recycler_view.visibility =View.VISIBLE
            QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,true,this,ArrayList<String>())
            if(this::AnswersArray.isInitialized) AnswersArray.clear()
            else AnswersArray = ArrayList<String>()
            for(i in 0..(post.questions.size-1)) AnswersArray.add("")
            recyclerView.adapter = QuestionAnswerAdapter
            val layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.layoutManager = layoutManager
            recyclerView.isNestedScrollingEnabled = false
        }
        else{
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
                        QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,false,this@Post_full_Screen,response.body()!!)
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
    fun apply_on_post(){
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
        if(postInfo.resume){
            if(postInfo.status!="false"){
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



        layout.location.text = postInfo.address
        layout.description.text = postInfo.info
        layout.created.text = DateUtils.getRelativeTimeSpanString(postInfo.time)
        Glide.with(applicationContext).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(layout.userimage)
        layout.username.text = userInfo.username
        if(postInfo.status == "assigned"){
            layout.assigned.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.rejected.visibility = View.GONE
            layout.waiting.visibility = View.GONE
        }else if(postInfo.status == "waiting"){
            layout.waiting.visibility = View.VISIBLE
            layout.apply.visibility = View.GONE
            layout.assigned.visibility = View.GONE
            layout.rejected.visibility = View.GONE
        }else if(postInfo.status == "rejected"){
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
                    postInfo.applied += 1;
                    postInfo.status = "waiting";
                    val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
                    val id = sp.getString("id","")!!
                    if(id!=postInfo.user_id) notify().notifyUser(applicationContext,postInfo.user_id,postInfo.post_id,id,"apply",System.currentTimeMillis(),"",postInfo.url,this)

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

                if(postInfo.resume){
                    if(k==0){
                        if(resumeChoosed() == 1){
                            fun uploader(url:String, pb: ProgressDialog, useless:String){
                                PostRelated().apply(applicationContext,postInfo,userInfo.user_id,this,::applied,answers,url)
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
                            ResumeUtils().uploadResume("${userInfo.user_id}/${postInfo.post_id}",r,cr,pb,"",::uploader)
                        }else {
                            if (!userInfo.resume.isNotEmpty()) {
                                Toast.makeText(applicationContext,"Resume must be selected!", Toast.LENGTH_LONG).show()
                            } else {
                                PostRelated().apply(applicationContext, postInfo, userInfo.user_id, this, ::applied, answers,userInfo.resume)
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
                    if(k==0) PostRelated().apply(applicationContext,postInfo,userInfo.user_id,this,::applied,answers,"")
                    if(k==1) Toast.makeText(applicationContext,"Every Question Must Be Answered!",Toast.LENGTH_LONG).show()
                }

            }
        }
        questionsView(layout,postInfo)
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


    private fun addComment() {
        val millis:Long = System.currentTimeMillis()
        val sp = this.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val comment_id = UUID.randomUUID()
        val commentAdd = CommentAddJson(postInfo.post_id,comment_id,id!!,userInfo.username,userInfo.profile_pic,comment_write.text.toString(),millis,0)
        val new_comment = commentData(postInfo.post_id,comment_id.toString(),id!!,userInfo.username,userInfo.profile_pic,comment_write.text.toString(),millis,0,false )
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
        PostRelated().likePost(applicationContext,postInfo,userInfo,this)
    }

    private fun disLikePost() {
        PostRelated().disLikePost(applicationContext,postInfo,userInfo,this)
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
        Log.i("message","${timeInfo}")
        val retrofitData = retrofitBuilder.getComments("$token id $id",timeInfo)
        retrofitData.enqueue(object : Callback<ArrayList<commentData>?> {
            override fun onFailure(call: Call<ArrayList<commentData>?>, t: Throwable) {
                Toast.makeText(this@Post_full_Screen,"${t.message}",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<commentData>?>,
                response: Response<ArrayList<commentData>?>
            ) {
                if(response.isSuccessful){
                    if(response.body()!!.size==0) hasMore =false
                    else{
                        commentList.addAll(response.body()!!)
                        commentAdapter.notifyDataSetChanged()
                    }
                    isLoading = false
                    refresh_full_post.setRefreshing(false)
                }else{
                    Log.i("message","${response.code()}")
                }
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