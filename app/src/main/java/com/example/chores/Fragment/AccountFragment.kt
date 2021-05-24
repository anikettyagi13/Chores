package com.example.chores.Fragment

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.*
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.*
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.dialog_location_picker.view.accept
import kotlinx.android.synthetic.main.dialog_location_picker.view.decline
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AccountFragment(val userInfoInterface: userInfoInterface): Fragment(),
    postClickListener, userInterface,questionAnswerClickListener {
    var postList = ArrayList<postData>()
    lateinit var postsAdapter: postAdapter
    var new_resume = 0;
    lateinit var uri_resume:Uri
    var uploading = false
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
    "","")
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var millis:Long = System.currentTimeMillis()/1000
    var loading = true
    lateinit var imageUri : Uri
    lateinit var AnswersArray:ArrayList<String>
    lateinit var QuestionAnswerAdapter:QuestionAnswerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_account, container, false)

        fragment.refresh_user_account.setOnRefreshListener{
            getPosts()
        }
        if(activity!!.intent.getSerializableExtra("userInfo") != null){
            userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
            showUserInfo(fragment)
            fragment.pBar.visibility=View.GONE
            fragment.refresh_user_account.visibility = View.VISIBLE
        }
        else{
            // call Api for userInfo
            fragment.pBar.visibility = View.GONE
            fragment.error.visibility = View.VISIBLE
//            TODO("Call api for user info")
        }

        return fragment
    }

    private fun showUserInfo(fragment: View?) {
        val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val id = sp.getString("id","")
        fragment!!.username_heading.text = userInfo.username
        fragment!!.userinfo_name.text = userInfo.name
        fragment!!.info_username2.text = userInfo.username
        fragment!!.userinfo_bio.text = userInfo.bio
        if(userInfo.resume.isNotEmpty()) fragment!!.resume.visibility = View.VISIBLE
        fragment!!.resume.setOnClickListener {
            val intent = Intent(context!!,WebView::class.java)
            intent.putExtra("url",userInfo.resume)
            startActivity(intent)
        }

        if(userInfo.website.isNullOrBlank()){
            fragment.userinfo_website.visibility = View.GONE
        }else{
            if(userInfo.website.indexOf("https")!=-1) fragment!!.userinfo_website.text = "${userInfo.website.substringAfter("https://","chores.com/").substringBefore("/")}"
            else fragment!!.userinfo_website.text = "${userInfo.website.substringAfter("http://","chores.com/").substringBefore("/")}"
            fragment.userinfo_website.setOnClickListener {
                var url = userInfo.website
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }

        Glide.with(fragment).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(fragment.info_userImage)
        if(userInfo.user_id == id){
            fragment!!.info_userimage_button.visibility = View.VISIBLE
            fragment.info_userimage_button.setOnClickListener{
                launchImageCropper()
            }
            fragment.edit_profile.visibility = View.VISIBLE
            fragment.edit_profile.setOnClickListener {
                val intent = Intent(activity,UserInfoActivity::class.java)
                intent.putExtra("userInfo",userInfo)
                startActivityForResult(intent,19029)
            }
        }
        fragment!!.userinfo_pincodes.setOnClickListener {
         OpenPincodesDialogBox()
        }
        var recyclerView :RecyclerView = fragment.findViewById(R.id.user_posts)
        postsAdapter = postAdapter(
            postList,
            this,
            userInfo,
            false
        )
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
                    refresh_user_account.isRefreshing=true
                    pBarPost.visibility = View.VISIBLE
                }
            }
        }
        if(postList.size==0){
            getPosts()
            fragment.pBarPost.visibility = View.VISIBLE
            fragment.refresh_user_account.isRefreshing = true
        }
    }

    override fun showTags(position: Int) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode!= RESULT_CANCELED){
            Log.i("message","hiijaidjskdalsadlkmsdmkasd $requestCode")
            when(requestCode){
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE->{
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i("message", "$result")
                        Log.i("message", " hiiiii $result")
                        Glide.with(this).load(result.uri).placeholder(R.drawable.account_border).into(info_userImage)
//                        info_userImage.setImageURI(result.uri)
                        imageUri =  result.uri
                        val pb = ProgressDialog(context)
                        pb.setTitle("Saving data")
                        pb.show()
                        if(this::imageUri.isInitialized){
                            imageUploader(pb)
                        }
                    } else {
                        Toast.makeText(context,"Unable to place image try again!",Toast.LENGTH_LONG).show()
                    }
                }
                151->{
                    if(data!!.getSerializableExtra("postData")!=null){
                    val post = data!!.getSerializableExtra("postData")as postData
                    Log.i("message hii","${post}")
                    val position = data!!.getIntExtra("position",0)
                    postList[position] = post
                    postsAdapter.notifyDataSetChanged()
                    }
                }
                19029->{
                    if(data!!.getSerializableExtra("userInfo")!=null){
                        val user = data!!.getSerializableExtra("userInfo") as UserInfoResponse
                        updateUserData(user)
                        userInfo = user
                        userInfoInterface.changeUserInfo(user)
                    }
                }
                190->{
                    uri_resume = data!!.data!!
                    new_resume=1
                }

            }
        }
    }

    private fun updateUserData(user:UserInfoResponse) {
        userinfo_bio.text = user.bio

        if(user.website.indexOf("https")!=-1) userinfo_website.text = "${user.website.substringAfter("https://","chores.com/").substringBefore("/")}"
        else userinfo_website.text = "${user.website.substringAfter("http://","chores.com/").substringBefore("/")}"

        userinfo_name.text = user.name
        info_username2.text = user.username
        username_heading.text = user.username

        edit_profile.setOnClickListener {
            val intent = Intent(activity,UserInfoActivity::class.java)
            intent.putExtra("userInfo",user)
            startActivityForResult(intent,19029)
        }
    }

    private fun imageUploader( pb : ProgressDialog) {
        val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val id = sp.getString("id","")
        val cr = activity!!.getContentResolver()
        val data = imageProcessor().imageProcessor(imageUri,cr,15)
        imageProcessor().imageUploader("",id!!,"$id/dp",data,pb,::uploader)
    }

    private fun uploader(postUUID: String, id: String, uri: String, pb: ProgressDialog){
        userInfo.profile_pic = uri
        Log.i("message","$uri")
        updateFunction()
    }

    private fun launchImageCropper() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(250,250)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(getContext()!!,this)
    }


    private fun OpenPincodesDialogBox() {
        val AlertDialog = AlertDialog.Builder(context).create();
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

    private fun updateFunction() {
        val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token =  sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val retrofitData = retrofitBuilder.putUserInfo("$token id $id", userInfo)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(context,"Unable to perform the action",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                Log.i("message","${response.code()}")
                if(response.code()==401){
                    unauthorized()
                }
                else if(response.code()==500){
                    Toast.makeText(context,"Unable to save! :(",Toast.LENGTH_LONG).show()
                }
                else{
//                    userInfoInterface.changeUserInfo(userInfo)
                }
            }
        })
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
                if(response.code() == 500){

                }
                else if(response.code()==401){

                }
                else{
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
                    pBarPost.visibility = View.GONE
                }
                }
        })
    }

    override fun showMenu(position: Int,post_more:View) {
        val popupMenu = PopupMenu(context!!,post_more)
        popupMenu.menuInflater.inflate(R.menu.post_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.edit_post->{
                    Toast.makeText(context!!,"edit",Toast.LENGTH_LONG).show()
                }
                R.id.delete->{
                    Toast.makeText(context!!,"edit",Toast.LENGTH_LONG).show()
                }
                R.id.applied_by->{
                    val intent = Intent(context!!,AppliedByList::class.java)
                    intent.putExtra("post_id",postList[position].post_id)
                    intent.putExtra("postInfo",postList[position])
                    startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()

    }

    override fun userNameClick(position: Int) {
        val intent = Intent(activity!!, AccountDetails::class.java)
        intent.putExtra("userId",postList[position].user_id)
        intent.putExtra("selfInfo",userInfo)
        startActivity(intent)
    }

    override fun postClick(position: Int) {
        val intent  = Intent(activity,Post_full_Screen::class.java)
        intent.putExtra("userInfo",userInfo)
        intent.putExtra("postInfo",postList[position])
        intent.putExtra("position",position)
        Log.i("message","${postList[position]}")
        startActivityForResult(intent,151)
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
        CommentRelated().AddComment(context!!,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun comment(position: Int, username: TextView) {
        username.text = userInfo.username
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
            val intent =Intent(context!!,WebView::class.java)
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

    override fun applyOnPost(position: Int) {
        val layout: View = activity!!.findViewById(R.id.bottom_sheet_apply)
        val bottomSheetBehavior = BottomSheetBehavior.from(layout)
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
        PostRelated().applyOnPost(context!!,postList[position],userInfo,this,layout,bottomSheetBehavior,postsAdapter,::questionsView,::sendAnswers,::choose_resume,::getResume,::resumeChoosed,::set_uploading)
    }

    private fun sendAnswers():ArrayList<String>{
        return AnswersArray
    }

    private fun questionsView(layout: View,post:postData) {
        val recyclerView = layout.questionsAnswer_recycler_view!!
        if(post.status =="false"){
            QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,true,this,ArrayList<String>())
            if(this::AnswersArray.isInitialized) AnswersArray.clear()
            else AnswersArray = ArrayList<String>()
            for(i in 0..(post.questions.size-1)) AnswersArray.add("")
            recyclerView.adapter = QuestionAnswerAdapter
            val layoutManager = LinearLayoutManager(this.context)
            recyclerView.layoutManager = layoutManager
            recyclerView.isNestedScrollingEnabled = false
        }else{
            layout.pBar_answers.visibility = View.VISIBLE
            layout.questionsAnswer_recycler_view.visibility =View.GONE
            val sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
            val token = sp.getString("token","")!!
            val id = sp.getString("id","")!!
            val retrofitData = retrofitBuilder.getAnswers("${token} id ${id}",post.post_id)
            retrofitData.enqueue(object : Callback<ArrayList<String>?> {
                override fun onFailure(call: Call<ArrayList<String>?>, t: Throwable) {
                    Toast.makeText(context!!,"Internet Connection Required!",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ArrayList<String>?>,
                    response: Response<ArrayList<String>?>
                ) {
                    if(response.isSuccessful){
                        QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,false,this@AccountFragment,response.body()!!)
                        layout.pBar_answers.visibility = View.GONE
                        layout.questionsAnswer_recycler_view.visibility =View.VISIBLE
                        recyclerView.adapter = QuestionAnswerAdapter
                        val layoutManager = LinearLayoutManager(this@AccountFragment.context)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.isNestedScrollingEnabled = false
                    }else{
                        if(response.code() == 401) unauthorized()
                        else Toast.makeText(context!!,"Error! Try Again Later",Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
    
    override fun changeAnswer(position: Int, answer: String) {
        AnswersArray[position] = answer
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