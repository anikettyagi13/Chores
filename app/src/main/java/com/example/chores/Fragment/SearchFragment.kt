package com.example.chores.Fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.*
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.Adapters.QuestionAnswerAdapter
import com.example.chores.utils.Adapters.postAdapter
import com.example.chores.utils.ClickListeners.appliedClickListener
import com.example.chores.utils.ClickListeners.postClickListener
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import com.example.chores.utils.postData
import com.example.chores.utils.usefullFunctions.CommentRelated
import com.example.chores.utils.usefullFunctions.PostRelated
import com.example.chores.utils.userBasicInfo
import com.example.chores.utils.userInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.apply_on_post.view.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment: Fragment(),postClickListener,userInterface,questionAnswerClickListener,appliedClickListener {
    lateinit var sp:SharedPreferences
    lateinit var userInfo :UserInfoResponse
    lateinit var postAdapter:postAdapter
    lateinit var searchPostAdapter:postAdapter
    var postList = ArrayList<postData>()
    var millis =System.currentTimeMillis()
    var millis2 =System.currentTimeMillis()
    val userList = mutableMapOf<String,userBasicInfo>()
    var new_resume = 0
    var searched =0
    var searchPostsList = ArrayList<postData>()
    var noMoreSearchPost = 1

    lateinit var uri_resume:Uri
    var uploading =false
    lateinit var QuestionAnswerAdapter: QuestionAnswerAdapter
    lateinit var AnswersArray :  ArrayList<String>
    var loading = false
    var noMore = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        val fragment = inflater.inflate(R.layout.fragment_search,container,false)
        sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse
        val recyclerView :RecyclerView = fragment.findViewById(R.id.search_global)
        postAdapter = postAdapter(postList,this,userInfo,true)
        recyclerView.adapter = postAdapter
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.setItemViewCacheSize(6)
        fragment.search_refresh.setOnRefreshListener {
            fragment.pbSearch.visibility = View.GONE
            getPosts()
        }

        val recyclerView_search_posts :RecyclerView = fragment.findViewById(R.id.search_posts_show)
        searchPostAdapter = postAdapter(searchPostsList,this,userInfo,true)
        val layoutManager_search_posts = LinearLayoutManager(context!!)
        recyclerView_search_posts.adapter= searchPostAdapter
        recyclerView_search_posts.layoutManager = layoutManager_search_posts
        fragment.search.isFocusableInTouchMode = true



        fragment.remove_search.setOnClickListener {
            search_global.visibility = View.VISIBLE
            searching.visibility = View.GONE
            searchPostsList.clear()
            fragment.search_now.visibility = View.VISIBLE
            fragment.remove_search.visibility = View.GONE
            fragment.search.setText("")
            searched =0
        }

        fragment.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                fragment.search_now.visibility = View.VISIBLE
                fragment.remove_search.visibility =View.GONE
            }
        })




        fragment.search_now.setOnClickListener {
            if(fragment.search.text.trim().length ==0) Toast.makeText(context!!,"Nothing Found in Search Field",Toast.LENGTH_LONG).show()
            else{
                searched = 1
                fragment.search_global.visibility = View.GONE
                fragment.searching.visibility = View.VISIBLE
                fragment.search_posts_show.visibility = View.VISIBLE
                searchPostsList.clear()
                pbSearch.visibility =View.VISIBLE
                fragment.search_now.visibility = View.GONE
                fragment.remove_search.visibility = View.VISIBLE
                doSearchPosts(fragment.search.text.trim().toString())
            }
        }

        recyclerView_search_posts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                val lastViewed = layoutManager_search_posts.findLastVisibleItemPosition()
                if(lastViewed >=(searchPostsList.size-2)){
                    if(!loading && noMoreSearchPost==0){
                        loading = true;
                        fragment.pbSearch.visibility = View.VISIBLE
                        doSearchPosts(search.text.trim().toString())
                    }
                }
            }
        })

        if(searched == 0 ){
            fragment.searching.visibility = View.GONE
            fragment.search_global.visibility = View.VISIBLE
            fragment.search_now.visibility = View.VISIBLE
            fragment.remove_search.visibility = View.GONE
        }else{
            fragment.search_now.visibility = View.GONE
            fragment.remove_search.visibility = View.VISIBLE
            fragment.searching.visibility = View.VISIBLE
            fragment.search_global.visibility = View.GONE
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastViewed = layoutManager.findLastVisibleItemPosition()
                fragment.search.clearFocus()
                Log.i("message","$lastViewed")
                if(lastViewed>=(postList.size-2)){
                    if(!loading && !noMore){
                        loading = true;
                        getPosts();
                        fragment.pbSearch.visibility = View.VISIBLE
                    }
                }
            }
        })

        if(postList.size == 0){
            fragment.pbSearch.visibility = View.VISIBLE
            fragment.search_refresh.isRefreshing = true
            getPosts()
        }

        return fragment
    }

    private fun doSearchPosts(search:String){
        try{
            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        lateinit var timeInfo : timeInfoAndUserId
            val token  = sp.getString("token","")
            val id  = sp.getString("id","")
        if(searchPostsList.size==0){
            timeInfo = timeInfoAndUserId(0,millis2)
        }else {
            var k = searchPostsList[searchPostsList.size - 1].time
            for (i in searchPostsList.size - 1 downTo 0) {
                if (millis2 > searchPostsList[i].time) {
                    k = searchPostsList[i].time
                    break
                } else {
                    millis2 = searchPostsList[i].time
                }
            }
            timeInfo = timeInfoAndUserId(millis,k)
        }
        val retrofitData = retrofitBuilder.getSearchPosts(search,timeInfo)
            retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
                override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable) {
                    Toast.makeText(context!!,"Internet Connection Required!",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ArrayList<postData>?>,
                    response: Response<ArrayList<postData>?>
                ) {
                    if(response.isSuccessful){
                        val p = response.body()!!
                        if(p.size <9) noMoreSearchPost = 0
                        val k = ArrayList<String>()
                        val o = ArrayList<String>()
                        for(i in p){
                            val a = userList.get(i.user_id)
                            if(a == null) k.add(i.user_id)
                            o.add(i.post_id)
                        }
                        val basicInfo = ArrayList<Deferred<Response<userBasicInfo>>>()
                        val status = ArrayList<Deferred<Response<String>>>()
                        val like = ArrayList<Deferred<Response<String>>>()
                        CoroutineScope(IO).launch {
                            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
                            k.map {
                                val retrofitData = retrofitBuilder.basicUserInfo(it)
                                basicInfo.add( async{ retrofitData.execute() })
                            }
                            o.map{
                                var retrofitData =retrofitBuilder.getPostStatus("$token id $id",it)
                                status.add( async{retrofitData.execute()} )
                            }
                            o.map{
                                var retrofitData = retrofitBuilder.getIfLiked("$token id $id",it)
                                like.add( async{retrofitData.execute()} )
                            }
                            try{
                                showPosts(basicInfo.awaitAll(),status.awaitAll(),like.awaitAll(),p,true)
                            }catch(e:Throwable){
                                Log.i("message","exception $e ${e.cause}" )
                            }
                        }
                    }else{
                        pbSearch.visibility = View.GONE
                        loading = false
                    }
                }
            })
        }catch(e:Exception){

        }

    }

    private fun getPosts() {
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitBuilder =RetrofitBuilder().retrofitBuilder()
        lateinit var timeInfo :timeInfoAndUserId
        if(postList.size==0){
            timeInfo = timeInfoAndUserId(0,millis)
        }else{
            var k=postList[postList.size-1].time
            for( i in postList.size-1 downTo 0){
                if(millis>postList[i].time){
                    k=postList[i].time
                    break
                }else{
                    millis = postList[i].time
                }
            }
            timeInfo = timeInfoAndUserId(millis,k)
            Log.i("message","${postList[(postList.size)-1].time} $k")
        }
        val retrofitData = retrofitBuilder.getGlobal("$token id $id",timeInfo)
        retrofitData.enqueue(object : Callback<ArrayList<postData>?> {
            override fun onFailure(call: Call<ArrayList<postData>?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Required!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<postData>?>,
                response: Response<ArrayList<postData>?>
            ) {
                try{
                    if(response.isSuccessful){
                        val p = response.body()!!
                        val k = ArrayList<String>()
                        val o = ArrayList<String>()
                        for(i in p){
                            val a = userList.get(i.user_id)
                            if(a == null) k.add(i.user_id)
                            o.add(i.post_id)
                        }
                        val basicInfo = ArrayList<Deferred<Response<userBasicInfo>>>()
                        val status = ArrayList<Deferred<Response<String>>>()
                        val like = ArrayList<Deferred<Response<String>>>()
                        CoroutineScope(IO).launch {
                            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
                            k.map {
                                val retrofitData = retrofitBuilder.basicUserInfo(it)
                                basicInfo.add( async{ retrofitData.execute() })
                            }
                            o.map{
                                var retrofitData =retrofitBuilder.getPostStatus("$token id $id",it)
                                status.add( async{retrofitData.execute()} )
                            }
                            o.map{
                                var retrofitData = retrofitBuilder.getIfLiked("$token id $id",it)
                                like.add( async{retrofitData.execute()} )
                            }
                            try{
                                showPosts(basicInfo.awaitAll(),status.awaitAll(),like.awaitAll(),p,false)
                            }catch(e:Throwable){
                                Log.i("message","exception $e ${e.cause}" )
                            }
                        }

                    }else{
                        if(response.code() == 401){

                        }else{
                            Toast.makeText(context!!,"Error! Try Again Later",Toast.LENGTH_LONG).show()
                        }
                        loading = false
                        noMore = false
                        search_refresh.isRefreshing = false
                        pbSearch.visibility =View.GONE
                    }

                }catch(e:Exception){
                    Log.i("message","exception")
                }
            }
        })
    }

    private suspend fun showPosts(basicInfo: List<Response<userBasicInfo>>, status: List<Response<String>>,like:List<Response<String>>,posts:ArrayList<postData>,search_related:Boolean) {
        try{
            withContext(Main){
                var k=0
                val set = mutableSetOf<Int>()
                basicInfo.map{
                    if(it.isSuccessful){
                        val body = it.body()!!
                        userList.put(body.user_id,body)
                    }
                }
                var l=0
                val s = ArrayList<String>()
                status.map{
                    if(it.isSuccessful){
                        val body = it.body()!!
                        s.add(body)
                    }else{
                        set.add(l)
                        k=1
                    }
                    l++
                }
                val likes = ArrayList<Boolean>()
                l=0
                like.map{
                    if(it.isSuccessful){
                        val body = it.body()!!
                        var m =false
                        if(body == "true") m=true
                        else m = false
                        likes.add(m)
                    }else{
                        set.add(l)
                    }
                    l++
                }
                l=0
                var remove = mutableSetOf<Int>()
                posts.map{
                    var j = userList.get(it.user_id)
                    var f = set.contains(l)
                    if(j != null && !f ){
                        it.status = s[l]
                        it.liked = likes[l]
                        it.username = j.username
                        it.profile_pic = j.profile_pic
                    }else{
                        remove.add(l)
                    }
                    l++
                }
                for( i in set){
                    posts.removeAt(i)
                }
                if(search_related){
                    searchPostsList.addAll(posts)
                    Log.i("message","sadas $searchPostsList" )
                    searchPostAdapter.notifyDataSetChanged()
                }else{
                    postList.addAll(posts)
                    postAdapter.notifyDataSetChanged()
                }
            }
            loading = false
            noMore = false
            search_refresh.isRefreshing = false
            pbSearch.visibility =View.GONE
        }catch(e:Exception){
            Log.i("message","exception $e")
        }
    }

    override fun showTags(position:Int){
    }

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
        val intent = Intent(activity, Post_full_Screen::class.java)
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
            val intent = Intent(context!!, WebView::class.java)
            intent.putExtra("url",userInfo.resume)
            startActivity(intent)
        }
    }

    fun getResume(): Uri {
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

        PostRelated().applyOnPost(context!!,postList[position],userInfo,this,layout,bottomSheetBehavior,postAdapter,::questionsView,::sendAnswers,::choose_resume,::getResume,::resumeChoosed,::set_uploading)
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
            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
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
                        QuestionAnswerAdapter = QuestionAnswerAdapter(post.questions,false,this@SearchFragment,response.body()!!)
                        layout.pBar_answers.visibility = View.GONE
                        layout.questionsAnswer_recycler_view.visibility =View.VISIBLE
                        recyclerView.adapter = QuestionAnswerAdapter
                        val layoutManager = LinearLayoutManager(this@SearchFragment.context)
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
                    val intent = Intent(context!!, AppliedByList::class.java)
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
        val sharedPref: SharedPreferences = context!!.getSharedPreferences("chores", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("id", "")
        editor.putString("token", "")
        editor.putString("username","")
        val intent  = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun assignClick(
        position: Int,
        assign: View,
        userList: View,
        excess_buttons: View,
        chat: View,
        date2: View,
        date: View
    ) {
    }

    override fun rejectClick(
        position: Int,
        assign: View,
        userList: View,
        excess_buttons: View,
        chat: View,
        date2: View,
        date: View
    ) {
    }

    override fun rejected(
        assign: View,
        userList: View,
        excess_buttons: View,
        chat: View,
        date2: View,
        date: View
    ) {
    }

    override fun assigned(
        assign: View,
        userList: View,
        excess_buttons: View,
        chat: View,
        date2: View,
        date: View
    ) {
    }

    override fun showRequirements(position: Int) {
    }
}