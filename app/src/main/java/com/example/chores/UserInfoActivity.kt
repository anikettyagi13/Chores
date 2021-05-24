package com.example.chores

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoJson
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ResumeUtils
import com.example.chores.utils.imageProcessor
import com.example.chores.utils.postData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.dialog_location_picker.view.accept
import kotlinx.android.synthetic.main.dialog_location_picker.view.decline
import kotlinx.android.synthetic.main.pincodes_dialog_box.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class UserInfoActivity : AppCompatActivity(),View.OnClickListener  {
    public var postList = ArrayList<postData>()
    private lateinit var postsAdapter: postAdapter
    private var pincodesArray = arrayListOf("","","")
    private lateinit var imageUri: Uri
    var new_resume = 0
    var uploading = false

    // elements from layout
    lateinit var info_username:TextView
    lateinit var info_username2:EditText
    lateinit var no_post : TextView
    lateinit var save_userinfo:Button
    lateinit var userinfo_name:EditText
    lateinit var userinfo_pincodes:Button
    lateinit var info_userimage_button:Button
    lateinit var  username:String
    lateinit var userinfo_bio: EditText
    lateinit var userinfo_website: EditText
    lateinit var userInfo: UserInfoResponse
    lateinit var resume:Button
    lateinit var see_resume:ImageButton
    lateinit var uri:Uri

    lateinit var pb:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // getting username form sharedPreferences
        val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username","")!!


        // elements from layout
        info_username= findViewById(R.id.info_username)
        info_username2 = findViewById(R.id.info_username2)
        no_post= findViewById(R.id.posts_noposts)
        save_userinfo= findViewById(R.id.save_userinfo)
        userinfo_name= findViewById(R.id.userinfo_name)
        userinfo_pincodes= findViewById(R.id.userinfo_pincodes)
        info_userimage_button = findViewById(R.id.info_userimage_button)
        userinfo_bio = findViewById(R.id.userinfo_bio)
        userinfo_website = findViewById(R.id.userinfo_website)
        resume = findViewById(R.id.userinfo_resume)
        see_resume = findViewById(R.id.see_resume)
        val back : ImageButton = findViewById(R.id.back)


        setUserNameInLayout(username!!,info_username,info_username2)

        if(this.intent.getSerializableExtra("userInfo")!=null){
            back.visibility = View.VISIBLE
            userInfo = this.intent.getSerializableExtra("userInfo") as UserInfoResponse
            actAsAEditScreen(userInfo)
            back.setOnClickListener {
                onBackPressed()
            }
        }

        //Click listeners
        save_userinfo.setOnClickListener (this)
        userinfo_pincodes.setOnClickListener(this)
        info_userimage_button.setOnClickListener(this)
        resume.setOnClickListener(this)
        see_resume.setOnClickListener(this)

        Log.i("username","$username ji")
        val param = no_post.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(30,10,30,10)
        no_post.layoutParams = param

        val mTextEditorWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if(s.length<=40){
                        bioCount.setText("${s.length}/40")
                    }else{
                        return
                    }
                }
                override fun afterTextChanged(s: Editable) {}
            }

        val nameLengthChecker: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.length<=20){
                    nameCount.setText("${s.length}/20")
                }else{
                    return
                }
            }
            override fun afterTextChanged(s: Editable) {}
        }
        val usernameLengthChecker: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.length<=20){
                    usernameCount.setText("${s.length}/20")
                }else{
                    return
                }
            }
            override fun afterTextChanged(s: Editable) {}
        }

        info_username2.addTextChangedListener(usernameLengthChecker);
        userinfo_name.addTextChangedListener(nameLengthChecker);
        userinfo_bio.addTextChangedListener(mTextEditorWatcher);

    }

    override fun onBackPressed() {
        if(uploading){
            Toast.makeText(applicationContext,"Uploading!! Please wait",Toast.LENGTH_LONG).show()
        }else{
            finish()
        }
    }

    private fun actAsAEditScreen(userInfo: UserInfoResponse){
        info_username.text = userInfo.username
        info_username2.setText("${userInfo.username}")
        pincodesArray = userInfo.pincodes
        userinfo_name.setText("${userInfo.name}")
        userinfo_bio.setText("${userInfo.bio}")
        Glide.with(this).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(findViewById(R.id.info_userImage))
        userinfo_website.setText("${userInfo.website}")
        bioCount.setText("${userInfo.bio.length}/40")
        nameCount.setText("${userInfo.name.length}/20")
        usernameCount.setText("${userInfo.username.length}/20")
        if(userInfo.resume.isNotEmpty()) see_resume.visibility = View.VISIBLE
    }

    private fun setUserNameInLayout(username:String,info_username:TextView,info_username2:TextView) {
        info_username.text = username
        info_username2.text = username
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_CANCELED){
            when(requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i("message", "$result")
                        Glide.with(this).load(result.uri).placeholder(R.drawable.account_border).into(findViewById(R.id.info_userImage))
//                        info_userImage.setImageURI(result.uri)
                        imageUri = result.uri
                    } else {
                        Toast.makeText(this,"Unable to place image try again!",Toast.LENGTH_LONG).show()
                    }
                }
                190->{
                    uri = data!!.data!!
                    val src = uri.path
                    new_resume=1
                    see_resume.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun launchImageUri() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(250,250)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }
    private fun OpenPincodesDialogBox() {
        val AlertDialog = AlertDialog.Builder(this).create();
        val layoutInflater = this.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.pincodes_dialog_box, null)
        dialogView.decline.setOnClickListener{
            AlertDialog.dismiss()
        }
        if(pincodesArray.size>0){
            for( i in (0 .. pincodesArray.size-1)){
                if(i==0) dialogView.info_pincode1.setText(pincodesArray[i])
                if(i==1) dialogView.info_pincode2.setText(pincodesArray[i])
                if(i==2) dialogView.info_pincode3.setText(pincodesArray[i])
            }
        }
        dialogView.accept.setOnClickListener{
            if(dialogView.info_pincode1.text.length>=3 && dialogView.info_pincode1.text.length<=16 && dialogView.info_pincode2.text.length>=3 && dialogView.info_pincode2.text.length<=16 && dialogView.info_pincode3.text.length>=3 && dialogView.info_pincode3.text.length<=16){
                pincodesArray[0]=dialogView.info_pincode1.text.toString()
                pincodesArray[1]=dialogView.info_pincode2.text.toString()
                pincodesArray[2]=dialogView.info_pincode3.text.toString()
                AlertDialog.dismiss()
            }else{
                if(dialogView.info_pincode1.text.length<3 || dialogView.info_pincode1.text.length>16 || dialogView.info_pincode2.text.length<3 && dialogView.info_pincode2.text.length>16 || dialogView.info_pincode3.text.length<3 || dialogView.info_pincode3.text.length>16){
                    dialogView.info_error.setText("Pincodes are allowed to be of length between 3 to 16")
                }else{
                    dialogView.info_error.setText("Every field is required*")
                }
            }
        }
        AlertDialog.setView(dialogView);
        AlertDialog.show();
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.save_userinfo ->{
                if (userinfo_name.text.length < 3 || userinfo_name.text.length > 20) {
                    no_post.text =
                        "Name is a required field and should have characters between 3 to 20."
                    userinfo_name.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
//                userinfo_name.setSupportButtonTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
                } else if (pincodesArray.size < 3 || pincodesArray.size > 5) {
                    no_post.text = "Every user is required to have atleast 3 pincodes"
                    userinfo_pincodes.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
                }
                else if(userinfo_bio.text.isEmpty()){
                    no_post.text = "Bio is a required Field"
                    userinfo_name.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
                }
                else if(info_username2.text.length<3 || info_username2.text.length>20){
                    no_post.text =
                        "Username is a required field and should have characters between 3 to 20."
                    info_username2.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
                }
                else{
                    pb = ProgressDialog(this)
                    uploading =true
                    pb.setTitle("Saving data")
                    pb.setCanceledOnTouchOutside(false)
                    pb.show()
                    val sp = getSharedPreferences("chores", Context.MODE_PRIVATE)
                    val id = sp.getString("id","")
                    if(this::imageUri.isInitialized){
                        uploadPost(pb,id!!)
                    }else{
                        if(this::userInfo.isInitialized){
                            resumeUploader("",id!!,userInfo.profile_pic,pb)
                        }else{
                            resumeUploader("",id!!,"",pb)
                        }
                    }
                }
            }
            R.id.userinfo_pincodes->{
                OpenPincodesDialogBox()
            }
            R.id.info_userimage_button->{
                launchImageUri()
            }
            R.id.userinfo_resume->{
                launchResumeSelector()
            }
            R.id.see_resume->{
                seeResume()
            }
        }
    }

    private fun seeResume(){
        if(new_resume==1){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setData(uri)
            startActivity(intent)
            if (intent.resolveActivityInfo(packageManager, 0) != null) {
                startActivity(intent)
            } else {
                Log.i("message","unable to load")
            }
        }else{
            val intent =Intent(this,WebView::class.java)
            intent.putExtra("url",userInfo.resume)
            startActivity(intent)
        }
    }

    private fun launchResumeSelector(){
        var intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("application/pdf")

        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, 190);
    }

    private fun uploadPost(pb:ProgressDialog,id:String) {
        val cr = getContentResolver()
        val data = imageProcessor().imageProcessor(imageUri,cr,15)
        if(this::userInfo.isInitialized){
            imageProcessor().imageUploader("",id,"$id/dp",data,pb,::resumeUploader)
        }else{
            imageProcessor().imageUploader("",id,"$id/dp",data,pb,::resumeUploader)
        }
    }

    public fun resumeUploader(postUUID: String,id: String,downloadUrl: String,pb:ProgressDialog){
        if(new_resume ==1){
            val cr =contentResolver
            val sp = getSharedPreferences("chores",Context.MODE_PRIVATE)
            val id = sp.getString("id","")
            if(this::userInfo.isInitialized){
                ResumeUtils().uploadResume("$id/resume",uri,cr,pb,downloadUrl,::uploader2)
            }else{
                ResumeUtils().uploadResume("$id/resume",uri,cr,pb,downloadUrl,::uploader)
            }
        }else{
            if(this::userInfo.isInitialized){
                uploader2("",pb,downloadUrl)
            }else{
                uploader("",pb,downloadUrl)
            }
        }
    }

    private fun uploader2(resume: String,pb:ProgressDialog,profile_pic:String){
        userInfo.name = userinfo_name.text.toString()
        userInfo.website = userinfo_website.text.toString()
        userInfo.bio = userinfo_bio.text.toString()
        userInfo.username = info_username2.text.toString()
        userInfo.pincodes = pincodesArray
        Log.i("message","$resume  resume")
        if(profile_pic.isNotEmpty()){
            userInfo.profile_pic = profile_pic
        }
        if(resume.isNotEmpty()) userInfo.resume = resume

        val sp = this.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token =  sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val retrofitData = retrofitBuilder.putUserInfo("$token id $id", userInfo)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                pb.dismiss()
                Toast.makeText(applicationContext,"Unable to perform the action",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                Log.i("message","${response.code()}")
                if(response.code()==401){
                    unauthorized()
                }
                else if(response.code()==500){
//                    Toast.makeText(this,"Unable to save! :(",Toast.LENGTH_LONG).show()
                }
                else{
                    onCloseActivity()
                }
                uploading = false
                pb.dismiss()
            }
        })
    }

    private fun onCloseActivity() {
        val intent = Intent()
        intent.putExtra("userInfo",userInfo)
        setResult(123,intent)
        finish()
    }

    fun unauthorized() {
        val sharedPref: SharedPreferences = this.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  =Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }

    private fun uploader(uri: String, pb: ProgressDialog,profile_pic:String){
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)

        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        val userInfoJson = UserInfoJson(info_username2.text.toString(),id!!,profile_pic,userinfo_name.text.toString(),pincodesArray,0,0,0.0,userinfo_bio.text.toString(),userinfo_website.text.toString(),uri)
        val retrofitData = retrofitBuilder.userInfo("$token id $id",userInfoJson)

        retrofitData.enqueue(object : Callback<UserInfoResponse?> {
            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                pb.dismiss()
                Log.i("hi o" ,"sading ${t.message}")
                Toast.makeText(this@UserInfoActivity,"Unable to save the data!!! TRY AGAIN!",Toast.LENGTH_LONG).show()
            }
            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                if(response.code() == 500){
                    Toast.makeText(this@UserInfoActivity,"ERROR",Toast.LENGTH_LONG).show()
                    uploading = false
                }
                else if(response.body()!!.error.length==0){
                    pb.dismiss()
                    uploading = false
                    Toast.makeText(this@UserInfoActivity,"Saved",Toast.LENGTH_LONG).show()
                    val intent = Intent(this@UserInfoActivity,Home::class.java)
                    val body = response.body()!!
                    intent.putExtra("userInfo_name",body.name)
                    intent.putExtra("userInfo_username",body.username)
                    intent.putExtra("userInfo_pincodes",body.pincodes)
                    intent.putExtra("userInfo_profile_pic",body.profile_pic)
                    startActivity(intent)
                }
                else{
                    if(response.body()!!.error == "unauthorized" || response.body()!!.error == "Cannot Retrieve user"){
                        Toast.makeText(this@UserInfoActivity,"Login Again, please!",Toast.LENGTH_LONG).show()
                        val sharedPref: SharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("id", "")
                        editor.putString("token", "")
                        editor.putString("username","")
                        val intent = Intent(this@UserInfoActivity,LoginActivity::class.java)
                        startActivity(intent)
                    }else{
                        pb.dismiss()
                        uploading = false
                        Log.i("hi o" ,"sading ${response.body()!!.error}")
                        Toast.makeText(this@UserInfoActivity,"Error !!",Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
