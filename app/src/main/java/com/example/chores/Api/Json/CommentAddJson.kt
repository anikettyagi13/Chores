package com.example.chores.Api.Json

import java.util.*

data class CommentAddJson(val post_id:String
                     ,val comment_id:UUID
                     ,val user_id:String
                     ,val username:String
                     ,val profile_pic:String
                     ,val comment:String
                     ,val time:Long
                     ,val likes:Int
)