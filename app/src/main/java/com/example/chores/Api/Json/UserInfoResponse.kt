package com.example.chores.Api.Json

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class UserInfoResponse(val name:String,
                            val profile_pic:String,
                            val username:String,
                            val pincodes:ArrayList<String>,
                            val jobs_created:Int,
                            val jobs_completed:Int,
                            val ratings:Double,
                            val user_id:String,
                            val error:String): Serializable