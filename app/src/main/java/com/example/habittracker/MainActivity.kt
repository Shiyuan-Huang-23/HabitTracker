package com.example.habittracker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private val useBackend = true
    private val backendURL = "YOUR BACKEND URL"

    private val dateFormat = SimpleDateFormat("MM/dd/yy")
    /** Invariant: There are no duplicates in habitList */
    private var habitList : ArrayList<String> = ArrayList()
    private var habitMap : HashMap<String, Int> = HashMap()
    private var notesMap: HashMap<String, String> = HashMap()
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
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
            return
        }

        if (habitMap.contains(habit) || notesMap.contains(habit)) {
            displayError("This habit has already been created.")
        } else {
            habitList.add(habit)
            addHabitEditText.text.clear()
            if (useBackend) {
                val postJson = JSONObject()
                postJson.put("name", habit)
                postJson.put("notes", dialog.findViewById<EditText>(R.id.notesEditText).text.toString())
                val jsonStr = PostJSONTask().execute(backendURL, "api/habits/", postJson.toString()).get()
                val json = JSONObject(jsonStr)
                habitMap[habit] = json.getJSONObject("data").getInt("id")
            } else {
                notesMap[habit] = dialog.findViewById<EditText>(R.id.notesEditText).text.toString()
            }
            dialog.dismiss()
        }
    }

    /**
     * Effect: Deletes a habit
     */
    fun deleteHabit(view: View) {
        val habit = dialog.findViewById<TextView>(R.id.habitTitleTextView).text.toString()

        if (useBackend) {
            val id = habitMap[habit]
            DeleteJSONTask().execute(backendURL, "api/habits/$id/")
            habitMap.remove(habit)
        } else {
            notesMap.remove(habit)
        }
        val index = habitList.indexOf(habit)
        habitList.remove(habit)
        viewAdapter.notifyItemRemoved(index)
        dialog.dismiss()
    }

    /**
     * Effect: Edits the name or notes of a habit
     */
    fun editHabit(view: View) {
        val manager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)

        val habitEditText = dialog.findViewById<EditText>(R.id.habitEditText)
        val newHabit = habitEditText.text.toString()
        val oldHabit = habitEditText.tag as String
        if (newHabit.isEmpty()) {
            displayError("Please enter a name for your habit.")
        } else if (newHabit != oldHabit && (habitMap.contains(newHabit) || notesMap.contains(newHabit))) {
            displayError("This habit has already been created.")
        } else {
            habitEditText.text.clear()
            val notes = dialog.findViewById<EditText>(R.id.notesEditText).text.toString()
            if (useBackend) {
                val id = habitMap[oldHabit]
                val postJson = JSONObject()
                postJson.put("name", newHabit)
                postJson.put("notes", notes)
                val jsonStr = PostJSONTask().execute(backendURL, "api/habit/$id/", postJson.toString()).get()
                val json = JSONObject(jsonStr)
                habitMap[newHabit] = json.getJSONObject("data").getInt("id")
            } else {
                notesMap.remove(oldHabit)
                notesMap[newHabit] = notes
            }

            val index = habitList.indexOf(oldHabit)
            habitList[index] = newHabit
            viewAdapter.notifyItemChanged(index)
            dialog.dismiss()
        }
    }

    /**
     * Effect: Marks a habit as done.
     */
    fun completeHabit(view: View) {
        val completedHabit: String
        if (view is CheckBox) {
            completedHabit = view.text.toString()
        } else if (view is Button) {
            completedHabit = ((view.parent as LinearLayout).parent as LinearLayout).findViewById<TextView>(R.id.habitTitleTextView).text.toString()
        } else {
            return
        }

        if (useBackend) {
            val id = habitMap[completedHabit]
            PostJSONTask().execute(backendURL, "api/habit/$id/done/")
        }

        removeHabitFromView(completedHabit)
        Toast.makeText(applicationContext, "Habit completed!", Toast.LENGTH_SHORT).show()
        if (view is Button) {
            dialog.dismiss()
        }
    }

    /**
     * Effect: Marks a habit as skipped.
     */
    fun skipHabit(view: View) {
        val parent = view.parent as LinearLayout
        val habit = parent.findViewById<TextView>(R.id.habitTextView).text.toString()

        if (useBackend) {
            val id = habitMap[habit]
            val postJson = JSONObject()
            postJson.put("category", "skip")
            postJson.put("skip_note", parent.findViewById<EditText>(R.id.notesEditText).text)
            PostJSONTask().execute(backendURL, "api/event/$id/", postJson.toString())
        }
        removeHabitFromView(habit)
        dialog.dismiss()
    }

    /**
     * Effect: Removes a habit from the list of currently displayed habits.
     */
    private fun removeHabitFromView(habit: String) {
        val index = habitList.indexOf(habit)
        habitList.removeAt(index)
        viewAdapter.notifyItemRemoved(index)
    }

    /**
     * Effect: Displays the Create Habit dialog
     */
    fun displayCreateHabitDialog(view: View) {
        dialog.setContentView(R.layout.create_habit)
        dialog.show()
    }

    /**
     * Effect: Displays the Skip Habit dialog
     */
    fun displaySkipHabitDialog(view: View) {
        dialog.setContentView(R.layout.skip_habit)
        val habit = ((view.parent as LinearLayout).parent as LinearLayout).findViewById<TextView>(R.id.habitTitleTextView).text.toString()
        val habitTextView = dialog.findViewById<TextView>(R.id.habitTextView)
        habitTextView.text = habit
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
        val notesTextView = dialog.findViewById<TextView>(R.id.notesTextView)
        val calendar = dialog.findViewById<CompactCalendarView>(R.id.calendarView)
        calendar.setFirstDayOfWeek(Calendar.SUNDAY)
        if (useBackend) {
            val id = habitMap[habit]
            val jsonStr = GetJSONTask().execute(backendURL, "api/habit/$id/").get()
            val json = JSONObject(jsonStr)

            notesTextView.text = json.getJSONObject("data").getString("notes")

            val habitEvents = GetJSONTask().execute(backendURL, "api/events/$id/").get()
            Log.i("JSON", habitEvents)

            val eventJson = JSONArray(JSONObject(habitEvents).getString("data"))
            val debugging = JSONObject(habitEvents).getString("data")
            Log.i("JSON", debugging)
            Log.i("Length", eventJson.length().toString())
            for (i in 0 until eventJson.length()) {
                val eventObj = eventJson.getJSONObject(i)
                var color = Color.GREEN
                val category = eventObj.getString("category")
                if (category == "skip") {
                    color = Color.RED
                }
                Log.i("date", eventObj.getString("date"))
                val date = dateFormat.parse(eventObj.getString("date"), ParsePosition(0))
                if (date != null) {
                    val epochTime: Long = date.time + 18000000

                    val event = Event(color, epochTime, eventObj.getString("skip_note"))
                    calendar.addEvent(event)
                }
            }

        } else {
            notesTextView.text = notesMap[habit]
        }
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
                if (!o.getBoolean("done")) {
                    habitList.add(habit)
                }
                habitMap[habit] = o.getInt("id")
            }

        } catch (e: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (useBackend) {
            getHabitsOnCreate()
        }
        viewManager = LinearLayoutManager(this)
        viewAdapter = CustomAdapter(habitList, this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply{
            layoutManager = viewManager
            adapter = viewAdapter
        }

        dialog = Dialog(this)
    }
}
