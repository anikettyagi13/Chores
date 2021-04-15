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
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.dialog_location_picker.view.accept
import kotlinx.android.synthetic.main.dialog_location_picker.view.decline
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AccountFragment(val userInfoInterface: userInfoInterface): Fragment(),
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
    "",
    "",
    "")
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var millis:Long = System.currentTimeMillis()/1000
    var loading = true
    lateinit var imageUri : Uri
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
        Log.i("message","etxt ${userInfo.website.isNullOrBlank()}")
        if(userInfo.website.isNullOrBlank()){
            fragment.userinfo_website.visibility = View.GONE
        }else{
            if(userInfo.website.indexOf("https")!=-1) fragment!!.userinfo_website.text = "${userInfo.website.substringAfter("https://","chores.com/").substringBefore("/")}"
            else fragment!!.userinfo_website.text = "${userInfo.website.substringAfter("http://","chores.com/").substringBefore("/")}"
            fragment.userinfo_website.setOnClickListener {
                Log.i("message","open")
                var url = userInfo.website
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                Log.i("message","${userInfo.website}")
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
                    if(data!!.getParcelableExtra<postData>("postData")!=null){
                    val post = data!!.getParcelableExtra<postData>("postData")!!
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
                    Log.i("message","HEYYYYIIIII")
                    Toast.makeText(context,"Unable to save! :(",Toast.LENGTH_LONG).show()
                }
                else{
                    Log.i("message","HEYYYY")
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

    override fun userNameClick(position: Int) {
        val intent = Intent(activity!!, AccountDetails::class.java)
        intent.putExtra("userId",postList[position].user_id)
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