package it.polito.mad.car_pooling.models

data class TripRequestRating (var tripRequest: TripRequest){
    var rating: Rating? = null
}