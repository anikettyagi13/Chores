package com.example.chores.utils.Adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.R
import com.example.chores.utils.ClickListeners.commentClickListener
import com.example.chores.utils.commentData

class CommentAdapter(var commentList:ArrayList<commentData>, val commentClickListener: commentClickListener) : RecyclerView.Adapter<CommentAdapter.myAdapter>() {
    public class myAdapter(view: View):RecyclerView.ViewHolder(view){
        val comment_username:TextView = view.findViewById(R.id.comment_username)
        val comment_body :TextView = view.findViewById(R.id.comment_body)
        val comment_like_clickable : ImageView = view.findViewById(R.id.comment_like_clickable)
        val commentCreated : TextView = view.findViewById(R.id.comment_created)
        val commentLikes : TextView = view.findViewById(R.id.comment_likes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.comments,parent,false)
        return myAdapter(
            itemView
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        holder.comment_body.setText(commentList[position].comment)
        holder.comment_username.setText(commentList[position].username)
        holder.commentCreated.setText(DateUtils.getRelativeTimeSpanString(commentList[position].time))
        holder.commentLikes.setText(commentList[position].likes.toString())
        Log.i("message comment","$commentList comment")
        if(commentList[position].liked){
            holder.comment_like_clickable.setImageDrawable(ContextCompat.getDrawable(holder.comment_like_clickable.context, R.drawable.liked));
        }

        holder.comment_like_clickable.setOnClickListener {
            if(!commentList[position].liked){
                commentList[position].liked=true
                commentClickListener.likeClickComment(position)
                commentList[position].likes+=1
                holder.commentLikes.setText(commentList[position].likes.toString())
                holder.comment_like_clickable.setImageDrawable(ContextCompat.getDrawable(holder.comment_like_clickable.context, R.drawable.liked));
            }else{
                commentList[position].liked=false
                commentList[position].likes-=1
                commentClickListener.dislikeClickComment(position)
                holder.commentLikes.setText(commentList[position].likes.toString())
                holder.comment_like_clickable.setImageDrawable(ContextCompat.getDrawable(holder.comment_like_clickable.context, R.drawable.like));
            }
        }

        Log.i("message","message herre")
    }
}