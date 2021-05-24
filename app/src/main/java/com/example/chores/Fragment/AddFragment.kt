package com.example.chores.Fragment

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat.animate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.AddPostJson
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.LoginActivity
import com.example.chores.R
import com.example.chores.utils.ClickListeners.questionsClickListener
import com.example.chores.utils.Adapters.QuestionAdapter
import com.example.chores.utils.imageProcessor
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.add_requirements_layout.*
import kotlinx.android.synthetic.main.add_requirements_layout.view.*
import kotlinx.android.synthetic.main.dialog_location_picker.view.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import java.util.EnumSet.range
import kotlin.collections.ArrayList


class AddFragment: Fragment(),View.OnClickListener,questionsClickListener{
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
    "",
    "",
    "","")
    var change = -1
    lateinit var questionAdapter : QuestionAdapter
    var selected_location = 0
    var questionList = ArrayList<String>()
    lateinit var recyclerView  :RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val yo = inflater!!.inflate(R.layout.fragment_add,container,false)
        yo.addImage.setOnClickListener (this)
        yo.pincode.setOnClickListener(this)
        yo.global.setOnClickListener (this)

        bottomSheet(yo)

        Handler().postDelayed({
            yo.main_scroll_post.smoothScrollTo(0,yo.post_layout.getTop())
        }, 0)


        if(activity!!.intent.getSerializableExtra("userInfo") != null)
        userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse

        yo.username.setText(userInfo.username)
        yo.username2.setText(userInfo.username)

        Glide.with(context!!).load(userInfo.profile_pic).placeholder(R.drawable.account_border).into(yo.userImage)
        definigUser()

        return yo
    }

    private fun bottomSheet(yo:View) {
        val layout = yo.add_requirement!!
        var k=0
        val bottomSheetBehavior = BottomSheetBehavior.from(layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
            }
            override fun onSlide(view: View, v: Float) {
            }
        })
        yo.upload_post.setOnClickListener { uploadPost() }
        yo.remove_requirements.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
         recyclerView= yo.findViewById(R.id.requirements_questions)
        questionAdapter = QuestionAdapter(questionList, this)
        recyclerView.adapter = questionAdapter
        val layoutInflater = LinearLayoutManager(context!!)
        recyclerView.layoutManager = layoutInflater
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setItemViewCacheSize(questionList.size)
        yo.addQuestion.setOnClickListener {
            if(yo.QuestionText.text.length>0) addQuestion()
            else Toast.makeText(context,"Empty Question can not be added",Toast.LENGTH_LONG).show()
        }

        yo.post.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            k=1
        }
    }

    private fun addQuestion() {
        if(change!=-1){
            questionList[change] = QuestionText.text.toString().trim()
            change=-1
        }else{
            val k = QuestionText.text.toString().trim()
            questionList.add(k)
        }
        QuestionText.setText("")
        questionAdapter.notifyDataSetChanged()
    }

    override fun questionEdit(position: Int,edit_question:View) {
        if(change!=-1){
            Toast.makeText(context,"Add the question you are editing first!",Toast.LENGTH_LONG).show()
        }else{
            change = position
            animate(edit_question).setDuration(500).alpha(0.4f)
            edit_question.setOnClickListener{ }
            QuestionText.setText(questionList[position])
        }
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
        Log.i("message error","rooooroerkoekro /${requestCode}")
        if(resultCode != RESULT_CANCELED){
            when(requestCode){
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                    val result = CropImage.getActivityResult(data)
                    Log.i("message error","rooooroerkoekro /${result.error}")
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
        }else{
            Toast.makeText(context,"No image selected",Toast.LENGTH_LONG)
        }

    }

    private fun uploadPost(){
        var l = tags.text.trim().split(Regex("\\s+"))
        for (i in 0..l.size-1){
            if(l[i].length >12){
                Toast.makeText(context!!,"A tag cannot have length greater than 12",Toast.LENGTH_LONG).show()
                return
            }
        }
        if(!this::imageUri.isInitialized){
            Toast.makeText(context,"Please add an image for the chore",Toast.LENGTH_LONG).show()
        }else if(selected_location==0 && !this::up.isInitialized &&!this::pin.isInitialized){
            Toast.makeText(context,"Please add a location for your chore",Toast.LENGTH_LONG).show()
        }else if(info.text.toString().length==0){
            Toast.makeText(context,"Please add some information about your chore",Toast.LENGTH_LONG).show()
        }else if(price_tag.text.toString().length==0){
            Toast.makeText(context,"Please add a price tag for your chore",Toast.LENGTH_LONG).show()
        }else if(l.size >5){
            Toast.makeText(context!!,"Could not have tags more than 5!",Toast.LENGTH_LONG).show()
        }else{
            val cr: ContentResolver = getActivity()!!.getContentResolver()
            val data = imageProcessor().imageProcessor(imageUri,cr,10)
            val postUUID = UUID.randomUUID().toString()
            val sharedPreferences = activity!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
            val id = sharedPreferences.getString("id","")
            val pb = ProgressDialog(context)
            pb.setTitle("Uploading post")
            pb.show()
            pb.setCanceledOnTouchOutside(false)
            val uri = uploadImage(data,postUUID,id!!,pb)
        }
    }

    private fun uploader(postUUID: String,id:String,uri: String,pb: ProgressDialog){
        val calendar = Calendar.getInstance()
        val date = DateFormat.getDateInstance().format(calendar.time)
        val millis:Long = System.currentTimeMillis()
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val resume:SwitchCompat = view!!.findViewById(R.id.resume)
        val k = tags.text.trim().split(Regex("\\s+"))

        val AddPostJson = AddPostJson(
            postUUID,
            id!!,
            if(selected_location == 0) pin else "",
            exact_location.text.toString(),
            if(selected_location == 0) up else "",
            userInfo.username,
            info.text.toString(),
            0,
            0,
            uri,
            price_tag.text.toString(),
            userInfo.profile_pic,
            date,
            millis,
            resume.isChecked,
            questionList,
            if (k.size>=1) k[0] else "null",
            if (k.size>=2) k[1] else "null",
            if (k.size>=3) k[2] else "null",
            if (k.size>=4) k[3] else "null",
            if (k.size==5) k[4] else "null"
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
                Log.i("message","${response.body()}dsf")
                if(response.isSuccessful) Toast.makeText(context,"Post Uploaded",Toast.LENGTH_LONG).show()
                else{
                    if(response.code()==401)  unauthorized()
                    else Toast.makeText(context,"Error! Try Again Later",Toast.LENGTH_LONG).show()

                }
            }
        })
    }

    private fun unauthorized() {
        val sharedPref: SharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  =Intent(activity, LoginActivity::class.java)
        startActivity(intent)
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

    @RequiresApi(21)
    private fun launchFindPlace(){
        val AlertDialog = AlertDialog.Builder(context).create();
        val layoutInflater = this.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_location_picker, null)
        dialogView.decline.setOnClickListener{
            AlertDialog.dismiss()
        }
        if(selected_location == 0){
            selected_location = 0
            dialogView.input.setTextColor(getResources().getColor(R.color.colorSuccess))
            dialogView.earth.visibility = View.VISIBLE
            dialogView.earth_selected.visibility =View.GONE
            dialogView.input_location.visibility =View.VISIBLE
        }else{
            selected_location = 1
            dialogView.earth.visibility = View.GONE
            dialogView.input.setTextColor(getResources().getColor(R.color.colorWhite))
            dialogView.earth_selected.visibility =View.VISIBLE
            dialogView.input_location.visibility =View.GONE
        }
        dialogView.earth.setOnClickListener {
            selected_location = 1
            dialogView.earth.visibility = View.GONE
            dialogView.input.setTextColor(getResources().getColor(R.color.colorWhite))
            dialogView.earth_selected.visibility =View.VISIBLE
            dialogView.input_location.visibility =View.GONE
        }
        dialogView.input.setOnClickListener {
            selected_location = 0
            dialogView.input.setTextColor(getResources().getColor(R.color.colorSuccess))
            dialogView.earth.visibility = View.VISIBLE
            dialogView.earth_selected.visibility =View.GONE
            dialogView.input_location.visibility =View.VISIBLE
        }
        dialogView.accept.setOnClickListener{
            if(selected_location ==1 ){
                global.visibility = View.VISIBLE
                pincode.visibility =View.GONE
                exact_location.setText("GLOBAL")
                AlertDialog.dismiss()
            }else{
                if(dialogView.pincodeSet.text.length>0 && dialogView.pincodeSet.text.length<16 && dialogView.address.text.length>0&& dialogView.state.text.length>0&& dialogView.city.text.length>0){
                    global.visibility =View.GONE
                    pincode.visibility = View.VISIBLE
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

        }
        AlertDialog.setView(dialogView);
        AlertDialog.show();

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
//            R.id.post -> uploadPost()
            R.id.addImage -> launchImageUri()
            R.id.pincode,R.id.global -> launchFindPlace()

        }
    }


}