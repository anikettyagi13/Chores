package com.example.chores.utils.Adapters

import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoAppliedList
import com.example.chores.R
import com.example.chores.utils.ClickListeners.appliedClickListener

class UsersAdapter(val usersList:ArrayList<UserInfoAppliedList>,val appliedClickListener: appliedClickListener):RecyclerView.Adapter<UsersAdapter.myAdapter>() {
    class myAdapter(view: View):RecyclerView.ViewHolder(view){
        val userimage : ImageView = view.findViewById(R.id.userImage)
        val username :TextView = view.findViewById(R.id.username)
        val bio :TextView = view.findViewById(R.id.bio)
        val jobs_created :TextView = view.findViewById(R.id.jobs_created)
        val jobs_completed :TextView = view.findViewById(R.id.jobs_completed)
        val ratings :TextView = view.findViewById(R.id.ratings)
        val date :TextView = view.findViewById(R.id.date)
        val date2 :TextView = view.findViewById(R.id.date2)
        val assign: ImageButton = view.findViewById(R.id.assign)
        val reject: ImageButton = view.findViewById(R.id.reject)
        val requirements: ImageView = view.findViewById(R.id.requirements)
        val userList :LinearLayout = view.findViewById(R.id.userList)
        val chat :ImageButton = view.findViewById(R.id.chat)
        val excess_button:RelativeLayout = view.findViewById(R.id.excess_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdapter {
        val intent =LayoutInflater.from(parent.context)
            .inflate(R.layout.userlist,parent, false)
        return myAdapter(intent)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        holder.username.setText("${usersList[position].name}")
        holder.bio.text = usersList[position].bio
        holder.jobs_created.text = usersList[position].jobs_created.toString()
        holder.jobs_completed.text = usersList[position].jobs_completed.toString()
        holder.ratings.text = usersList[position].ratings.toString()
        Glide.with(holder.userimage.context).load(usersList[position].profile_pic).placeholder(R.drawable.account_border).into(holder.userimage)
        holder.date.text = DateUtils.getRelativeTimeSpanString(usersList[position].time)
        holder.date2.text = DateUtils.getRelativeTimeSpanString(usersList[position].time)
        holder.userList.setOnClickListener {
            appliedClickListener.showRequirements(position)
        }

        if(usersList[position].status =="assigned"){
            appliedClickListener.assigned(holder.assign,holder.userList,holder.excess_button,holder.chat,holder.date2,holder.date)
        }else if(usersList[position].status == "rejected"){
         appliedClickListener.rejected(holder.assign,holder.userList,holder.excess_button,holder.chat,holder.date2,holder.date)
        }else{
            holder.assign.setOnClickListener {
                appliedClickListener.assignClick(position,holder.assign,holder.userList,holder.excess_button,holder.chat,holder.date2,holder.date)
            }
            holder.reject.setOnClickListener{
                appliedClickListener.rejectClick(position,holder.assign,holder.userList,holder.excess_button,holder.chat,holder.date2,holder.date)
            }
        }

    }
}