package com.example.chores.Api.Json

import java.util.*

data class RegisterApiResponse(val id:UUID,
                               val name:String,
                               val token:String,
                               val error:String) {
}