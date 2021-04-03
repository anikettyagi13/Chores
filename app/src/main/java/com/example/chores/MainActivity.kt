package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chores.Api.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)

        val id = sharedPreferences.getString("id","")
        val token = sharedPreferences.getString("token","")
        Log.i("see this","$token $id nkj")
        if(token?.length!!>0 && id?.length!!>0){
            val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
            val retroData  = retrofitBuilder.checkLoggedIn("$token id $id")
            retroData.enqueue(object : Callback<String?> {
                override fun onFailure(call: Call<String?>, t: Throwable) {
                    Log.i("message error "," hiiiii ${t.message}")
                    if(t.message!!.contains("Failed to connect to",true)){
                        Toast.makeText(this@MainActivity,"No Internet Connection",Toast.LENGTH_LONG).show()
                        val intent = Intent(this@MainActivity, Home::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    Log.i("login","${response.body()}")
                    if(response.body()=="loggedIN"){
                        val intent = Intent(this@MainActivity, Home::class.java)
                        startActivity(intent)
                    }else if(response.body()=="first time login"){
                        val intent = Intent(this@MainActivity, UserInfoActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        val sharedPref: SharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("id", "")
                        editor.putString("token", "")
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