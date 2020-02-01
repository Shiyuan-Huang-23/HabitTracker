package com.example.habittracker

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val backendURL = "http://192.168.56.1:5000/"

    /** Invariant: There are no duplicates in habitList */
    private var habitList : ArrayList<String> = ArrayList()
    private var habitMap : HashMap<String, Int> = HashMap()
    private lateinit var viewManager : RecyclerView.LayoutManager
    private lateinit var viewAdapter : RecyclerView.Adapter<*>
    private lateinit var recyclerView: RecyclerView
    private lateinit var dialog: Dialog

    /**
     * Effect: Adds a habit to Today's Habits
     */
    fun createHabit(view: View) {
        val manager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)

        val addHabitEditText = dialog.findViewById<EditText>(R.id.habitEditText)
        val habit = addHabitEditText.text.toString()
        if (habit.isEmpty()) {
            displayError("Please enter a name for your habit.")
        } else if (habitMap.contains(habit)) {
            displayError("This habit has already been created.")
        } else {
            habitList.add(habit)
            addHabitEditText.text.clear()
            val postJson = JSONObject()
            postJson.put("name", habit)
            postJson.put("notes", dialog.findViewById<EditText>(R.id.notesEditText).text.toString())
            val jsonStr = PostJSONTask().execute(backendURL, "api/habits/", postJson.toString()).get()
            val json = JSONObject(jsonStr)
            habitMap[habit] = json.getJSONObject("data").getInt("id")
            dialog.dismiss()
        }
    }

    /**
     * Effect: Deletes a habit
     */
    fun deleteHabit(view: View) {
        val habit = dialog.findViewById<TextView>(R.id.habitTitleTextView).text.toString()

        val id = habitMap[habit]
        DeleteJSONTask().execute(backendURL, "api/habits/$id/")

        habitMap.remove(habit)
        val index = habitList.indexOf(habit)
        habitList.remove(habit)
        viewAdapter.notifyItemRemoved(index)

        dialog.dismiss()
    }

    fun editHabit(view: View) {
        val manager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)

        val habitEditText = dialog.findViewById<EditText>(R.id.habitEditText)
        val newHabit = habitEditText.text.toString()
        val oldHabit = habitEditText.tag as String
        if (newHabit.isEmpty()) {
            displayError("Please enter a name for your habit.")
        } else if (habitMap.contains(newHabit) && newHabit != oldHabit) {
            displayError("This habit has already been created.")
        } else {
            habitEditText.text.clear()
            val id = habitMap[oldHabit]
            val postJson = JSONObject()
            postJson.put("name", newHabit)
            postJson.put("notes", dialog.findViewById<EditText>(R.id.notesEditText).text.toString())
            val jsonStr = PostJSONTask().execute(backendURL, "api/habit/$id/", postJson.toString()).get()
            val json = JSONObject(jsonStr)
            habitMap[newHabit] = json.getJSONObject("data").getInt("id")

            val index = habitList.indexOf(oldHabit)
            habitList.remove(oldHabit)
            viewAdapter.notifyItemRemoved(index)
            habitList.add(index, newHabit)
            viewAdapter.notifyItemInserted(index)
            dialog.dismiss()
        }
    }

    /**
     * Effect: Displays the Create Habit dialog
     */
    fun displayCreateHabitDialog(view: View) {
        dialog.setContentView(R.layout.create_habit)
        dialog.show()
    }

    /**
     * Effect: Displays a habit's information (notes, etc.) as well as Edit and Delete options
     */
    fun showInfo(view: View) {
        dialog.setContentView(R.layout.habit_info)

        val habitTitleTextView = dialog.findViewById<TextView>(R.id.habitTitleTextView)
        val habit = (((view as Button).parent) as LinearLayout).findViewById<CheckBox>(R.id.checkBox).text
        habitTitleTextView.text = habit

        val id = habitMap[habit]
        val jsonStr = GetJSONTask().execute(backendURL, "api/habit/$id/").get()
        val json = JSONObject(jsonStr)

        val notesTextView = dialog.findViewById<TextView>(R.id.notesTextView)
        notesTextView.text = json.getJSONObject("data").getString("notes")

        dialog.show()
    }

    /**
     * Effect: Displays the Edit Habit Dialog
     */
    fun displayEditHabitDialog(view: View) {
        dialog.setContentView(R.layout.edit_habit)
        val parent = (((view as Button).parent) as LinearLayout).parent as LinearLayout
        val habitEditText = dialog.findViewById<EditText>(R.id.habitEditText)
        val habit = parent.findViewById<TextView>(R.id.habitTitleTextView).text
        habitEditText.text = SpannableStringBuilder(habit)
        habitEditText.tag = habit.toString()
        val notesEditText = dialog.findViewById<EditText>(R.id.notesEditText)
        notesEditText.text = SpannableStringBuilder(parent.findViewById<TextView>(R.id.notesTextView).text)
    }

    /**
     * Effect: Closes the currently-displayed dialog box
     */
    fun closeDialog(view: View) {
        dialog.dismiss()
    }

    /** Displays the given error message. */
    private fun displayError(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }

    /**
     * Effect: Fills habitList and notesMap with appropriate information from
     * backend on startup.
     */
    private fun getHabitsOnCreate() {
        val json = GetJSONTask().execute(backendURL, "api/habits/").get()
        try {
            val data = JSONObject(json).getString("data")
            val jsonArray = JSONArray(data)
            for (i in 0..jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                val habit = o.getString("name")
                habitList.add(habit)
                habitMap[habit] = o.getInt("id")
            }

        } catch (e: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getHabitsOnCreate()
        viewManager = LinearLayoutManager(this)
        viewAdapter = CustomAdapter(habitList, this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply{
            layoutManager = viewManager
            adapter = viewAdapter
        }

        dialog = Dialog(this)
    }
}
