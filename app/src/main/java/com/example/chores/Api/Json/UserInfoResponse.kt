package com.example.chores.Api.Json

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class UserInfoResponse(var name:String,
                            var profile_pic:String,
                            var username:String,
                            var pincodes:ArrayList<String>,
                            val jobs_created:Int,
                            val jobs_completed:Int,
                            val ratings:Double,
                            val user_id:String,
                            var bio:String,
                            var website:String,
                            val error:String): Serializable