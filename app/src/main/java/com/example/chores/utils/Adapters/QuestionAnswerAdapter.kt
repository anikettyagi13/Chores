package com.example.chores.utils.Adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chores.R
import com.example.chores.utils.ClickListeners.questionAnswerClickListener
import org.w3c.dom.Text

class QuestionAnswerAdapter(val QuestionsList:ArrayList<String>,val applying:Boolean,val questionAnswerClickListener: questionAnswerClickListener,val AnswersArray: ArrayList<String>):RecyclerView.Adapter<QuestionAnswerAdapter.myAdapter>() {
    class myAdapter(val view: View):RecyclerView.ViewHolder(view){
        val applied:LinearLayout = view.findViewById(R.id.applied)
        val appling:LinearLayout = view.findViewById(R.id.appling)
        val question: TextView = view.findViewById(R.id.questions)
        val answer :EditText =view.findViewById(R.id.answer)
        val questions :TextView = view.findViewById(R.id.question)
        val answers :TextView = view.findViewById(R.id.answers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdapter {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.answer_requirement,parent,false)
        return myAdapter(view)
    }

    override fun getItemCount(): Int {
        return QuestionsList.size
    }

    override fun onBindViewHolder(holder: myAdapter, position: Int) {
        if(applying){
            holder.appling.visibility = View.VISIBLE
            holder.applied.visibility = View.GONE
            holder.question.text =  "Q${position+1} ${QuestionsList[position]}"
            holder.answer.setOnClickListener {
                holder.answer.requestFocus();
            }
            holder.answer.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?){
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    questionAnswerClickListener.changeAnswer(position,p0.toString())
                }
            })
        }else{
            holder.appling.visibility = View.GONE
            holder.applied.visibility = View.VISIBLE
            holder.questions.text =  "Q${position+1} ${QuestionsList[position]}"
            holder.answers.text = AnswersArray[position]
        }

    }
}