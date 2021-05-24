package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.Api.Json.*
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.ClickListeners.appliedClickListener
import com.example.chores.utils.Adapters.UsersAdapter
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.notify
import com.example.chores.utils.postData
import com.example.chores.utils.userInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_applied_by_list.*
import kotlinx.android.synthetic.main.activity_applied_by_list.view.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.apply_on_post.view.pBar_answers
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppliedByList : AppCompatActivity(),appliedClickListener,userInterface,questionAnswerClickListener {
    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
    lateinit var userAdapter : UsersAdapter
    var usersList = ArrayList<UserInfoAppliedList>()
    lateinit var post_id :String
    lateinit var postInfo:postData
    var requirement_open = false
    lateinit var bottomSheetBehavior :BottomSheetBehavior<View>
    val millis = System.currentTimeMillis()
    var userIdList = ArrayList<applyResponse>()
    lateinit var QuestionAnswerAdapter:QuestionAnswerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applied_by_list)

        if(this.intent.getStringExtra("post_id") != null){
            post_id = this.intent.getStringExtra("post_id")!!
            postInfo = this.intent.getSerializableExtra("postInfo") as postData
            showInfo()
        }else{
            TODO("when post_id is not passed")
        }

        applied_user_refresh.setOnRefreshListener {
            applied_user_refresh.isRefreshing = true
            loading2.visibility = View.VISIBLE
            getUserList()
        }

        back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        Log.i("message","$requirement_open requirement_open")
        if(!requirement_open)  finish()
        else{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showInfo() {
        val recyclerView :RecyclerView = findViewById(R.id.userList_recycler_view)
        userAdapter =
            UsersAdapter(usersList, this)
        recyclerView.adapter = userAdapter
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.setItemViewCacheSize(5)
        recyclerView.isNestedScrollingEnabled =true
        getUserList()
    }

    fun getUserList(){
        lateinit var time :timeInfoAndUserId
        if( usersList.size>0 ) time = timeInfoAndUserId(millis,userIdList[(userIdList.size)-1].time)
        else time = timeInfoAndUserId(0,millis)

        val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val id = sp.getString("id","")
        val token = sp.getString("token","")


        val retrofitData = retrofitBuilder.getAppliedList("$token id $id",post_id,time)
        retrofitData.enqueue(object : Callback<ArrayList<applyResponse>?> {
            override fun onFailure(call: Call<ArrayList<applyResponse>?>, t: Throwable) {
                Toast.makeText(applicationContext,"Internet Required!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<applyResponse>?>,
                response: Response<ArrayList<applyResponse>?>
            ) {
                if(response.isSuccessful){

                    val users = ArrayList<Deferred<Response<UserInfoResponse>>>()
                    val retrofitBuilder = RetrofitBuilder().retrofitBuilder()


                    CoroutineScope(IO).launch{
                         response.body()!!.map{
                             val retrofitData = retrofitBuilder.getUserInfoById(it.user_id)
                             users.add( async { retrofitData.execute() })
                             userIdList.add(it)
                         }
                        showUserList(users.awaitAll(),response.body()!!)
                    }

                }else{
                    applied_user_refresh.isRefreshing = false;
                    applied_user_refresh.visibility = View.VISIBLE
                    loading2.visibility = View.GONE
                    loading.visibility = View.GONE
                    Toast.makeText(applicationContext,"Error! Please try again later!!",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private suspend fun showUserList(users: List<Response<UserInfoResponse>>,new_users:ArrayList<applyResponse>) {
        withContext(Main){
            var k=0;
            new_users.map{
                if(users[k].isSuccessful){
                    val o = users[k].body()!!
                    val k = UserInfoAppliedList(o.name,o.profile_pic,o.username,o.pincodes,o.jobs_created,o.jobs_completed,o.ratings,o.user_id,o.bio,o.website,it.status,it.time)
                    usersList.add(k)
                }
                k++;
            }
            applied_user_refresh.visibility = View.VISIBLE
            applied_user_refresh.isRefreshing = false;
            loading2.visibility = View.GONE
            loading.visibility = View.GONE

            userAdapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun assignClick(position:Int,assign: View,user:View,excess_button:View,chat:View,date2:View,date:View) {

        val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val assignJ = assignJson(post_id,usersList[position].user_id)

        val retrofitData = retrofitBuilder.assign("$token id $id",assignJ)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(applicationContext,"Internet Connection Required!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.isSuccessful){
                    assign.visibility= View.GONE
                    excess_button.visibility= View.GONE
                    user.setBackgroundTintList(getResources().getColorStateList(R.color.colorSuccess))
                    chat.setBackgroundTintList(getResources().getColorStateList(R.color.colorSuccess))
                    chat.visibility = View.VISIBLE
                    date2.visibility = View.VISIBLE
                    date.visibility = View.GONE
                    notify().notifyUser(applicationContext,usersList[position].user_id,post_id,id!!,"assign",System.currentTimeMillis(),"",postInfo.url,this@AppliedByList)
                }else{
                    if(response.code() == 401) unauthorized()
                    else Toast.makeText(applicationContext,"Error! Try again later!",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun rejectClick(position: Int, assign: View, user: View, excess_button: View,chat:View,date2:View,date:View) {
        val sp = applicationContext.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val assignJ = assignJson(post_id,usersList[position].user_id)

        val retrofitData = retrofitBuilder.reject("$token id $id",assignJ)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(applicationContext,"Internet Connection Required!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.isSuccessful){
//                    animate(assign).alpha(0f).setDuration(500)
                    excess_button.visibility= View.GONE
                    assign.visibility= View.GONE
                    chat.visibility = View.VISIBLE
                    date2.visibility = View.VISIBLE
                    date.visibility = View.GONE
                    user.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
                    chat.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
                }else{
                    if(response.code() == 401) unauthorized()
                    else Toast.makeText(applicationContext,"Error! Try again later!",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun assigned(assign: View, user: View, excess_buttons: View,chat:View,date2:View,date:View) {
        assign.visibility = View.GONE
        excess_buttons.visibility= View.GONE
        date2.visibility = View.VISIBLE
        date.visibility = View.GONE
        user.setBackgroundTintList(getResources().getColorStateList(R.color.colorSuccess))
        chat.setBackgroundTintList(getResources().getColorStateList(R.color.colorSuccess))
        chat.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun rejected(assign: View, user: View, excess_buttons: View,chat:View,date2:View,date:View) {
        assign.visibility = View.GONE
        excess_buttons.visibility= View.GONE
        date2.visibility = View.VISIBLE
        date.visibility = View.GONE
        user.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
        chat.setBackgroundTintList(getResources().getColorStateList(R.color.colorError))
        chat.visibility = View.VISIBLE
    }

    override fun showRequirements(position: Int) {
        var layout :RelativeLayout = findViewById(R.id.question_answer)
        bottomSheetBehavior = BottomSheetBehavior.from(layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        requirement_open =true
        layout.pBar_answers.visibility = View.VISIBLE
        layout.question_answer_recycler.visibility = View.GONE
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.i("message","$newState")
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    requirement_open = true
                }else{
                    requirement_open =false
                }
            }
        })
        if(postInfo.resume){
            layout.viewable_resume.visibility = View.VISIBLE
            layout.resume.visibility = View.VISIBLE
            layout.resume.setOnClickListener {
                val intent = Intent(this,WebView::class.java)
                intent.putExtra("url","https://firebasestorage.googleapis.com/v0/b/chores-305107.appspot.com/o/"+usersList[position].user_id+"%2F"+postInfo.post_id+".pdf?alt=media")
                startActivity(intent)
            }
        }else{
            layout.viewable_resume.visibility = View.GONE
            layout.resume.visibility = View.GONE
        }
        layout.close.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val sp = getSharedPreferences("chores",Context.MODE_PRIVATE)
        val token =sp.getString("token","")
        val id =sp.getString("id","")

        val retrofitData = retrofitBuilder.getAnswersOfUser("$token id $id",post_id,usersList[position].user_id)
        retrofitData.enqueue(object : Callback<ArrayList<String>?> {
            override fun onFailure(call: Call<ArrayList<String>?>, t: Throwable) {
                Toast.makeText(applicationContext,"Internet Connection Required!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<String>?>,
                response: Response<ArrayList<String>?>
            ) {
                if(response.isSuccessful){
                    QuestionAnswerAdapter = QuestionAnswerAdapter(postInfo.questions,false,this@AppliedByList,response.body()!!)
                    val recyclerView : RecyclerView = findViewById(R.id.question_answer_recycler)
                    val layoutManager = LinearLayoutManager(this@AppliedByList)
                    recyclerView.layoutManager = layoutManager
                    recyclerView.adapter = QuestionAnswerAdapter
                    recyclerView.setItemViewCacheSize(5)
                }else{

                    Toast.makeText(applicationContext,"Error! Try Again Later ${response.code()}",Toast.LENGTH_LONG).show()
                }
                layout.pBar_answers.visibility = View.GONE
                layout.question_answer_recycler.visibility = View.VISIBLE
            }
        })
    }

    override fun unauthorized() {
        val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  = Intent(this@AppliedByList,LoginActivity::class.java)
        startActivity(intent)
    }

    override fun changeAnswer(position: Int, answer: String) {
    }

}