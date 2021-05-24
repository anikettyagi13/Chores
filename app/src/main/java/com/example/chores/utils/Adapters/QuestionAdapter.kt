package com.example.chores.utils.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.R
import com.example.chores.utils.ClickListeners.questionsClickListener

class QuestionAdapter(val questionsList :ArrayList<String>,val questionsClickListener: questionsClickListener):RecyclerView.Adapter<QuestionAdapter.myAdapter>() {

    public class myAdapter(view: View):RecyclerView.ViewHolder(view) {
        val question: TextView = view.findViewById(R.id.question)
        val edit_question :ImageButton= view.findViewById(R.id.edit_question)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdapter {
        Log.i("message","${questionsList} view holder")
        val view =  LayoutInflater.from(parent.context)
            .inflate(R.layout.question_requirement,parent,false)
        return myAdapter(view)
    }

    override fun getItemCount(): Int {
        return questionsList.size
    }

    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        holder.question.setText("${questionsList[position]}")
        holder.edit_question.setOnClickListener { questionsClickListener.questionEdit(position,holder.edit_question) }
        ViewCompat.animate(holder.edit_question).setDuration(500).alpha(1f)
    }
}