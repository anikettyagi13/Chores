package com.example.chores.utils.Adapters

import android.text.Html
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.R
import com.example.chores.utils.ClickListeners.notificationClickListener
import com.example.chores.utils.notificationData

class notificationAdapter(val notificationList:ArrayList<notificationData>, val notificationClickListener: notificationClickListener) :RecyclerView.Adapter<notificationAdapter.myAdapter>() {
    class myAdapter(view: View):RecyclerView.ViewHolder(view){
        val notification:RelativeLayout =view.findViewById(R.id.notification)
        val noti_profile_pic:ImageView = view.findViewById(R.id.noti_profile_pic)
        val noti_pic:ImageView = view.findViewById(R.id.noti_pic)
        val noti_info:TextView = view.findViewById(R.id.noti_info)
        val noti_time:TextView = view.findViewById(R.id.noti_time)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): myAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification,parent,false)
        return myAdapter(
            itemView
        )
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        Glide.with(holder.noti_profile_pic.context).load(notificationList[position].profile_pic).placeholder(R.drawable.account_border).into(holder.noti_profile_pic)
        Glide.with(holder.noti_pic.context).load(notificationList[position].post_pic).placeholder(R.drawable.ic_outline_image_24).into(holder.noti_pic)
        holder.noti_profile_pic.setOnClickListener {
            notificationClickListener.userClick(position)
        }
        var text = ""
        if(notificationList[position].type == "assign") text = "<b>${notificationList[position].username}</b> assigned you this chore. ${notificationList[position].data}"
         else text  ="<b>${notificationList[position].username}</b> and others ${notificationList[position].data} ${notificationList[position].count}"
        holder.noti_info.setText(Html.fromHtml(text))
        holder.noti_time.text = DateUtils.getRelativeTimeSpanString(notificationList[position].time)

        holder.notification.setOnClickListener {
            notificationClickListener.notificationClick(position)
        }
    }
}