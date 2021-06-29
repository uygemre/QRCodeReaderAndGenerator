package com.uygemre.qrcode.extensions

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateExtensions {

    @Throws(ParseException::class)
    fun dateDiff2(date: String, format: String = "dd MMMM yyyy HH:mm"): String {
        val inputFormatString = "dd-MM-yyyy'T'hh:mm:ss'Z'"
        val inputDateFormat = SimpleDateFormat(inputFormatString, Locale.getDefault())
        val inputDate = inputDateFormat.parse(date)
        val outputFormat = SimpleDateFormat(format, Locale.getDefault())
        return outputFormat.format(inputDate)
    }

    @Throws(ParseException::class)
    fun dateDiff4(date: String, format: String = "dd MMMM yyyy"): String {
        val inputFormatString = "dd MM yyyy HH:mm"
        val inputDateFormat = SimpleDateFormat(inputFormatString, Locale.getDefault())
        val inputDate = inputDateFormat.parse(date)
        val outputFormat = SimpleDateFormat(format, Locale.getDefault())
        return outputFormat.format(inputDate)
    }

    @Throws(ParseException::class)
    fun dateDiff6(date: String, inFormat: String? = "dd MM yyyy HH:mm", outFormat: String = "dd MMMM yyyy"): String {
        val inputDateFormat = SimpleDateFormat(inFormat, Locale.getDefault())
        val inputDate = inputDateFormat.parse(date)
        val outputFormat = SimpleDateFormat(outFormat, Locale("TR", "tr"))
        return outputFormat.format(inputDate)
    }

    @Throws(ParseException::class)
    fun dateDiff8(outputFormat: String? = "dd EEE MMM yyyy HH:mm aa"): String {
        val dateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun addTodayToDate(amount: Int = 0, inputFormat: String = "dd-MM-yyyy", endFormat: String = "dd-MM-yyyy"): String {
        val startDate = SimpleDateFormat(inputFormat, Locale.getDefault()).format(Date())
        val startDateSimpleDateFormat = SimpleDateFormat(inputFormat)
        val calendar = Calendar.getInstance()
        try {
            calendar.time = startDateSimpleDateFormat.parse(startDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        calendar.add(Calendar.DATE, amount)
        val endDateSimpleDateFormat = SimpleDateFormat(endFormat)
        return endDateSimpleDateFormat.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun currentDateString(inputFormat: String = "dd-MM-yyyy", outputFormat: String = "dd-MM-yyyy"): String {
        val startDate = SimpleDateFormat(inputFormat, Locale.getDefault()).format(Date())
        val startDateSimpleDateFormat = SimpleDateFormat(inputFormat)
        val calendar = Calendar.getInstance()
        try {
            calendar.time = startDateSimpleDateFormat.parse(startDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val endDateSimpleDateFormat = SimpleDateFormat(outputFormat)
        return endDateSimpleDateFormat.format(calendar.time)
    }
}