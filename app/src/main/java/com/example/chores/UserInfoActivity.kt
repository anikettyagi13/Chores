package com.example.chores

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.chores.Api.Json.UserInfoJson
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.imageProcessor
import com.example.chores.utils.postAdapter
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


class UserInfoActivity : AppCompatActivity(),View.OnClickListener  {
    public var postList = ArrayList<postData>()
    private lateinit var postsAdapter: postAdapter
    private var pincodesArray = ArrayList<String>()
    private lateinit var imageUri: Uri

    // elements from layout
    lateinit var info_username:TextView
    lateinit var info_username2:TextView
    lateinit var no_post : TextView
    lateinit var save_userinfo:Button
    lateinit var userinfo_name:EditText
    lateinit var userinfo_pincodes:Button
    lateinit var info_userimage_button:ImageButton
    lateinit var  username:String
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


        //Click listeners
        save_userinfo.setOnClickListener (this)
        userinfo_pincodes.setOnClickListener(this)
        info_userimage_button.setOnClickListener(this)

        Log.i("username","$username ji")
        setUserNameInLayout(username!!,info_username,info_username2)
        val param = no_post.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(30,10,30,10)

        no_post.text = "No Chores Uploaded By User"
        no_post.layoutParams = param
//        var recyclerView:RecyclerView = findViewById(R.id.posts_user)
//        postsAdapter = postAdapter(postList,this)
//        val layoutManager = LinearLayoutManager(applicationContext)
//        recyclerView.layoutManager = layoutManager
//        recyclerView.adapter = postsAdapter
//        recyclerView.setItemViewCacheSize(5)
//        recyclerView.setNestedScrollingEnabled(true)
//        preparePosts()
    }

    private fun setUserNameInLayout(username:String,info_username:TextView,info_username2:TextView) {
        info_username.text = username
        info_username2.text = username
    }

    private fun preparePosts() {
        postsAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("message", "$result")
                    info_userImage.setImageURI(result.uri)
                    imageUri = result.uri
                } else {
                    Toast.makeText(this,"Unable to place image try again!",Toast.LENGTH_LONG).show()
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
                pincodesArray.add(dialogView.info_pincode1.text.toString())
                pincodesArray.add(dialogView.info_pincode2.text.toString())
                pincodesArray.add(dialogView.info_pincode3.text.toString())
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
                else{
                     pb = ProgressDialog(this)
                    pb.setTitle("Saving data")
                    pb.show()
                    val sp = getSharedPreferences("chores", Context.MODE_PRIVATE)
                    val id = sp.getString("id","")
                    if(this::imageUri.isInitialized){
                        uploadPost(pb,id!!)
                    }else{
                        uploader("",id!!,"",pb)
                    }
                }
            }
            R.id.userinfo_pincodes->{
                OpenPincodesDialogBox()
            }
            R.id.info_userimage_button->{
                launchImageUri()
            }
        }
    }

    private fun uploadPost(pb:ProgressDialog,id:String) {
        val cr = getContentResolver()
        val data = imageProcessor().imageProcessor(imageUri,cr,15)


        imageProcessor().imageUploader("",id,"$id/dp",data,pb,::uploader)
    }

    private fun uploader(postUUID: String, id: String, uri: String, pb: ProgressDialog){
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)

        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        val userInfoJson = UserInfoJson(username,id!!,uri,userinfo_name.text.toString(),pincodesArray,0,0,0.0)
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
                if(response.body()!!.error.length==0){
                    pb.dismiss()
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
                        Log.i("hi o" ,"sading ${response.body()!!.error}")
                        Toast.makeText(this@UserInfoActivity,"Error !!",Toast.LENGTH_LONG).show()

                    }
                }
            }
        })
    }
}
