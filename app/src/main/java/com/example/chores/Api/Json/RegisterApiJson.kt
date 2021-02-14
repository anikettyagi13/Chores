package com.example.chores.Api.Json

import java.util.*

data class RegisterApiJson(
    val id: UUID,
    val name:String,
    val email:String,
    val password:String
)