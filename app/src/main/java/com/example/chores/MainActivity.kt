package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chores.Api.ApiInterface
import com.example.chores.Api.Json.HeaderJson
import com.example.chores.Api.Json.RegisterApiJson
import com.example.chores.Api.Json.RegisterApiResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.utilsInterface
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)

        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        if(token?.length!!>0 && id?.length!!>0){
            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
            val retroData  = retrofitBuilder.checkLoggedIn("$token id $id")
            retroData.enqueue(object : Callback<String?> {
                override fun onFailure(call: Call<String?>, t: Throwable) {
                    Log.i("message error ","${t.message}")
                }
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if(response.body() =="loggedIN"){
                        val intent = Intent(this@MainActivity, Home::class.java)
                        intent.putExtra("key", "hiiii")
                        startActivity(intent)
                    }else{
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.putExtra("key", "hiiii")
                        startActivity(intent)
                    }
                }
            })
        }else{
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.putExtra("key", "hiiii")
            startActivity(intent)
        }
    }

}