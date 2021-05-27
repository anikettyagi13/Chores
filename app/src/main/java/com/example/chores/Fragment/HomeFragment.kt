package com.example.chores.Fragment

import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.stream.QMediaStoreUriLoader
import com.example.chores.*
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.notify
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.postData
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.example.chores.utils.userInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.IntStream.range


class HomeFragment: Fragment(),
    postClickListener,userInterface,questionAnswerClickListener {
    var postList = ArrayList<postData>()
    lateinit var postsAdapter : postAdapter
    lateinit var QuestionAnswerAdapter:QuestionAnswerAdapter
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    var millis:Long = System.currentTimeMillis()
    var loadMore:Boolean = true
    var new_resume =0
    var uploading = false
    lateinit var uri_resume :Uri
    lateinit var AnswersArray:ArrayList<String>
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
    var noMoreChores =false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val yo =inflater.inflate(R.layout.fragment_home,container,false)
        if(activity!!.intent.getSerializableExtra("userInfo")!=null){
            userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
        }else{
            getUser()
        }

        yo.refresh_home.setOnRefreshListener{
            getPosts()
        }

        var recyclerView :RecyclerView = yo.findViewById(R.id.home_recycler_view)
        postsAdapter = postAdapter(
            postList,
            this,
            userInfo,
            false
        )
        val layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = postsAdapter
        recyclerView.setItemViewCacheSize(5)
        recyclerView.setNestedScrollingEnabled(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // super.onScrolled(recyclerView, dx, dy);
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisiblePosition == postList.size -1) {
                    Log.i("message","scrolling")
                    if (loadMore&& !noMoreChores) {
                        loadMore = false
                        getPosts()
                        refresh_home.isRefreshing=true
                        pBar_posts.visibility = View.VISIBLE
                    }
                }
            }
        })
        if(postList.size==0 &&userInfo.name.isNotEmpty()){
            getPosts()
        }
        return yo
    }

//    override fun onBackPressed(){
//        if(uploading){
//            Toast.makeText(context!!,"Wait For The Upload!!",Toast.LENGTH_LONG).show()
//        }
//    }


    private fun getUser() {
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val sp = activity!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitData = retrofitBuilder.getUserInfo("$token id $id")
        retrofitData.enqueue(object : Callback<UserInfoResponse?> {
            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                if(t.message!!.contains("Failed to connect to",true)) {
                    Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_LONG).show()
                    error.visibility = View.VISIBLE
                    refresh_home.visibility = View.GONE
                }
            }
            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                userInfo = response.body()!!
                Log.i("message","${response.body()} hiiii")
                getPosts()
                pBar_posts.visibility = View.VISIBLE
            }
        })
    }


    private fun getPosts() {
        val sharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        lateinit var timeInfo :timeInfoAndUserId
        if(postList.size==0){
            timeInfo = timeInfoAndUserId(0,millis)
        }else{
            var k=postList[postList.size-1].time
            for( i in postList.size-1 downTo 0){
                if(millis>postList[i].time){
                    k=postList[i].time
                    break
                }
            }
            timeInfo = timeInfoAndUserId(millis,k)
            Log.i("message","${postList[(postList.size)-1].time}")
        }
        val retrofitData = retrofitBuilder.getPosts("$token id $id",timeInfo)
        retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
            override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable){
                Toast.makeText(context,"${t.message}",Toast.LENGTH_LONG).show()
                error.visibility = View.VISIBLE
                refresh_home.visibility = View.GONE
            }

            override fun onResponse(
                call: Call<ArrayList<postData>?>,
                response: Response<ArrayList<postData>?>
            ) {
                if(response.body()!!.size>0&&response.body()!![0].profile_pic.contains("Error",true)){
                    Toast.makeText(context,"ERROR:",Toast.LENGTH_LONG).show()
                }else{
                    postList.addAll(response.body()!!)
                    if(response.body()!!.size==0) noMoreChores = true
                    else
                    for(i in 0..(response.body()!!.size-1)){
                        if(millis>response.body()!![i].time){
                            break
                        }else{
                            Log.i("message","${response.body()!![i].time}")
                            millis=response.body()!![i].time
                        }
                    }
                    Log.i("message","${postList.size}")
                    loadMore = true
                    postsAdapter.notifyDataSetChanged()
                    refresh_home.isRefreshing=false
                    pBar_posts.visibility = View.GONE
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != RESULT_CANCELED){
            when(requestCode){
                151->{
                    if(data!!.getSerializableExtra("postData")!=null){
                        val post = data!!.getSerializableExtra("postData") as postData
                        val position = data!!.getIntExtra("position",0)
                        postList[position] = post
                        postsAdapter.notifyDataSetChanged()
                    }
                }
                190->{
                    uri_resume = data!!.data!!
                    val src = uri_resume.path
                    new_resume=1
                }
            }
        }
    }

    // post click listeners
    override fun userNameClick(position: Int) {
        val intent = Intent(activity!!, AccountDetails::class.java)
        intent.putExtra("userId",postList[position].user_id)
        intent.putExtra("selfInfo",userInfo)
        startActivity(intent)
    }

    override fun addCommentClick(position: Int, comment: String,comment_write:EditText,comment_view:LinearLayout) {
        CommentRelated().AddComment(context!!,postList[position],userInfo,comment,comment_write,comment_view)
    }

    override fun likeClick(position: Int) {
        PostRelated().likePost(context!!,postList[position],userInfo,this)
    }

    override fun disLikeCLick(position: Int) {
        PostRelated().disLikePost(context!!,postList[position],userInfo,this)
    }
    override fun postClick(position: Int) {
        val intent = Intent(activity,Post_full_Screen::class.java)
        intent.putExtra("userInfo",userInfo)
        intent.putExtra("postInfo",postList[position])
        intent.putExtra("position",position)
        startActivityForResult(intent,151)
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
        if(post.status=="false"){
            QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,true,this,ArrayList<String>())
            if(this::AnswersArray.isInitialized) AnswersArray.clear()
            else AnswersArray = ArrayList<String>()
            for(i in 0..(post.questions.size-1)) AnswersArray.add("")
            recyclerView.adapter = QuestionAnswerAdapter
            val layoutManager = LinearLayoutManager(this.context)
            recyclerView.layoutManager = layoutManager
            recyclerView.isNestedScrollingEnabled = false
        }
        else{
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
                        QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,false,this@HomeFragment,response.body()!!)
                        layout.pBar_answers.visibility = View.GONE
                        layout.questionsAnswer_recycler_view.visibility =View.VISIBLE
                        recyclerView.adapter = QuestionAnswerAdapter
                        val layoutManager = LinearLayoutManager(this@HomeFragment.context)
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

    override fun showMenu(position: Int, post_more: View) {
        val popupMenu = PopupMenu(context!!,post_more)
        popupMenu.menuInflater.inflate(R.menu.post_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
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
    override fun showTags(position: Int) {
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
