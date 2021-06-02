package it.polito.mad.car_pooling.Utils

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor

object TimeUtilFunctions {

    private val timeFormat = "HH:mm"
    private val dateFormat = "dd.MM.yyyy"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToDateViaInstant(dateToConvert: LocalDateTime): Date {
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                .toInstant());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimestampFromDateAndTime(date: String, time: String): Timestamp {
        val dateFormater = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val newDate = LocalDateTime.parse("$date $time", dateFormater)
        val newTimestamp = Timestamp(convertToDateViaInstant(newDate))
        return newTimestamp
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDateFromTimestamp(ts: Timestamp): String {
        return SimpleDateFormat(dateFormat).format(ts.toDate())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getTimeFromTimestamp(ts: Timestamp): String {
        return SimpleDateFormat(timeFormat).format(ts.toDate())
    }

    fun getTimestampDifferenceAsStr(departureTimestamp: Timestamp, arrivalTimestamp: Timestamp): String {
        val departureSeconds: Long = departureTimestamp.seconds
        val arrivalSeconds: Long = arrivalTimestamp.seconds

        // I can divide by 60 because I am not mannaging seconds
        var difference: Long = floor((arrivalSeconds - departureSeconds) / 60.0).toLong()
        // I have a difference in Minutes
        val minutes = difference % 60
        val hoursLong = floor(difference / 60.0).toLong()
        val hours = hoursLong % 24
        val days: Long = floor(hoursLong / 24.0).toLong()

        val minutesInt = minutes.toInt()
        val hoursInt = hours.toInt()
        val daysInt = days.toInt()

        val returnString = StringBuilder()
        if (daysInt > 0) {
            returnString.append("$daysInt days")
        }
        if (hoursInt > 0) {
            val sep = if (returnString.isEmpty()) "" else ", "
            returnString.append("${sep}$hoursInt hours")
        }
        if (minutesInt > 0){
            val sep = if (returnString.isEmpty()) "" else ", "
            returnString.append("${sep}$minutesInt minutes")
        }

        return returnString.toString()
    }
}