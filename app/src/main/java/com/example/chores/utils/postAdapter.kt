package com.example.chores.utils

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chores.Api.Json.UserInfoResponse
import com.example.chores.R
import com.example.chores.utils.ClickListeners.postClickListener


class postAdapter(val postsList:List<postData>, val postClickListener: postClickListener, val userData:UserInfoResponse): RecyclerView.Adapter<postAdapter.myAdapter>(){
    public class myAdapter(view: View):RecyclerView.ViewHolder(view){
        val post_userImage: ImageView = view.findViewById(R.id.posts_userImage)
        val username:TextView = view.findViewById(R.id.posts_username)
        val price_tag:TextView = view.findViewById(R.id.posts_price_tag)
        val pincode:TextView = view.findViewById(R.id.posts_pincode)
        val posts_image: ImageView = view.findViewById(R.id.posts_image)
        val username2:TextView = view.findViewById(R.id.posts_username2)
        val posts_exact_location:TextView = view.findViewById(R.id.posts_exact_location)
        val posts_info:TextView = view.findViewById(R.id.posts_info)
        val posts_created:TextView = view.findViewById(R.id.posts_created)
        val post_share: ImageView = view.findViewById(R.id.post_share)
        val post_like: ImageView = view.findViewById(R.id.post_like)
        val post_comment: ImageView = view.findViewById(R.id.post_comment)
        val post_likes: TextView = view.findViewById(R.id.post_likes)
        val post_comments: TextView = view.findViewById(R.id.post_comments)
        val comment_write:TextView = view.findViewById(R.id.comment_write)
        val comment_view:LinearLayout = view.findViewById(R.id.comment_view)
        val add_comment:ImageButton = view.findViewById(R.id.add_comment)
        val commentUsername:TextView = view.findViewById(R.id.post_comment_username)
        val apply_image:ImageView = view.findViewById(R.id.applyImage)
        val post_info_show_more:TextView = view.findViewById(R.id.post_info_show_more)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): postAdapter.myAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.posts,parent,false)
        return myAdapter(itemView)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
    @RequiresApi(21)
    override fun onBindViewHolder(holder: postAdapter.myAdapter, position: Int) {
        holder.username.text = postsList[position].username
        Glide.with(holder.posts_image.getContext()).load(postsList[position].url).placeholder(R.drawable.ic_outline_image_24)
            .into(holder.posts_image)

        Glide.with(holder.post_userImage.getContext()).load(postsList[position].profile_pic).placeholder(R.drawable.account_border).into(holder.post_userImage)
        Glide.with(holder.apply_image.getContext()).load(userData.profile_pic).placeholder(R.drawable.account_border).into(holder.apply_image)
        holder.price_tag.text = postsList[position].price_tag
        holder.pincode.text = postsList[position].pincode
        if(postsList[position].info.length>20){
            holder.posts_info.text = postsList[position].info.substring(0,20) + " ..."
            holder.post_info_show_more.visibility = VISIBLE
        }else{
            holder.posts_info.text = postsList[position].info
        }
        holder.posts_exact_location.text = postsList[position].address
        holder.username2.text = postsList[position].username
        holder.posts_created.text = postsList[position].created
        holder.post_likes.text = postsList[position].likes.toString()
        holder.post_comments.text = postsList[position].comments.toString()

        if(postsList[position].liked){
            holder.post_like.setImageDrawable(ContextCompat.getDrawable(holder.post_like.context, R.drawable.liked));
        }else{
            holder.post_like.setImageDrawable(ContextCompat.getDrawable(holder.post_like.context, R.drawable.like))
        }
        holder.posts_image.setBackgroundTintList(holder.posts_image.context.getResources().getColorStateList(R.color.colorPrimaryDark))

        holder.post_comment.setOnClickListener {
            postClickListener.comment(position,holder.commentUsername)
            holder.comment_view.setVisibility(View.VISIBLE)
        }
        holder.add_comment.setOnClickListener{
            postClickListener.addCommentClick(position,holder.comment_write.text.toString(),holder.comment_write as EditText,holder.comment_view)
            postsList[position].comments+=1
            holder.post_comments.setText(postsList[position].comments.toString())
        }
        holder.username.setOnClickListener{
         postClickListener.userNameClick(position)
        }
        holder.itemView.setOnClickListener{
            postClickListener.postClick(position)
        }
        holder.post_info_show_more.setOnClickListener{
            showMore(position,holder)
        }
        holder.post_like.setOnClickListener{
            if(!postsList[position].liked){
                Log.i("message likedddddd","liked kokokokokokokokoko click")
                holder.post_like.setImageDrawable(ContextCompat.getDrawable(holder.post_like.context, R.drawable.liked))
                postsList[position].liked = true
                postsList[position].likes +=1
                holder.post_likes.text = postsList[position].likes.toString()
                holder.post_comments.text = postsList[position].comments.toString()
                postClickListener.likeClick(position)
            }
            else if(postsList[position].liked){
                Log.i("message likedddddd","disliked kokokokokokokokoko click")
                postsList[position].liked = false
                postsList[position].likes -=1
                holder.post_likes.text = postsList[position].likes.toString()
                holder.post_comments.text = postsList[position].comments.toString()
                holder.post_like.setImageDrawable(ContextCompat.getDrawable(holder.post_like.context, R.drawable.like))
                postClickListener.disLikeCLick(position)
            }
        }
    }

    private fun showMore(position: Int,holder: myAdapter) {
        holder.posts_info.text = postsList[position].info
        holder.post_info_show_more.visibility = GONE
    }
}