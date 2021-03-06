package com.example.habittracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val habitList: ArrayList<String>, private val context: Context) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {

    class CustomViewHolder : RecyclerView.ViewHolder {
        private val view: View
        val checkBox: CheckBox
        private val infoButton: Button

        constructor(view: View) : super(view) {
            this.view = view
            checkBox = view.findViewById(R.id.checkBox)
            infoButton = view.findViewById(R.id.infoButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list_item, parent, false) as View
        val holder = CustomViewHolder(view)
        holder.checkBox.setOnLongClickListener {
            Toast.makeText(parent.context, "I was held!", Toast.LENGTH_SHORT).show()
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.checkBox.text = habitList[position]
        holder.checkBox.isChecked = false
//        holder.checkBox.setOnClickListener {
//            val completedHabit = holder.checkBox.text.toString()
//            habitList.remove(completedHabit)
//            notifyItemRemoved(position)
//            Toast.makeText(context, "Habit completed!", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun getItemCount() = habitList.size
}