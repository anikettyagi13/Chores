package com.example.chores.Api.Json

data class UserInfoAppliedList(var name:String,
                          var profile_pic:String,
                          var username:String,
                          var pincodes:ArrayList<String>,
                          val jobs_created:Int,
                          val jobs_completed:Int,
                          val ratings:Double,
                          val user_id:String,
                          var bio:String,
                          var website:String,
                          var status:String, val time:Long)