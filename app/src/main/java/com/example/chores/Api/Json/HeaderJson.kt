package com.example.chores.Api.Json

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class HeaderJson(val token:String,val id:String):Parcelable