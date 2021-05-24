package com.example.chores

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.Fragment.*
import com.example.chores.utils.userInfoInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class Home : AppCompatActivity(),Serializable,userInfoInterface {
    lateinit var userInfo:UserInfoResponse
    var home = true
    override fun onCreate(savedInstanceState: Bundle?) {
        val HomeFragment = HomeFragment()
        val HeartFragment = HeartFragment()
        val SearchFragment = SearchFragment()
        val AddFragment = AddFragment()
        val AccountFragment = AccountFragment(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val view : View = findViewById(R.id.bottom_sheet_apply)
        val bottomSheetBehavior = BottomSheetBehavior.from(view)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottom.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    setCurrentFragment(HomeFragment)
                    home = true
                }
                R.id.search ->{
                    setCurrentFragment(SearchFragment)
                    home=false
                }
                R.id.add ->{
                    setCurrentFragment(AddFragment)
                    home=false
                }
                R.id.heart ->{
                    setCurrentFragment(HeartFragment)
                    home=false
                }
                R.id.account ->{
                    setCurrentFragment(AccountFragment)
                    home=false
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

    override fun onBackPressed() {
        if(home){
            finish()
        }else{
            setCurrentFragment(HomeFragment())
            home =true
        }
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