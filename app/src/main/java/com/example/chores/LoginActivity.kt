package com.example.chores

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.chores.Api.Json.LoginApiJson
import com.example.chores.Api.Json.RegisterApiResponse
import com.example.chores.Api.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.chores.Home

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    register.setOnClickListener(this)

        loginClick.setOnClickListener{
            if(email.text.toString().length>0 && password.text.toString().length>=8){
                Log.i("message","adasdasdas")
                val retrofitBuilder = RetrofitBuilder().retrofitBuilder()
                val loginApi = LoginApiJson(email.text.toString(),password.text.toString())
                val retrofitData = retrofitBuilder.LoginApi(loginApi)

                retrofitData.enqueue(object : Callback<RegisterApiResponse?> {
                    override fun onFailure(call: Call<RegisterApiResponse?>, t: Throwable) {
                        Log.i("message error","${t.message}")
                    }

                    override fun onResponse(
                        call: Call<RegisterApiResponse?>,
                        response: Response<RegisterApiResponse?>
                    ) {
                        Log.i("message","${response.body()}")
                        val sharedPref: SharedPreferences = getSharedPreferences("chores", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("id", response.body()!!.id.toString())
                        editor.putString("token", response.body()!!.token)
                        editor.apply()

                        val intent= Intent(this@LoginActivity,Home::class.java)
                        startActivity(intent)
                    }
                })
            }
        }


}

override fun onClick(p0: View?) {
    if (p0 != null) {
        when(p0.id){
            R.id.register -> {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.putExtra("key", "hiiii")
                startActivity(intent)
            }
        }
    }
}
}