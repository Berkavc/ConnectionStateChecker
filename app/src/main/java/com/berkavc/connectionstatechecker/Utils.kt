package com.berkavc.connectionstatechecker

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.berkavc.connectionstatechecker.service.ConnectivityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * This class will used for shared preferences.
 */
class SharedPreference(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun saveString(KEY_NAME: String, text: String?) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, text)
        editor.apply()
    }

    @SuppressLint("ApplySharedPref")
    fun saveStringSynchronized(KEY_NAME: String, text: String?) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, text)
        editor.commit()
    }

    fun saveInt(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.apply()
    }

    fun saveBoolean(KEY_NAME: String, status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.apply()
    }

    @SuppressLint("ApplySharedPref")
    fun saveBooleanSynchronized(KEY_NAME: String, status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.commit()
    }

    fun getValueString(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, null)
    }

    fun getValueInt(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {
        return sharedPref.getBoolean(KEY_NAME, defaultValue)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(KEY_NAME)
        editor.apply()
    }

}

 fun Context.startConnectivityService() {
    val intent = Intent(this, ConnectivityService::class.java)
    startForegroundService(intent)
}

fun Context.stopConnectivityService() {
    val intent = Intent(this, ConnectivityService::class.java)
    stopService(intent)
}

suspend fun getPingMs(url: String = "http://www.google.com"): Int = withContext(Dispatchers.IO) {
    try {
        val startTime = System.nanoTime()

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 1500
        connection.readTimeout = 1500
        connection.connect()
        connection.disconnect()

        val endTime = System.nanoTime()
        ((endTime - startTime) / 1_000_000).toInt()
    } catch (e: Exception) {
        -1
    }
}