package com.example.chores.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class postData(val username:String,
                    val profile_pic:String,
                    val url:String,
                    val user_id: String,
                    val price_tag:String,
                    val post_id:String,
                    val address:String,
                    val state:String,
                    val info :String,
                    val created:String,
                    var likes:Int,
                    var comments:Int,
                    val pincode:String,
                    val time:Long,
                    var liked:Boolean
):Parcelable