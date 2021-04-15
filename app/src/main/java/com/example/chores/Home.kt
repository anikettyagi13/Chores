package com.example.chores

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.Fragment.*
import com.example.chores.utils.userInfoInterface
import kotlinx.android.synthetic.main.activity_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class Home : AppCompatActivity(),Serializable,userInfoInterface {
    lateinit var userInfo:UserInfoResponse
    override fun onCreate(savedInstanceState: Bundle?) {
        val HomeFragment = HomeFragment()
        val HeartFragment = HeartFragment()
        val SearchFragment = SearchFragment()
        val AddFragment = AddFragment()
        val AccountFragment = AccountFragment(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottom.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    setCurrentFragment(HomeFragment)
                }
                R.id.search ->{
                    setCurrentFragment(SearchFragment)
                }
                R.id.add ->{
                    setCurrentFragment(AddFragment)
                }
                R.id.heart ->{
                    setCurrentFragment(HeartFragment)
                }
                R.id.account ->{
                    setCurrentFragment(AccountFragment)
                }
            }
            true
        }

        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val sp = getSharedPreferences("chores", Context.MODE_PRIVATE)
        val token = sp.getString("token","")
        val id = sp.getString("id","")
        val retrofitData = retrofitBuilder.getUserInfo("$token id $id")
        retrofitData.enqueue(object : Callback<UserInfoResponse?> {
            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                Log.i("message error","hiii ${t.message}")
            }

            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                 userInfo = response.body()!!
                Log.i("message","${response.body()} hiiii")
                setCurrentFragment(HomeFragment)
                getIntent().putExtra("userInfo",response.body())
            }
        })
    }

    private fun setCurrentFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment,fragment)
            commit()
        }
    }

    override fun changeUserInfo(info:UserInfoResponse) {
        userInfo = info
        getIntent().putExtra("userInfo",userInfo)
    }

}