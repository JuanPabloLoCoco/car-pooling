package it.polito.mad.car_pooling.models

data class Rating (var comment: String, var ratingNumber: Double) {
    var writer: String = ""
    var rated: String = ""
    var requestTripId: String = ""
}