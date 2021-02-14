package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import com.example.chores.Api.Json.RegisterApiJson
import com.example.chores.Api.Json.RegisterApiResponse
import com.example.chores.Api.RetrofitBuilder
import com.example.chores.utils.utilsInterface
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SignUpActivity : AppCompatActivity() , View.OnClickListener {
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        login.setOnClickListener(this)
        login2.setOnClickListener(this)
         val sharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)

//        sharedPreferences.
         Log.i("message he","${sharedPreferences.getString("id","njk")}")
        signup.setOnClickListener{
            if(email.text.toString().length <1){
                error.text = "Email cannot be left blank"
            }
            else if(username.text.toString().length < 1 ){
                error.text = "Name cannot be left blank"
            }
            else if(password.length()<8 ){
                error.text = "password cannot have less than 8 characters"
            }
            else{
                CallRegisterApi()
            }
        }

    }

    override fun onClick(p0:View){
        when(p0.id){
            R.id.login, R.id.login2 -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun CallRegisterApi() {
        val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
        val uuid = UUID.randomUUID()
        Log.i("message","$uuid")
        val register = RegisterApiJson(
            uuid,
            username.text.toString(),
            email.text.toString(),
            password.text.toString()
        )
        val retrofitData = retrofitBuilder.RegisterApi(register)

        retrofitData.enqueue(object : Callback<RegisterApiResponse?> {
            override fun onFailure(call: Call<RegisterApiResponse?>, t: Throwable) {
                Log.i("message", "${t.message}")
            }
            override fun onResponse(
                call: Call<RegisterApiResponse?>,
                response: Response<RegisterApiResponse?>
            ) {
                if(response.body()?.error?.length ==0){
                    Log.i("message is here","adsdasknjksadmkm")
                    val sharedPref: SharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("id", response.body()!!.id.toString())
                    editor.putString("token", response.body()!!.token)
                    editor.apply()
                    val intent = Intent(this@SignUpActivity, Home::class.java)
                    startActivity(intent)
                }
            }
        })
    }
}