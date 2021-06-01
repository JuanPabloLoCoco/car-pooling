package it.polito.mad.car_pooling.models


data class TripRequestResponse(var tripRequest: TripRequest) {
    var passenger: Profile = Profile("passenger")
    var driver: Profile = Profile("driver")

    constructor(tripRequest: TripRequest, passenger: Profile, driver: Profile):this(tripRequest) {
        this.passenger = passenger
        this.driver = driver
    }
}