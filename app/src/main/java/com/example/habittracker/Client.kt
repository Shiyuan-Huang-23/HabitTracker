package com.example.habittracker

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Makes a GET request to the specified route and returns the JSON obtained.
 * params[0] is the address of the server
 * params[1] is the specific route
 */
class GetJSONTask : AsyncTask<String, Int, String>() {
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

/**
 * Makes a POST request to the specified route and returns the JSON obtained.
 * params[0] is the address of the server
 * params[1] is the specific route
 * params[1] is the JSON to be posted, as a string
 */
class PostJSONTask : AsyncTask<String, Int, String>() {

    override fun doInBackground(vararg params: String?): String {
        val result = StringBuilder()
        try {
            val url = URL(params[0] + params[1])
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8")
            urlConnection.setRequestProperty("Accept", "application/json")
            urlConnection.doOutput = true

            val outputStream = DataOutputStream(urlConnection.outputStream)
            outputStream.writeBytes(params[2])
            outputStream.close()
            urlConnection.connect()

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