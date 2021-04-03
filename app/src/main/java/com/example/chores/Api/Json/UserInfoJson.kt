package com.example.chores.Api.Json

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfoJson(val username:String
                        ,val user_id:String
                        ,val profile_pic:String
                        ,val name:String
                        ,val pincodes:ArrayList<String>
                        ,val jobs_created:Int
                        ,val jobs_completed:Int
                        ,val ratings:Double): Parcelable