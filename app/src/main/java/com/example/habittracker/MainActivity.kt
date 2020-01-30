package com.example.habittracker

import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    private class GetHabitsTask : AsyncTask<Int, Int, String>() {
        val backendURL = "http://100.008.00.1:5070/"
        override fun doInBackground(vararg params: Int?): String {
            val result = StringBuilder()
            try {
                val url = URL( backendURL + "api/habits/")
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

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val output = StringBuilder()
            try {
                val dataString = JSONObject(result).getString("data")
                val jsonArray = JSONArray(dataString)
                for (i in 0..jsonArray.length()) {
                    val o = jsonArray.getJSONObject(i)
                    output.append(o.getString("name") + " " + o.getString("notes"))
                }

            } catch (e: Exception) {}
            Log.i("JSON", output.toString())
        }
    }

    /** Invariant: There are no duplicates in habitList */
    private var habitList : ArrayList<String> = ArrayList()
    private var notesMap : HashMap<String, String> = HashMap()
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
            notesMap[habit] = dialog.findViewById<EditText>(R.id.notesEditText).text.toString()
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

        val notesTextView = dialog.findViewById<TextView>(R.id.notesTextView)
        notesTextView.text = notesMap[habitTitleTextView.text.toString()]

        dialog.show()
        val data = GetHabitsTask().execute(1).get()
        Log.i("data", data)
        // Toast.makeText(this, data, Toast.LENGTH_LONG).show()
    }

    /**
     * Effect: Closes the currently-displayed dialog box
     */
    fun closeDialog(view: View) {
        dialog.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        habitList = arrayListOf("10 push-ups", "Hug frogs", "Attempt handstands")
        for (s in habitList) {
            notesMap[s] = "None"
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
