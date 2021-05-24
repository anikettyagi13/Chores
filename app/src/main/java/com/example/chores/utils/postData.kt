package com.example.chores.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


data class postData(var username:String,
                    var profile_pic:String,
                    val url:String,
                    val user_id: String,
                    val price_tag:String,
                    val post_id:String,
                    val address:String,
                    val state:String,
                    val info :String,
                    var likes:Int,
                    var comments:Int,
                    val pincode:String,
                    val time:Long,
                    var liked:Boolean,
                    var applied: Int,
                    var status :String,
                    val resume:Boolean,
                    val questions:ArrayList<String>,
                    val tag1:String,
                    val tag2:String,
                    val tag3:String,
                    val tag4:String,
                    val tag5:String
):Serializable