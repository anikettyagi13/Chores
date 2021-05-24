package com.example.chores.utils.ClickListeners

import android.view.View

interface appliedClickListener {
    fun assignClick(position:Int,assign: View,userList:View,excess_buttons:View,chat:View,date2:View,date:View)
    fun rejectClick(position:Int,assign: View,userList:View,excess_buttons:View,chat:View,date2:View,date:View)
    fun rejected(assign: View,userList:View,excess_buttons:View,chat:View,date2:View,date:View)
    fun assigned(assign: View,userList:View,excess_buttons:View,chat:View,date2:View,date:View)
    fun showRequirements(position: Int)
}