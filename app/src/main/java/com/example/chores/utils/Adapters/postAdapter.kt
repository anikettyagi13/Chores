package com.example.chores.utils.Adapters

import android.text.format.DateUtils
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
import com.example.chores.utils.postData
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.posts.*


class postAdapter(val postsList:List<postData>, val postClickListener: postClickListener, val userData:UserInfoResponse,val search:Boolean): RecyclerView.Adapter<postAdapter.myAdapter>(){
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
        val global :ImageView =view.findViewById(R.id.global)
        val apply_image1:RelativeLayout= view.findViewById(R.id.applyImage1)
        val post_info_show_more:TextView = view.findViewById(R.id.post_info_show_more)
        var applied: TextView = view.findViewById(R.id.applied)
        val rejected :RelativeLayout= view.findViewById(R.id.rejected)
        val assigned :RelativeLayout= view.findViewById(R.id.assigned)
        val waiting :RelativeLayout= view.findViewById(R.id.waiting)
        val post_more :ImageButton = view.findViewById(R.id.post_more)
        val tags:FlexboxLayout = view.findViewById(R.id.tags)
        val tag1:TextView = view.findViewById(R.id.tag1)
        val tag2:TextView = view.findViewById(R.id.tag2)
        val tag3:TextView = view.findViewById(R.id.tag3)
        val tag4:TextView = view.findViewById(R.id.tag4)
        val tag5:TextView = view.findViewById(R.id.tag5)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.posts,parent,false)
        return myAdapter(itemView)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
    @RequiresApi(21)
    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        holder.username.text = postsList[position].username
        Glide.with(holder.posts_image.getContext()).load(postsList[position].url)
            .into(holder.posts_image)

        Glide.with(holder.post_userImage.getContext()).load(postsList[position].profile_pic).placeholder(R.drawable.account_border).into(holder.post_userImage)
        Glide.with(holder.apply_image.getContext()).load(userData.profile_pic).placeholder(R.drawable.account_border).into(holder.apply_image)
        holder.price_tag.text = postsList[position].price_tag

        if(postsList[position].address == "GLOBAL"){
            holder.global.visibility =View.VISIBLE
            holder.pincode.visibility = View.GONE
        }
        else{
            holder.global.visibility =View.GONE
            holder.pincode.visibility = View.VISIBLE
            holder.pincode.text = postsList[position].pincode
        }

        if(search){
            if(postsList[position].tag1 !="null"&& postsList[position].tag1 != ""){
                holder.tags.visibility =View.VISIBLE
                holder.tag1.visibility = View.VISIBLE
                holder.tag1.text = postsList[position].tag1
            }
            if(postsList[position].tag2 !="null"&& postsList[position].tag2 != ""){
                holder.tag2.visibility = View.VISIBLE
                holder.tag2.text = postsList[position].tag2
            }
            if(postsList[position].tag3 !="null" && postsList[position].tag3 != ""){
                holder.tag3.visibility = View.VISIBLE
                holder.tag3.text = postsList[position].tag3
            }
            if(postsList[position].tag4 !="null"&& postsList[position].tag4 != ""){
                holder.tag4.visibility = View.VISIBLE
                holder.tag4.text = postsList[position].tag4
            }
            if(postsList[position].tag5 !="null"&& postsList[position].tag5 != ""){
                holder.tag5.visibility = View.VISIBLE
                holder.tag5.text = postsList[position].tag5
            }
        }else{
            holder.tags.visibility = View.GONE
        }

        if(postsList[position].info.length>20){
            holder.posts_info.text = postsList[position].info.substring(0,20) + " ..."
            holder.post_info_show_more.visibility = VISIBLE
        }else{
            holder.posts_info.text = postsList[position].info
        }
        holder.posts_exact_location.text = postsList[position].address
        holder.username2.text = postsList[position].username
        holder.posts_created.text = DateUtils.getRelativeTimeSpanString(postsList[position].time)
        holder.post_likes.text = postsList[position].likes.toString()
        holder.post_comments.text = postsList[position].comments.toString()
        holder.applied.text = postsList[position].applied.toString()
        if(userData.user_id == ""){
            holder.apply_image1.visibility = View.GONE
            holder.waiting.visibility = View.GONE
            holder.assigned.visibility = View.GONE
            holder.rejected.visibility  =View.GONE
            holder.assigned.visibility = View.GONE
        }else{
            if(userData.user_id == postsList[position].user_id){
                holder.apply_image1.visibility = View.GONE
                holder.post_more.visibility = View.VISIBLE
                holder.post_more.setOnClickListener {
                    postClickListener.showMenu(position,holder.post_more)
                }
            }else{
                if(postsList[position].status == "rejected"){
                    holder.rejected.visibility = View.VISIBLE
                    holder.apply_image1.visibility = View.GONE
                    holder.waiting.visibility = View.GONE
                    holder.assigned.visibility = View.GONE
                    holder.rejected.setOnClickListener {
                        postClickListener.applyOnPost(position)
                    }
                }else if(postsList[position].status == "waiting"){
                    holder.waiting.visibility = View.VISIBLE
                    holder.apply_image1.visibility = View.GONE
                    holder.rejected.visibility  =View.GONE
                    holder.assigned.visibility = View.GONE
                    holder.waiting.setOnClickListener {
                        postClickListener.applyOnPost(position)
                    }
                }else if(postsList[position].status == "assigned"){
                    holder.assigned.visibility = View.VISIBLE
                    holder.apply_image1.visibility = View.GONE
                    holder.rejected.visibility = View.GONE
                    holder.waiting.visibility = View.GONE
                    holder.assigned.setOnClickListener {
                        postClickListener.applyOnPost(position)
                    }
                }else{
                    holder.apply_image1.visibility = View.VISIBLE
                    holder.assigned.visibility = View.GONE
                    holder.waiting.visibility=View.GONE
                    holder.rejected.visibility = View.GONE
                    holder.apply_image.setOnClickListener {
                        postClickListener.applyOnPost(position)
                    }
                }

            }

        }

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
        holder.username2.setOnClickListener{
            postClickListener.userNameClick(position)
        }
        holder.post_userImage.setOnClickListener {
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