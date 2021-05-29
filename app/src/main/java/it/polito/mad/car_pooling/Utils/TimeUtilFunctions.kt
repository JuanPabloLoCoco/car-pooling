package it.polito.mad.car_pooling.Utils

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtilFunctions {

    private val timeFormat = "HH:mm"
    private val dateFormat = "dd.MM.yyyy"

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToDateViaInstant(dateToConvert: LocalDateTime): Date {
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
}