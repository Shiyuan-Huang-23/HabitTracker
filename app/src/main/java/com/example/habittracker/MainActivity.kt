package com.example.habittracker

import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val backendURL = "http://192.168.56.1:5000/"

    /**
     * Makes a get request to the specified route and returns the JSON obtained.
     * The first argument is the address of the server, and the second is the specific route.
     */
    private class GetJSONTask : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg params: String?): String {
            val result = StringBuilder()
            try {
                val url = URL(params[0] + params[1])
                val urlConnection = url.openConnection() as HttpURLConnection
                val inStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inStream))
                var data = reader.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = reader.read()
                }
            } catch (e : IOException) {
                return e.toString()
            }
            return result.toString()
        }
    }

    private class PostJSONTask : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg params: String?): String {
            val result = StringBuilder()
            try {
                val url = URL(params[0] + params[1])
                val urlConnection = url.openConnection() as HttpURLConnection
                val inStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inStream))
                var data = reader.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = reader.read()
                }
            } catch (e : IOException) {
                return e.toString()
            }
            return result.toString()
        }
    }


    /** Invariant: There are no duplicates in habitList */
    private var habitList : ArrayList<String> = ArrayList()
    private var habitMap : HashMap<String, Int> = HashMap()
//    private var notesMap : HashMap<String, String> = HashMap()
//    private var habitIdList : HashMap<String, Int> = HashMap()
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
            Toast.makeText(applicationContext, "Please enter a name for your habit.", Toast.LENGTH_SHORT).show()
        } else if (habitList.contains(habit)) {
            Toast.makeText(applicationContext, "This habit has already been created.", Toast.LENGTH_SHORT).show()
        } else {
            habitList.add(habit)
            addHabitEditText.text.clear()
            TODO("Add notes to habit on creation")
            // notesMap[habit] = dialog.findViewById<EditText>(R.id.notesEditText).text.toString()
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
     * Effect: Deletes a habit
     */
    fun deleteHabit(view: View) {
        val habit = dialog.findViewById<TextView>(R.id.habitTitleTextView).text.toString()
        val index = habitList.indexOf(habit)
        habitList.remove(habit)
        viewAdapter.notifyItemRemoved(index)
        dialog.dismiss()
    }

    /**
     * Effect: Displays a habit's information (notes, etc.) as well as Edit and Delete options
     */
    fun showInfo(view: View) {
        dialog.setContentView(R.layout.habit_info)

        val habitTitleTextView = dialog.findViewById<TextView>(R.id.habitTitleTextView)
        habitTitleTextView.text = (((view as Button).parent) as LinearLayout).findViewById<CheckBox>(R.id.checkBox).text

        TODO("Pull habit notes from backend on show info")
        // val notesTextView = dialog.findViewById<TextView>(R.id.notesTextView)
        // notesTextView.text = notesMap[habitTitleTextView.text.toString()]

        dialog.show()
    }

    /**
     * Effect: Closes the currently-displayed dialog box
     */
    fun closeDialog(view: View) {
        dialog.dismiss()
    }

    /**
     * Effect: Fills {@code habitList} and {@code notesMap} with appropriate information from
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
