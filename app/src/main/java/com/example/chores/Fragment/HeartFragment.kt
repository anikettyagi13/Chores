package com.example.chores.Fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.AccountDetails
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.Json.notificationCount
import com.example.chores.Api.Json.notificationCountPost
import com.example.chores.Api.Json.timeInfoAndUserId
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.Post_full_Screen
import com.example.chores.R
import com.example.chores.utils.ClickListeners.notificationClickListener
import com.example.chores.utils.Adapters.notificationAdapter
import com.example.chores.utils.notificationData
import com.example.chores.utils.userBasicInfo
import kotlinx.android.synthetic.main.fragment_heart.*
import kotlinx.android.synthetic.main.fragment_heart.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HeartFragment: Fragment(),notificationClickListener {
    lateinit var notificationAdapter: notificationAdapter
    var notificationList =ArrayList<notificationData>()
    var loading = false
    lateinit var userInfo :UserInfoResponse
    var millis:Long = System.currentTimeMillis()
    var usersInfo = mutableMapOf<String,userBasicInfo>()
    var noMore = false
    lateinit var sp :SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_heart,container,false)
        sp = context!!.getSharedPreferences("chores",Context.MODE_PRIVATE)
        val recyclerView:RecyclerView = fragment.findViewById(R.id.notifications);
        notificationAdapter =
            notificationAdapter(
                notificationList,
                this
            )
        val layoutInflater = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = layoutInflater
        recyclerView.adapter = notificationAdapter
        recyclerView.isNestedScrollingEnabled = true

        if(activity!!.intent.getSerializableExtra("userInfo") != null) userInfo = activity!!.intent.getSerializableExtra("userInfo") as UserInfoResponse

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastViewed = layoutInflater.findLastVisibleItemPosition()
                Log.i("message","$lastViewed")
                if(lastViewed>=(notificationList.size-2)){
                    if(!loading && !noMore){
                        loading = true;
                        getNotifications();
                        fragment.pBar_noti.visibility = View.VISIBLE
                    }
                }
            }
        })
        fragment.notifications_swipe.setOnRefreshListener {
            getNotifications();
            notifications_swipe.isRefreshing = true
            fragment.pBar_noti.visibility = View.VISIBLE
        }
        if(notificationList.size == 0){
            getNotifications()
            fragment.notifications_swipe.isRefreshing = true
            fragment.pBar_noti.visibility = View.VISIBLE
        }
        return fragment
    }

    private fun getNotifications(){
        val token = sp.getString("token","");
        val id = sp.getString("id","");
        lateinit var time:timeInfoAndUserId
        if(notificationList.size == 0) time = timeInfoAndUserId(0,millis)
        else time = timeInfoAndUserId(millis,notificationList[notificationList.size-1].time)
        Log.i("message","${time} ")
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder();
        val retrofitData = retrofitBuilder.notification("$token id $id",time);
        retrofitData.enqueue(object : Callback<ArrayList<notificationData>?> {
            override fun onFailure(call: Call<ArrayList<notificationData>?>, t: Throwable) {
                Toast.makeText(context,"Internet Connection Hindered! ${t.message}",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ArrayList<notificationData>?>,
                response: Response<ArrayList<notificationData>?>
            ) {
                try{
                    if(response.isSuccessful){
                        if(response.body()!!.size >0){
//                        var new_arr:ArrayList<notificationData> = notificationList.toMutableList() as ArrayList<notificationData>
                            var new_users = ArrayList<String>()
                            var k = response.body()!!

                            for(i in 0 .. (k.size-1)){
                                if(!usersInfo.contains(k[i].user_id)){
                                    new_users.add(k[i].user_id)
                                    usersInfo.put(k[i].user_id, userBasicInfo(k[i].user_id,"",""))
                                }
                            }

                            for(i in 0..(response.body()!!.size-1)){
                                if(millis>response.body()!![i].time){
                                    break
                                }else{
                                    millis=response.body()!![i].time
                                }
                            }

                            val token = sp.getString("token","")
                            val id = sp.getString("id","")
                            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
                            var newkl = ArrayList<Deferred<Response<notificationCount>>>()
                            var newUser = ArrayList<Deferred<Response<userBasicInfo>>>()
                            CoroutineScope(IO).launch {
                                k.map {
                                    val retrofitData = retrofitBuilder.getCount("$token id $id", notificationCountPost(it.post_id,it.type))
                                    newkl.add (async {  retrofitData.execute() })
                                }
                                new_users.map{
                                    val retrofitData = retrofitBuilder.basicUserInfo(it)
                                    newUser.add(async { retrofitData.execute()})
                                }
                                makeNewFunction(newkl.awaitAll(),newUser.awaitAll(),notificationList,k)
                            }
                        }else{
                            noMore = true
                            loading = false
                            if(notifications_swipe != null) notifications_swipe.isRefreshing = false
                            if(pBar_noti !=null) pBar_noti.visibility = View.GONE
                        }
                    }
                }catch(e:Exception){
                    Log.i("message","Exception $e")
                }
            }
        })
    }

    private suspend fun makeNewFunction(noti: List<Response<notificationCount>>,new_user:List<Response<userBasicInfo>>,notifications:ArrayList<notificationData>,new_noti:ArrayList<notificationData>){
        try{
        withContext(Main){
                var serverProb = 0;
                var k=0;
                Log.i("message","hiiiiiooiiioioioioi")
                noti.map{
                    if(!it.isSuccessful) serverProb++;
                    else{
                        new_noti[k].count = it.body()!!.count
                        k++
                    }
                }
                new_user.map {
                    if (it.isSuccessful) {
                        val user = it.body()!!
                        usersInfo.put(user.user_id, userBasicInfo(user.user_id, user.username, user.profile_pic))
                    }else {
                        serverProb++;
                    }
                }
                if(serverProb==0)
                    new_noti.map{
                        val user = usersInfo.get(it.user_id)!!
                        it.username = user.username
                        it.profile_pic = user.profile_pic
                    }
                if(serverProb ==0){
                    // check if new noti with notifications passed here.
                    val new = notificationList.toMutableList() as ArrayList<notificationData>
                    new.addAll(new_noti)
                    new.sortByDescending { noti -> noti.time }
                    notificationList.clear()
                    notificationList.addAll(new)
                    notificationAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(context, "Try again later!", Toast.LENGTH_LONG).show()
                }
            }

            notifications_swipe.isRefreshing = false
            pBar_noti.visibility = View.GONE
        }
        catch (e:Exception){
            Log.i("message","Expection $e")
        }
        loading = false
    }

    override fun notificationClick(position: Int) {
        val intent = Intent(context!!,Post_full_Screen::class.java)
        intent.putExtra("postId",notificationList[position].post_id)
        intent.putExtra("userInfo",userInfo)
        startActivity(intent)
    }

    override fun userClick(position: Int) {
        val intent = Intent(context!!, AccountDetails::class .java)
        intent.putExtra("userId",notificationList[position].user_id)
        intent.putExtra("selfInfo",userInfo)
        startActivity(intent)
    }

}