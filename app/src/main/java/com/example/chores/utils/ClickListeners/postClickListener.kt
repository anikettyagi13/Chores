package com.example.chores.utils.ClickListeners

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

interface postClickListener {
    fun userNameClick(position:Int)
    fun postClick(position: Int)
    fun likeClick(position:Int)
    fun disLikeCLick(position:Int)
    fun addCommentClick(position:Int,comment:String,comment_write:EditText,comment_view:LinearLayout)
    fun comment(position:Int,username: TextView)
}