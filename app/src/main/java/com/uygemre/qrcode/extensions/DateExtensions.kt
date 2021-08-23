package com.uygemre.qrcode.extensions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateExtensions {

    @Throws(ParseException::class)
    fun dateDiff8(outputFormat: String? = "dd EEE MMM yyyy HH:mm aa"): String {
        val dateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }
}