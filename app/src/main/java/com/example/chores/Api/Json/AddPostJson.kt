package com.example.chores.Api.Json

import android.os.Parcelable
import android.text.Editable
import kotlinx.android.parcel.Parcelize
import java.util.*
data class AddPostJson(  val post_id: String
                         ,val user_id: String
                         ,val pincode: String
                         ,val address:String
                         ,val state: String
                         ,val username: String
                         ,val info: String
                         ,val likes:Int
                         ,val comments:Int
                         ,val url :String
                         ,val price_tag :String,
                         val profile_pic:String,
                         val created:String,
                         val time:Long
)