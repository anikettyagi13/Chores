package com.example.chores.utils

data class commentData(val post_id:String
                          ,val comment_id:String
                          ,val user_id:String
                          ,val username:String
                          ,val profile_pic:String
                          ,val comment:String
                          ,val time:Long
                          ,var likes:Int
                          ,var liked:Boolean
)