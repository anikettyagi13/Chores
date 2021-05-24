package com.example.chores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web_view.*
import java.net.URLEncoder

class WebView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val url = this.intent.getStringExtra("url")!!
        Log.i("message","$url")
        web_view.webViewClient = WebViewClient()
        web_view.settings.setSupportZoom(true)
        web_view.settings.javaScriptEnabled = false
        web_view.loadUrl("http://docs.google.com/gview?embedded=true&url="+URLEncoder.encode(url,"UTF-8"))

        back.setOnClickListener { finish() }
    }
}