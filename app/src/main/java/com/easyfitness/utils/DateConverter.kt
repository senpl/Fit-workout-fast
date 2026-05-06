package com.easyfitness.utils

import android.content.Context
import com.easyfitness.DAO.DAOUtils
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object DateConverter {
    val MILLISECONDINDAY: Int = 60 * 60 * 24 * 1000

    fun nbDays(millisecondes: Double): Double {
        return (millisecondes / MILLISECONDINDAY).toInt().toDouble()
    }

    fun nbMinutes(millisecondes: Double): Double {
        return (millisecondes / (60 * 1000))
    }

    fun nbMilliseconds(days: Double): Double {
        return days * MILLISECONDINDAY
    }


    val newDate: Date
        get() = Date()

    fun editToDate(editText: String): Date? {
        var date: Date?
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = dateFormat.parse(editText)
        } catch (e: ParseException) {
            e.printStackTrace()
            date = Date()
        }

        return date
    }

    fun localDateStrToDate(dateStr: String, pContext: Context): Date? {
        var date: Date?
        try {
            val dateFormat =
                android.text.format.DateFormat.getDateFormat(pContext.getApplicationContext())
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            date = Date()
        }
        return date
    }

    fun dateToLocalDateStr(date: Date?, pContext: Context): String {
        val dateFormat3 =
            android.text.format.DateFormat.getDateFormat(pContext.getApplicationContext())
        dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"))
        return dateFormat3.format(date)
    }

    fun dateToDBDateStr(date: Date?): String {
        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        return dateFormat.format(date)
    }

    fun DBDateStrToDate(dateStr: String): Date? {
        var date: Date?
        try {
            val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            date = Date()
        }
        return date
    }

    fun currentTime(): String {
        //Rajoute le moment du dernier ajout dans le bouton Add
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val df = DecimalFormat("00")
        return df.format(hours.toLong()) + ":" + df.format(minutes.toLong()) + ":" + df.format(
            seconds.toLong()
        )
    }

    fun currentDate(): String {
        //Rajoute le moment du dernier ajout dans le bouton Add
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val df = DecimalFormat("00")
        return df.format(day.toLong()) + "/" + df.format((month + 1).toLong()) + "/" + df.format(
            year.toLong()
        )
    }

    fun dateToString(year: Int, month: Int, day: Int): String {
        // Do something with the date chosen by the user
        val df = DecimalFormat("00")
        return df.format(day.toLong()) + "/" + df.format(month.toLong()) + "/" + df.format(year.toLong())
    }

    /**
     * @param year
     * @param month    0-based
     * @param day
     * @param pContext
     * @return date for local format
     */
    fun dateToLocalDateStr(year: Int, month: Int, day: Int, pContext: Context): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        val date = calendar.getTime()

        return dateToLocalDateStr(date, pContext)
    }

    fun dateToDate(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        return calendar.getTime()
    }

    /**
     * @param longVal in milliseconds
     * @return duration in format "HH:MM"
     */
    fun durationToHoursMinutesStr(longVal: Long): String {
        var longVal = longVal
        longVal = longVal / 1000
        val hours = longVal.toInt() / 3600
        val remainder = longVal.toInt() - hours * 3600
        val mins = remainder / 60

        //remainder = remainder - mins * 60;
        //int secs = remainder;
        return String.format("%02d:%02d", hours, mins)
    }

    /**
     * @param longVal in milliseconds
     * @return duration in format "HH:MM"
     */
    fun durationToHoursMinutesSecondsStr(longVal: Long): String {
        var longVal = longVal
        longVal = longVal / 1000
        val hours = longVal.toInt() / 3600
        var remainder = longVal.toInt() - hours * 3600
        val mins = remainder / 60
        remainder = remainder - mins * 60
        val secs = remainder

        return String.format("%02d:%02d:%02d", hours, mins, secs)
    }

    fun durationStringToLong(source: String): Long {
        val tokens: Array<String?> =
            source.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val secondsToMs = tokens[2]!!.toInt() * 1000
        val minutesToMs = tokens[1]!!.toInt() * 60000
        val hoursToMs = tokens[0]!!.toInt() * 3600000
        return (secondsToMs + minutesToMs + hoursToMs).toLong()
    }
}
