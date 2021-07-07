package com.uygemre.qrcode.helpers

import android.content.Context
import android.content.SharedPreferences
import com.uygemre.qrcode.constants.PrefConstants

class LocalPrefManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PrefConstants.PREF_NAME, 0)

    fun push(key: String, value: String) {
        sharedPreferences.edit()?.putString(key, value)?.apply()
    }

    fun push(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun push(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun push(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun push(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun push(key: String, value: Set<String?>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun pull(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue)!!

    fun pull(key: String, defaultValue: Int): Int = sharedPreferences.getInt(key, defaultValue)

    fun pull(key: String, defaultValue: Long): Long = sharedPreferences.getLong(key, defaultValue)

    fun pull(key: String, defaultValue: Float): Float =
        sharedPreferences.getFloat(key, defaultValue)

    fun pull(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    fun pull(key: String, defaultValue: Set<String>): Set<String> =
        sharedPreferences.getStringSet(key, defaultValue)!!

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}