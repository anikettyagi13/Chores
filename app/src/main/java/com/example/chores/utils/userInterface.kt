package com.example.chores.utils

import com.example.chores.Api.Json.UserInfoResponse

interface userInterface {
    public fun unauthorized()
}

interface userInfoInterface{
    public fun changeUserInfo(info:UserInfoResponse)
}