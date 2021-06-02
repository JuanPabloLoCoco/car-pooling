package it.polito.mad.car_pooling.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class Rating (var comment: String, var ratingNumber: Double) {
    var writer: String = ""
    var rated: String = ""
    var tripId: String = ""
    var id: String = ""

    companion object {
        private val COMMENT = "comment"
        private val RATED = "ratedId"
        private val WRITER = "writerId"
        private val TRIP_ID = "tripId"
        private val RATING_NUMBER = "ratingNumber"
        private const val TAG = "Rating"

        fun DocumentSnapshot.toRating(): Rating? {
            val newRating = Rating("", 1.0)
            try {
                newRating.id = id
                newRating.comment = getString(COMMENT)?: ""
                newRating.ratingNumber = getDouble(RATING_NUMBER) ?: 0.0
                newRating.rated = getString(RATED) ?: ""
                newRating.writer = getString(WRITER) ?: ""
                newRating.tripId = getString(TRIP_ID) ?: ""
                return newRating
            } catch (e: Exception) {
                Log.d(TAG, "Error converting rating with id ${newRating.id}", e)
                return null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        var returnMap = mapOf<String, Any>(
            COMMENT to comment,
            RATED to rated,
            WRITER to writer,
            TRIP_ID to tripId,
            RATING_NUMBER to ratingNumber
        )
        return returnMap
    }
}