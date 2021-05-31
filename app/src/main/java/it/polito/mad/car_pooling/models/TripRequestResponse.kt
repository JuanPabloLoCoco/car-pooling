package it.polito.mad.car_pooling.models

data class TripRequestResponse(var tripRequest: TripRequest, var passenger: Profile,var driver: Profile) {
}