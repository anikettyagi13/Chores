package com.example.chores.Fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.contentValuesOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.AddPostJson
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.R
import com.example.chores.utils.imageProcessor
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.dialog_location_picker.view.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DateFormat
import java.util.*


class AddFragment: Fragment(),View.OnClickListener{
    private lateinit var imageUri:Uri
    private lateinit var pin:String
    private lateinit var up:String
    private var userInfo:UserInfoResponse = UserInfoResponse("",
    "",
    "",
        ArrayList<String>(),
    0,
    0,
    0.0,
    "",
    "")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val yo = inflater!!.inflate(R.layout.fragment_add,container,false)
        Log.i("message","hiiii")
        yo.addImage.setOnClickListener (this)
        yo.pincode.setOnClickListener(this)
        yo.post.setOnClickListener(this)

        if(activity!!.intent.getSerializableExtra("userInfo") != null)
        userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
        Log.i("me","${userInfo.profile_pic} hiiiiiiiiiiiiii")

        yo.username.setText(userInfo.username)
        yo.username2.setText(userInfo.username)

        Glide.with(context!!).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(yo.userImage)
        definigUser()

        return yo
    }

    private fun definigUser() {
//        username.setText(userInfo.username)
//        Glide.with(context!!).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(userImage)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when(requestCode){
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    Glide.with(addImage.context).load(result.uri).into(addImage)
                    imageUri = result.uri
                }else{
                    Log.i("message error","rooooroerkoekro /${result.error}")
                }
            }
            100 ->{
                if(resultCode== RESULT_OK){
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    exact_location.setText(place.getAddress())
                }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
                    val status  = Autocomplete.getStatusFromIntent(data!!)
                    Log.i("message err","${status.statusMessage}")
                    Toast.makeText(context,status.statusMessage,Toast.LENGTH_LONG).show()
                }
            }

        }

    }

    private fun uploadPost(){
        if(!this::imageUri.isInitialized){
            Toast.makeText(context,"Please add an image for the chore",Toast.LENGTH_LONG).show()
        }else if(!this::up.isInitialized &&!this::pin.isInitialized){
            Toast.makeText(context,"Please add a location for your chore",Toast.LENGTH_LONG).show()
        }else if(info.text.toString().length==0){
            Toast.makeText(context,"Please add some information about your chore",Toast.LENGTH_LONG).show()
        }else if(price_tag.text.toString().length==0){
            Toast.makeText(context,"Please add a price tag for your chore",Toast.LENGTH_LONG).show()
        }
        else{
            val cr: ContentResolver = getActivity()!!.getContentResolver()

            val data = imageProcessor().imageProcessor(imageUri,cr,10)
            val postUUID = UUID.randomUUID().toString()
            val sharedPreferences = activity!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
            val id = sharedPreferences.getString("id","")
            val pb = ProgressDialog(context)
            pb.setTitle("Uploading post")
            pb.show()
            val uri = uploadImage(data,postUUID,id!!,pb)
            Log.i("message","$uri")
        }
    }

    private fun uploader(postUUID: String,id:String,uri: String,pb: ProgressDialog){

        val calendar = Calendar.getInstance()
        val date = DateFormat.getDateInstance().format(calendar.time)
        val millis:Long = System.currentTimeMillis()/1000

        val userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
        Log.i("me","$userInfo hiiiiiiiiiiiiii")
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val AddPostJson = AddPostJson(
            postUUID,
            id!!,
            pin,
            exact_location.text.toString(),
            up,
            userInfo.username,
            info.text.toString(),
            0,
            0,
            uri,
            price_tag.text.toString(),
            userInfo.profile_pic,
            date,
            millis
        )
        val sharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)

        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        val retrofitData = retrofitBuilder.AddPost("$token id $id" ,AddPostJson)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                pb.dismiss()
                Toast.makeText(context,"Error!! Please Try Again!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>
            ) {
                pb.dismiss()
                Toast.makeText(context,"Post Uploaded",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun uploadImage(data: ByteArray,postUUID:String,id:String,pb:ProgressDialog):String {
        val pathname = "${id}/${postUUID}"
        val uri = imageProcessor().imageUploader(postUUID,id,pathname,data,pb,::uploader)
        return uri
    }

    private fun launchImageUri() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(400,250)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(getContext()!!,this)

    }

    private fun launchFindPlace(){
        val AlertDialog = AlertDialog.Builder(context).create();
        val layoutInflater = this.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_location_picker, null)
        dialogView.decline.setOnClickListener{
            AlertDialog.dismiss()
        }
        dialogView.accept.setOnClickListener{
            Log.i("message pincode","${dialogView.pincodeSet.text} jj")
            if(dialogView.pincodeSet.text.length>0 && dialogView.pincodeSet.text.length<16 && dialogView.address.text.length>0&& dialogView.state.text.length>0&& dialogView.city.text.length>0){
                pincode.setText(dialogView.pincodeSet.text.toString())
                pin = dialogView.pincodeSet.text.toString()
                up = dialogView.state.text.toString()
                exact_location.setText("${dialogView.address.text}, ${dialogView.city.text}, ${dialogView.state.text}, ${dialogView.pincodeSet.text}")
                AlertDialog.dismiss()
            }else{
                if(dialogView.pincodeSet.text.length>16){
                    dialogView.error.setText("Pincode length cannot exceed 16 characters")
                }else{
                    dialogView.error.setText("Every field is required*")
                }
            }
        }
        AlertDialog.setView(dialogView);
        AlertDialog.show();

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.post -> uploadPost()
            R.id.addImage -> launchImageUri()
            R.id.pincode -> launchFindPlace()
        }
    }


}