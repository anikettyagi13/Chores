package com.example.chores.Api.Json

import java.util.*

data class RegisterApiResponse(val id:String,
                               val username:String,
                               val token:String,
                               val error:String) {
}